package com.artillexstudios.axrankmenu.commands;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.AxRankMenu;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.commands.annotations.Groups;
import com.artillexstudios.axrankmenu.effects.PersistentEffectManager;
import com.artillexstudios.axrankmenu.grindable.GrindableRankManager;
import com.artillexstudios.axrankmenu.gui.impl.RankGui;
import com.artillexstudios.axrankmenu.hooks.HookManager;
import com.artillexstudios.axrankmenu.items.PersistentItemRewardManager;
import com.artillexstudios.axrankmenu.utils.CommandMessages;
import com.artillexstudios.axrankmenu.utils.PlaytimeTracker;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;
import revxrsal.commands.orphan.Orphans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.artillexstudios.axrankmenu.AxRankMenu.CONFIG;
import static com.artillexstudios.axrankmenu.AxRankMenu.LANG;
import static com.artillexstudios.axrankmenu.AxRankMenu.MESSAGEUTILS;
import static com.artillexstudios.axrankmenu.AxRankMenu.RANKS;

public class Commands implements OrphanCommand {

    @DefaultFor({"~", "~ open"})
    @CommandPermission(value = "axrankmenu.use")
    public void open(@NotNull Player sender) {
        new RankGui(sender).open();
    }

    @Subcommand({"reload"})
    @CommandPermission(value = "axrankmenu.reload")
    public void reload(@NotNull Player sender) {
        if (!CONFIG.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload.failed", Collections.singletonMap("%file%", "tiers.yml"));
            return;
        }

        if (!LANG.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload.failed", Collections.singletonMap("%file%", "lang.yml"));
            return;
        }

        if (!RANKS.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload.failed", Collections.singletonMap("%file%", "ranks.yml"));
            return;
        }

        new HookManager().updateHooks();

        MESSAGEUTILS.sendLang(sender, "reload.success");
    }

    @Subcommand({"addrank"})
    @CommandPermission(value = "axrankmenu.admin.addrank")
    public void addRank(@NotNull Player sender, @Groups String group) {
        final Section section = RANKS.getBackingDocument().createSection(group);
        section.set("rank", group);
        section.set("server", "");
        section.set("price", -1.0);
        section.set("currency", "Vault");
        section.set("slot", getFirstEmptySlot());
        section.set("item.type", "GRAY_BANNER");
        section.set("item.name", "&#00FF00" + group + " &fRANK");
        section.set("item.lore", Arrays.asList(
                " ",
                " &7- &fPrice: &#00AA00$%price%",
                " ",
                "&#00FF00ᴘᴇʀᴍɪssɪᴏɴs",
                " &7- &f%permission%",
                " ",
                "&#00FF00&l(!) &#00FF00Click here to purchase!"
        ));
        section.set("buy-actions", Arrays.asList(
                "[MESSAGE] &#00FF00You have purchased the &f%name%&#00FF00! &7(%rank%)",
                "[CONSOLE] lp user %player% parent set " + group,
                "[CLOSE] menu"
        ));
        RANKS.save();
        MESSAGEUTILS.sendLang(sender, "add-rank", Map.of("%rank%", group));
    }

