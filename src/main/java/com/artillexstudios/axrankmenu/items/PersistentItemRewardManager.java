package com.artillexstudios.axrankmenu.items;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axrankmenu.AxRankMenu.getInstance;

public class PersistentItemRewardManager {
    private static Config itemRewardsConfig;
    private static final Map<UUID, List<PersistentItemReward>> activeRewards = new ConcurrentHashMap<>();

    public static void init() {
        File dataFile = new File(getInstance().getDataFolder(), "item-rewards.yml");
        itemRewardsConfig = new Config(
                dataFile,
                null,
                GeneralSettings.builder().setUseDefaults(false).build(),
                LoaderSettings.builder().setAutoUpdate(false).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setKeepAll(true).build()
        );

        // Load existing item rewards from config
        loadFromConfig();

        // Start daily check task (runs every hour to check for daily claims)
        Scheduler.get().runTimer(task -> {
            giveEligibleDailyRewards();
            removeExpiredRewards();
        }, 20L, 20L * 60L * 60L); // Check every hour

        Bukkit.getConsoleSender().sendMessage("§a[AxRankMenu] PersistentItemRewardManager initialized - Loaded " + activeRewards.size() + " players with active rewards!");
    }

    /**
     * Add a new daily item reward for a player
     */
    public static void addItemReward(UUID playerId, ItemStack itemStack, int durationDays) {
        long currentTime = System.currentTimeMillis();
        PersistentItemReward reward = new PersistentItemReward(playerId, itemStack, durationDays, currentTime, 0L);
        
        List<PersistentItemReward> playerRewards = activeRewards.computeIfAbsent(playerId, k -> new ArrayList<>());
        playerRewards.add(reward);
        
        // Give the first item immediately
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            giveItem(player, itemStack);
            player.sendMessage(StringUtils.formatToString("&#33FF33You have received " + getItemDisplayName(itemStack) + " x" + itemStack.getAmount() + "!"));
            player.sendMessage(StringUtils.formatToString("&#FFD700You will receive this item daily for the next " + durationDays + " days!"));
        }
        