    @Subcommand({"test effect"})
    @CommandPermission(value = "axrankmenu.admin.test")
    public void testEffect(@NotNull Player sender, String playerName, String effectName, int durationDays, int simulateDays) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Player not found: " + playerName));
            return;
        }

        PotionEffectType effectType = PotionEffectType.getByName(effectName.toUpperCase());
        if (effectType == null) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Invalid effect type: " + effectName));
            sender.sendMessage(StringUtils.formatToString("&#FFD700Available effects: SPEED, SLOW, HASTE, MINING_FATIGUE, STRENGTH, INSTANT_HEALTH, INSTANT_DAMAGE, JUMP_BOOST, NAUSEA, REGENERATION, RESISTANCE, FIRE_RESISTANCE, WATER_BREATHING, INVISIBILITY, BLINDNESS, NIGHT_VISION, HUNGER, WEAKNESS, POISON, WITHER, HEALTH_BOOST, ABSORPTION, SATURATION, GLOWING, LEVITATION, LUCK, UNLUCK, SLOW_FALLING, CONDUIT_POWER, DOLPHINS_GRACE, BAD_OMEN, HERO_OF_THE_VILLAGE, DARKNESS"));
            return;
        }

        if (durationDays <= 0) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Duration must be a positive number!"));
            return;
        }
        
        if (simulateDays < 0) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Simulated days cannot be negative!"));
            return;
        }

        // Allow simulating beyond expiration for testing
        // if (simulateDays > durationDays) {
        //     sender.sendMessage(StringUtils.formatToString("&#FF0000Simulated days cannot be greater than duration days!"));
        //     return;
        // }

        // Calculate the expiration time as if the effect was added 'simulateDays' ago
        long currentTime = System.currentTimeMillis();
        long daysInMillis = durationDays * 24L * 60L * 60L * 1000L;
        long simulateMillis = simulateDays * 24L * 60L * 60L * 1000L;
        long adjustedStartTime = currentTime - simulateMillis;
        long expirationTime = adjustedStartTime + daysInMillis;

        long remainingDays = (expirationTime - currentTime) / (1000L * 60L * 60L * 24L);
        
        // Add effect with adjusted time (even if expired, for testing)
        PersistentEffectManager.addEffectWithCustomTime(target.getUniqueId(), effectType, 0, expirationTime);
        
        // If expired, immediately remove the visual effect from player
        if (remainingDays < 0) {
            target.removePotionEffect(effectType);
        }
        
        sender.sendMessage(StringUtils.formatToString("&#33FF33Test effect added to " + target.getName() + "!"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Effect: " + effectName.toUpperCase() + " I"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Total Duration: " + durationDays + " days"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Simulated: " + simulateDays + " days elapsed"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Remaining: " + remainingDays + " days"));
        
        if (remainingDays <= 0) {
            sender.sendMessage(StringUtils.formatToString("&#FF9900Note: Effect is EXPIRED! It will be removed on next cleanup cycle."));
            sender.sendMessage(StringUtils.formatToString("&#FF9900The effect was NOT applied to the player (already expired)."));
        }
    }

    @Subcommand({"test playtime"})
    @CommandPermission(value = "axrankmenu.admin.test")
    public void testPlaytime(@NotNull Player sender, String playerName, int hours) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Player not found: " + playerName));
            return;
        }

        if (hours < 0) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Hours must be a positive number!"));
            return;
        }

        // Set simulated playtime
        long playtimeMillis = hours * 60L * 60L * 1000L; // Convert hours to milliseconds
        PlaytimeTracker.setPlaytime(target.getUniqueId(), playtimeMillis);

        double totalDays = hours / 24.0;
        
        sender.sendMessage(StringUtils.formatToString("&#33FF33Simulated playtime set for " + target.getName() + "!"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Playtime: " + hours + " hours (" + String.format("%.1f", totalDays) + " days)"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Checking for eligible grindable ranks..."));

        // Check for grindable ranks
        if (CONFIG.getBoolean("grindable-ranks.enabled", true)) {
            GrindableRankManager.checkPlayerRanks(target);
            sender.sendMessage(StringUtils.formatToString("&#33FF33Grindable rank check completed! Check player's permissions."));
        } else {
            sender.sendMessage(StringUtils.formatToString("&#FF9900Note: Grindable ranks are disabled in config.yml"));
        }
    }
    
    @Subcommand({"test item"})
    @CommandPermission(value = "axrankmenu.admin.test")
    public void testItem(@NotNull Player sender, String playerName, String itemString, int durationDays, int simulateDays) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Player not found: " + playerName));
            return;
        }
        
        if (durationDays <= 0) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Duration must be a positive number!"));
            return;
        }
        
        if (simulateDays < 0) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Simulated days cannot be negative!"));
            return;
        }
        
        // Parse the item
        ItemStack itemStack = PersistentItemRewardManager.parseItemStack(itemString);
        if (itemStack == null) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Invalid item format!"));
            sender.sendMessage(StringUtils.formatToString("&#FFD700Format: MATERIAL:AMOUNT:NAME:ENCHANT:LEVEL,ENCHANT:LEVEL"));
            sender.sendMessage(StringUtils.formatToString("&#FFD700Examples:"));
            sender.sendMessage(StringUtils.formatToString("  &f- DIAMOND:5:&#33FF33Daily Diamond"));
            sender.sendMessage(StringUtils.formatToString("  &f- DIAMOND_SWORD:1:&#FFD700Legendary Sword:SHARPNESS:5,FIRE_ASPECT:2"));
            sender.sendMessage(StringUtils.formatToString("  &f- GOLDEN_APPLE:3:&#FFAACure Apple"));
            return;
        }
        
        // Calculate start time to simulate days elapsed
        long currentTime = System.currentTimeMillis();
        long simulateMillis = simulateDays * 24L * 60L * 60L * 1000L;
        long adjustedStartTime = currentTime - simulateMillis;
        
        // Add the item reward with adjusted start time
        PersistentItemRewardManager.addTestItemReward(target.getUniqueId(), itemStack, durationDays, adjustedStartTime);
        
        // Calculate remaining days for display
        long expirationTime = adjustedStartTime + (durationDays * 24L * 60L * 60L * 1000L);
        long remainingDays = (expirationTime - currentTime) / (1000L * 60L * 60L * 24L);
        String itemName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() 
            ? itemStack.getItemMeta().getDisplayName() 
            : itemStack.getType().name().replace("_", " ");
        
        sender.sendMessage(StringUtils.formatToString("&#33FF33Test item reward added to " + target.getName() + "!"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Item: " + itemName + " x" + itemStack.getAmount()));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Total Duration: " + durationDays + " days"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Simulated: " + simulateDays + " days elapsed"));
        sender.sendMessage(StringUtils.formatToString("&#FFD700Remaining: " + remainingDays + " days"));
        
        if (remainingDays <= 0) {
            sender.sendMessage(StringUtils.formatToString("&#FF9900Note: Reward is EXPIRED! It will be removed on next cleanup cycle."));
            sender.sendMessage(StringUtils.formatToString("&#FF9900The item was NOT given to the player (already expired)."));
        } else {
            sender.sendMessage(StringUtils.formatToString("&#33FF33Player received the item and will get it daily for " + remainingDays + " more days!"));
        }
    }
    
    @Subcommand({"set"})
    @CommandPermission(value = "axrankmenu.admin.set")
    public void setRank(@NotNull Player sender, String playerName, String rankName) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Player not found: " + playerName));
            return;
        }
        
        // Find the rank in ranks.yml
        Section rankSection = null;
        
        for (String key : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
            String configRank = RANKS.getString(key + ".rank");
            if (configRank != null && configRank.equalsIgnoreCase(rankName)) {
                rankSection = RANKS.getSection(key);
                break;
            }
        }
        
        if (rankSection == null) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000Rank not found in ranks.yml: " + rankName));
            sender.sendMessage(StringUtils.formatToString("&#FFD700Available ranks: " + String.join(", ", getRankNames())));
            return;
        }
        
        // Check if the rank group exists in LuckPerms
        final LuckPerms luckPerms = LuckPermsProvider.get();
        Group group = luckPerms.getGroupManager().getGroup(rankName);
        if (group == null) {
            sender.sendMessage(StringUtils.formatToString("&#FF0000LuckPerms group not found: " + rankName));
            return;
        }
        
        // Set the player's primary group
        final Section finalRankSection = rankSection;
        final String finalRankName = rankName;
        final String serverContext = rankSection.getString("server", "");
        
        Bukkit.getScheduler().runTaskAsynchronously(AxRankMenu.getInstance(), () -> {
            try {
                luckPerms.getUserManager().modifyUser(target.getUniqueId(), user -> {
                    user.setPrimaryGroup(finalRankName);
                });
                
                // Execute buy-actions if available
                var actions = finalRankSection.getStringList("buy-actions");
                if (!actions.isEmpty()) {
                    for (String action : actions) {
                        final String[] type = action.split(" ");
                        String ac = action.replace(type[0] + " ", "");
                        ac = ac.replace("%player%", target.getName());
                        ac = ac.replace("%name%", finalRankSection.getString("item.name", finalRankName));
                        ac = ac.replace("%rank%", finalRankName);
                        ac = ac.replace("%price%", finalRankSection.getString("price", "0"));
                        ac = ac.replace("%server%", serverContext);

                        switch (type[0]) {
                            case "[MESSAGE]": {
                                target.sendMessage(StringUtils.formatToString(ac));
                                break;
                            }
                            case "[CONSOLE]": {
                                String finalAc = ac;
                                Bukkit.getScheduler().runTask(AxRankMenu.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalAc));
                                break;
                            }
                            case "[CLOSE]": {
                                target.closeInventory();
                                break;
                            }
                            case "[EFFECT]": {
                                String[] effectArgs = ac.split(" ");
                                if (effectArgs.length >= 3) {
                                    try {
                                        String effectName = effectArgs[0].toUpperCase();
                                        int amplifier = Integer.parseInt(effectArgs[1]);
                                        int durationDays = Integer.parseInt(effectArgs[2]);
                                        
                                        PotionEffectType effectType = PotionEffectType.getByName(effectName);
                                        if (effectType != null) {
                                            PersistentEffectManager.addEffect(target.getUniqueId(), effectType, amplifier, durationDays);
                                        }
                                    } catch (NumberFormatException ignored) {}
                                }
                                break;
                            }
                            case "[ITEM]": {
                                String[] itemArgs = ac.split(" ");
                                if (itemArgs.length >= 2) {
                                    try {
                                        int durationDays = Integer.parseInt(itemArgs[itemArgs.length - 1]);
                                        String itemString = ac.substring(0, ac.lastIndexOf(" ")).trim();
                                        
                                        ItemStack itemStack = PersistentItemRewardManager.parseItemStack(itemString);
                                        if (itemStack != null) {
                                            PersistentItemRewardManager.addItemReward(target.getUniqueId(), itemStack, durationDays);
                                        }
                                    } catch (NumberFormatException ignored) {}
                                }
                                break;
                            }
                        }
                    }
                }
                
                sender.sendMessage(StringUtils.formatToString("&#33FF33Successfully set " + target.getName() + "'s rank to " + finalRankName + "!"));
                sender.sendMessage(StringUtils.formatToString("&#FFD700All rank rewards have been granted."));
                
            } catch (Exception e) {
                sender.sendMessage(StringUtils.formatToString("&#FF0000Failed to set rank: " + e.getMessage()));
                e.printStackTrace();
            }
        });
    }
    
    private List<String> getRankNames() {
        List<String> ranks = new ArrayList<>();
        for (String key : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
            String rank = RANKS.getString(key + ".rank");
            if (rank != null && !rank.isEmpty()) {
                ranks.add(rank);
            }
        }
        return ranks;
    }

    private int getFirstEmptySlot() {
        final ArrayList<Integer> filled = new ArrayList<>();
        for (String str : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
            filled.add(RANKS.getInt(str + ".slot", -1));
        }
        for (int i = 0; i < RANKS.getInt("rows", 3) * 9; i++) {
            if (filled.contains(i)) continue;
            return i;
        }
        return -1;
    }

    private static BukkitCommandHandler handler = null;

    public static void registerCommand() {
        if (handler == null) {
            handler = BukkitCommandHandler.create(AxRankMenu.getInstance());

            handler.getAutoCompleter().registerSuggestionFactory(parameter -> {
                if (parameter.hasAnnotation(Groups.class)) {
                    return (args, sender, command) -> {
                        final LuckPerms luckPerms = LuckPermsProvider.get();
                        final Set<Group> groups = new HashSet<>(luckPerms.getGroupManager().getLoadedGroups());
                        groups.removeIf(group -> {
                            for (String str : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
                                if (RANKS.getString(str + ".rank", "").equalsIgnoreCase(group.getName())) return true;
                            }
                            return false;
                        });

                        return groups.stream().map(Group::getName).collect(Collectors.toList());
                    };
                }
                return null;
            });

            handler.getTranslator().add(new CommandMessages());
            handler.setLocale(new Locale("en", "US"));
        }
        handler.unregisterAllCommands();

        handler.register(Orphans.path(CONFIG.getStringList("command-aliases").toArray(String[]::new)).handler(new Commands()));
        handler.registerBrigadier();
    }
}