        save();
    }
    
    /**
     * Add a test item reward with custom start time (for testing)
     * @param playerId Player UUID
     * @param itemStack Item to give
     * @param durationDays Duration in days
     * @param startTime Custom start timestamp
     */
    public static void addTestItemReward(UUID playerId, ItemStack itemStack, int durationDays, long startTime) {
        PersistentItemReward reward = new PersistentItemReward(playerId, itemStack, durationDays, startTime, 0L);
        
        List<PersistentItemReward> playerRewards = activeRewards.computeIfAbsent(playerId, k -> new ArrayList<>());
        playerRewards.add(reward);
        
        // Only give the first item immediately if NOT expired
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline() && !reward.isExpired()) {
            giveItem(player, itemStack);
            player.sendMessage(StringUtils.formatToString("&#33FF33You received " + getItemDisplayName(itemStack) + " x" + itemStack.getAmount() + "!"));
        } else if (player != null && reward.isExpired()) {
            player.sendMessage(StringUtils.formatToString("&#FF9900Test item reward is EXPIRED - no item given!"));
        }
        
        save();
        
        long daysElapsed = (System.currentTimeMillis() - startTime) / (1000L * 60L * 60L * 24L);
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString(
            "&#33FF33[AxRankMenu] Added test item reward " + getItemDisplayName(itemStack) + 
            " x" + itemStack.getAmount() + " to " + playerId + " for " + durationDays + " days (started " + 
            daysElapsed + " days ago, " + (reward.isExpired() ? "EXPIRED" : "active") + ")"
        ));
    }

    /**
     * Give eligible daily rewards to online players
     */
    private static void giveEligibleDailyRewards() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<PersistentItemReward> rewards = activeRewards.get(player.getUniqueId());
            if (rewards == null) continue;

            for (PersistentItemReward reward : rewards) {
                if (!reward.isExpired() && reward.canClaimToday()) {
                    giveItem(player, reward.getItemStack());
                    reward.setLastClaimTime(System.currentTimeMillis());
                    player.sendMessage(StringUtils.formatToString("&#33FF33Daily reward: " + getItemDisplayName(reward.getItemStack()) + " x" + reward.getItemStack().getAmount() + "!"));
                    player.sendMessage(StringUtils.formatToString("&#FFD700Remaining days: " + reward.getRemainingDays()));
                }
            }
        }
        save();
    }

    /**
     * Give item to player (tries to add to inventory, drops if full)
     */
    private static void giveItem(Player player, ItemStack itemStack) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack.clone());
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItem(player.getLocation(), item);
            }
            player.sendMessage(StringUtils.formatToString("&#FF9900Your inventory is full! Items dropped at your feet."));
        }
    }

    /**
     * Remove expired rewards
     */
    private static void removeExpiredRewards() {
        boolean modified = false;
        for (UUID playerId : new ArrayList<>(activeRewards.keySet())) {
            List<PersistentItemReward> rewards = activeRewards.get(playerId);
            if (rewards == null) continue;

            int originalSize = rewards.size();
            rewards.removeIf(PersistentItemReward::isExpired);

            if (rewards.isEmpty()) {
                activeRewards.remove(playerId);
                modified = true;
            } else if (rewards.size() != originalSize) {
                modified = true;
            }
        }
        if (modified) {
            save();
        }
    }

    /**
     * Check and give rewards when player joins
     */
    public static void onPlayerJoin(UUID playerId) {
        List<PersistentItemReward> rewards = activeRewards.get(playerId);
        if (rewards == null) return;

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        // Check for any pending daily rewards
        for (PersistentItemReward reward : rewards) {
            if (!reward.isExpired() && reward.canClaimToday()) {
                giveItem(player, reward.getItemStack());
                reward.setLastClaimTime(System.currentTimeMillis());
                player.sendMessage(StringUtils.formatToString("&#33FF33Daily reward: " + getItemDisplayName(reward.getItemStack()) + " x" + reward.getItemStack().getAmount() + "!"));
                player.sendMessage(StringUtils.formatToString("&#FFD700Remaining days: " + reward.getRemainingDays()));
            }
        }
        save();
    }

    /**
     * Get display name of an item
     */
    private static String getItemDisplayName(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }
        return itemStack.getType().name().replace("_", " ");
    }

    /**
     * Parse ItemStack from string format
     * Format: MATERIAL:AMOUNT:NAME:ENCHANT:LEVEL,ENCHANT:LEVEL
     * Example: DIAMOND_SWORD:1:&6Legendary Sword:SHARPNESS:5,FIRE_ASPECT:2
     */
    public static ItemStack parseItemStack(String itemString) {
        String[] parts = itemString.split(":");
        
        if (parts.length < 1) return null;

        Material material = Material.getMaterial(parts[0].toUpperCase());
        if (material == null) return null;

        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        ItemStack itemStack = new ItemStack(material, amount);

        if (parts.length > 2) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                // Set display name
                String name = parts[2];
                if (!name.isEmpty() && !name.equals("none")) {
                    meta.setDisplayName(StringUtils.formatToString(name));
                }

                // Set enchantments
                if (parts.length > 3) {
                    String[] enchants = parts[3].split(",");
                    for (String enchant : enchants) {
                        String[] enchantParts = enchant.split(":");
                        if (enchantParts.length == 2) {
                            Enchantment enchantment = Enchantment.getByName(enchantParts[0].toUpperCase());
                            if (enchantment != null) {
                                int level = Integer.parseInt(enchantParts[1]);
                                meta.addEnchant(enchantment, level, true);
                            }
                        }
                    }
                }

                itemStack.setItemMeta(meta);
            }
        }

        return itemStack;
    }

    /**
     * Save rewards to config
     */
    private static void save() {
        itemRewardsConfig.getBackingDocument().clear();

        for (Map.Entry<UUID, List<PersistentItemReward>> entry : activeRewards.entrySet()) {
            String playerKey = entry.getKey().toString();
            List<Map<String, Object>> rewardsList = new ArrayList<>();

            for (PersistentItemReward reward : entry.getValue()) {
                Map<String, Object> rewardData = new HashMap<>();
                ItemStack item = reward.getItemStack();
                
                rewardData.put("material", item.getType().name());
                rewardData.put("amount", item.getAmount());
                rewardData.put("duration-days", reward.getDurationDays());
                rewardData.put("start-time", reward.getStartTime());
                rewardData.put("last-claim-time", reward.getLastClaimTime());

                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    rewardData.put("name", item.getItemMeta().getDisplayName());
                }

                if (item.hasItemMeta() && !item.getItemMeta().getEnchants().isEmpty()) {
                    List<String> enchants = new ArrayList<>();
                    for (Map.Entry<Enchantment, Integer> ench : item.getItemMeta().getEnchants().entrySet()) {
                        enchants.add(ench.getKey().getName() + ":" + ench.getValue());
                    }
                    rewardData.put("enchantments", enchants);
                }

                rewardsList.add(rewardData);
            }

            itemRewardsConfig.set(playerKey, rewardsList);
        }

        itemRewardsConfig.save();
    }

    /**
     * Load rewards from config
     */
    private static void loadFromConfig() {
        activeRewards.clear();

        for (String key : itemRewardsConfig.getBackingDocument().getRoutesAsStrings(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                Section playerSection = itemRewardsConfig.getSection(key);
                if (playerSection == null) continue;

                List<PersistentItemReward> rewards = new ArrayList<>();

                for (Object obj : playerSection.getList("")) {
                    if (!(obj instanceof Map)) continue;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> rewardData = (Map<String, Object>) obj;

                    String materialName = (String) rewardData.get("material");
                    Material material = Material.getMaterial(materialName);
                    if (material == null) continue;

                    int amount = ((Number) rewardData.get("amount")).intValue();
                    int durationDays = ((Number) rewardData.get("duration-days")).intValue();
                    long startTime = ((Number) rewardData.get("start-time")).longValue();
                    long lastClaimTime = ((Number) rewardData.get("last-claim-time")).longValue();

                    ItemStack itemStack = new ItemStack(material, amount);
                    ItemMeta meta = itemStack.getItemMeta();

                    if (meta != null && rewardData.containsKey("name")) {
                        meta.setDisplayName((String) rewardData.get("name"));
                    }

                    if (meta != null && rewardData.containsKey("enchantments")) {
                        @SuppressWarnings("unchecked")
                        List<String> enchants = (List<String>) rewardData.get("enchantments");
                        for (String enchant : enchants) {
                            String[] parts = enchant.split(":");
                            if (parts.length == 2) {
                                Enchantment enchantment = Enchantment.getByName(parts[0]);
                                if (enchantment != null) {
                                    meta.addEnchant(enchantment, Integer.parseInt(parts[1]), true);
                                }
                            }
                        }
                    }

                    if (meta != null) {
                        itemStack.setItemMeta(meta);
                    }

                    PersistentItemReward reward = new PersistentItemReward(playerId, itemStack, durationDays, startTime, lastClaimTime);
                    if (!reward.isExpired()) {
                        rewards.add(reward);
                    }
                }

                if (!rewards.isEmpty()) {
                    activeRewards.put(playerId, rewards);
                }

            } catch (IllegalArgumentException e) {
                Bukkit.getConsoleSender().sendMessage("§c[AxRankMenu] Failed to load item reward for key: " + key);
            }
        }
    }

    /**
     * Get active rewards for a player
     */
    public static List<PersistentItemReward> getActiveRewards(UUID playerId) {
        return activeRewards.getOrDefault(playerId, new ArrayList<>());
    }

    /**
     * Shutdown and save
     */
    public static void shutdown() {
        save();
    }
}
