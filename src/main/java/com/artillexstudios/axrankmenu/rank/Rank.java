package com.artillexstudios.axrankmenu.rank;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.NumberUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.hooks.HookManager;
import com.artillexstudios.axrankmenu.hooks.currency.CurrencyHook;
import com.artillexstudios.axrankmenu.effects.PersistentEffectManager;
import com.artillexstudios.axrankmenu.items.PersistentItemRewardManager;
import com.artillexstudios.axrankmenu.utils.ItemBuilderUtil;
import com.artillexstudios.axrankmenu.utils.PlaceholderUtils;
import com.artillexstudios.axrankmenu.utils.PurchaseTracker;
import com.artillexstudios.axrankmenu.utils.PlaytimeTracker;
import dev.triumphteam.gui.guis.GuiItem;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.track.Track;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.artillexstudios.axrankmenu.AxRankMenu.CONFIG;
import static com.artillexstudios.axrankmenu.AxRankMenu.LANG;
import static com.artillexstudios.axrankmenu.AxRankMenu.MESSAGEUTILS;
import static com.artillexstudios.axrankmenu.AxRankMenu.RANKS;

public class Rank {
    private static final LuckPerms luckPerms = LuckPermsProvider.get();
    private final Group group;
    private final Section section;
    private final Player requester;

    public Rank(@NotNull Section section, @NotNull Player requester) {
        this.section = section;
        this.requester = requester;
        group = luckPerms.getGroupManager().getGroup(section.getString("rank"));
    }

    public Node[] getNodes() {
        return group.getNodes().stream().filter(node -> !node.isNegated()).toArray(Node[]::new);
    }

    public GuiItem getItem() {
        final List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("item.lore")) {
            if (line.contains("%permission%")) {
                LANG.getBackingDocument().setGeneralSettings(GeneralSettings.builder().setRouteSeparator('倀').build());
                for (Node node : getNodes()) {
                    ImmutableContextSet set = luckPerms.getContextManager().getStaticContext();
                    if (!section.getString("server", "").isEmpty()) {
                        set = ImmutableContextSet.of("server", section.getString("server"));
                    }

                    if (!CONFIG.getBoolean("include-global-permissions") && !node.getContexts().equals(set)) continue;
                    if (CONFIG.getBoolean("include-global-permissions") && !node.getContexts().isEmpty() && !node.getContexts().equals(set)) continue;
                    String permission = node.getKey();

                    Integer number = null;
                    for (String t1 : permission.split("\\.")) {
                        if (!NumberUtils.isInt(t1)) continue;
                        number = Integer.parseInt(t1);
                    }

                    permission = permission.replace("" + number, "#");

                    if (LANG.getString("permissions倀" + permission) == null) {
                        LANG.set("permissions倀" + permission, permission);
                        LANG.save();
                    }

                    String tName = LANG.getString("permissions倀" + permission);
                    if (tName.isEmpty()) continue;
                    lore.add(PlaceholderUtils.parsePlaceholders(requester, line.replace("%permission%", tName.replace("#", "" + number)), section));
                }
                LANG.getBackingDocument().setGeneralSettings(GeneralSettings.builder().setRouteSeparator('.').build());
            } else {
                lore.add(PlaceholderUtils.parsePlaceholders(requester, line, section));
            }
        }

        final ItemStack it = ItemBuilderUtil.newBuilder(section.getSection("item"), requester).setLore(lore).get();

        return new GuiItem(it, event -> {
            String rankName = section.getString("rank");
            
            // Check prerequisite rank first (must be purchased before this one)
            String prerequisiteRank = section.getString("requires-rank", null);
            if (prerequisiteRank != null && !prerequisiteRank.isEmpty()) {
                if (!PurchaseTracker.hasPurchased(requester.getUniqueId(), prerequisiteRank)) {
                    MESSAGEUTILS.sendLang(requester, "error.requires-previous-rank", 
                        java.util.Map.of("%rank%", prerequisiteRank)
                    );
                    return;
                }
            }
            
            // Check playtime requirement second
            int requiredPlaytimeHours = section.getInt("required-playtime-hours", 0);
            if (requiredPlaytimeHours > 0) {
                long currentPlaytimeHours = PlaytimeTracker.getPlaytimeHours(requester.getUniqueId());
                if (currentPlaytimeHours < requiredPlaytimeHours) {
                    MESSAGEUTILS.sendLang(requester, "error.insufficient-playtime", 
                        java.util.Map.of(
                            "%required%", String.valueOf(requiredPlaytimeHours),
                            "%current%", String.valueOf(currentPlaytimeHours),
                            "%remaining%", String.valueOf(requiredPlaytimeHours - currentPlaytimeHours)
                        )
                    );
                    return;
                }
            }
            
            // Check if rank is one-time purchase only
            boolean isOneTimePurchase = section.getBoolean("one-time-purchase", false);
            
            if (isOneTimePurchase && PurchaseTracker.hasPurchased(requester.getUniqueId(), rankName)) {
                MESSAGEUTILS.sendLang(requester, "error.already-purchased");
                return;
            }
            
            final String cGroupName = luckPerms.getUserManager().getUser(requester.getUniqueId()).getPrimaryGroup();
            final Group cGroup = luckPerms.getGroupManager().getGroup(cGroupName);
            if (CONFIG.getBoolean("prevent-downgrading", true) && cGroup.getWeight().isPresent() && group.getWeight().isPresent() && group.getWeight().getAsInt() <= cGroup.getWeight().getAsInt()) {
                MESSAGEUTILS.sendLang(requester, "error.downgrade-disabled");
                return;
            }

            if (CONFIG.getBoolean("force-buy-order.enabled", false)) {
                final Track track = luckPerms.getTrackManager().getTrack(CONFIG.getString("force-buy-order.track"));
                final String nextGroup = track.getNext(cGroup);
                if (nextGroup == null && !track.containsGroup(cGroup) && !track.getGroups().get(0).equals(group.getName())) {
                    MESSAGEUTILS.sendLang(requester, "error.buy-order");
                    return;
                }
                if (nextGroup != null && !group.getName().equals(nextGroup)) {
                    MESSAGEUTILS.sendLang(requester, "error.buy-order");
                    return;
                }
            }

            double price = section.getDouble("price", -1.0D);
            final String currency = section.getString("currency", "Vault");

            if (price == -1) return;

            if (CONFIG.getBoolean("discount-ranks", false)) {
                double currentPrice = Math.max(RANKS.getDouble(cGroup.getName() + ".price", -1), RANKS.getDouble(cGroup.getName().toUpperCase() + ".price", -1));
                if (currentPrice != -1) {
                    price -= currentPrice;
                    price = Math.max(0, price);
                }
            }

            final CurrencyHook hook = HookManager.getCurrencyHook(currency);
            if (hook == null) return;

            if (hook.getBalance(requester) < price) {
                MESSAGEUTILS.sendLang(requester, "buy.no-currency");
                return;
            }

            hook.takeBalance(requester, price);
            
            // Mark as purchased if it's a one-time purchase
            if (isOneTimePurchase) {
                PurchaseTracker.markAsPurchased(requester.getUniqueId(), rankName);
            }

            var actions = section.getStringList("buy-actions");
            if (actions.isEmpty()) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] The buy-actions section is missing from the " + section.getString("rank") + " rank, this will cause issues!"));
            }
            for (String action : actions) { // todo: add a warning if missing
                final String[] type = action.split(" ");
                String ac = action.replace(type[0] + " ", "");
                ac = ac.replace("%player%", requester.getName());
                ac = ac.replace("%name%", section.getString("item.name"));
                ac = ac.replace("%rank%", section.getString("rank"));
                ac = ac.replace("%price%", section.getString("price", "---"));
                ac = ac.replace("%server%", section.getString("server"));

                switch (type[0]) {
                    case "[MESSAGE]": {
                        requester.sendMessage(StringUtils.formatToString(ac));
                        break;
                    }
                    case "[CONSOLE]": {
                        String finalAc = ac;
                        Scheduler.get().execute(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalAc));
                        break;
                    }
                    case "[CLOSE]": {
                        requester.closeInventory();
                        break;
                    }
                    case "[EFFECT]": {
                        // Format: [EFFECT] <effect_type> <amplifier> <duration_days>
                        String[] effectArgs = ac.split(" ");
                        if (effectArgs.length >= 3) {
                            try {
                                String effectName = effectArgs[0].toUpperCase();
                                int amplifier = Integer.parseInt(effectArgs[1]);
                                int durationDays = Integer.parseInt(effectArgs[2]);
                                
                                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                                if (effectType != null) {
                                    PersistentEffectManager.addEffect(requester.getUniqueId(), effectType, amplifier, durationDays);
                                    requester.sendMessage(StringUtils.formatToString("&#33FF33You have received " + effectName + " (Level " + (amplifier + 1) + ") for " + durationDays + " days!"));
                                } else {
                                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] Invalid effect type: " + effectName));
                                }
                            } catch (NumberFormatException e) {
                                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] Invalid effect parameters in: " + action));
                            }
                        } else {
                            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] Invalid [EFFECT] action format. Expected: [EFFECT] <type> <amplifier> <days>"));
                        }
                        break;
                    }
                    case "[ITEM]": {
                        // Format: [ITEM] <item_string> <duration_days>
                        // Item string format: MATERIAL:AMOUNT:NAME:ENCHANT:LEVEL,ENCHANT:LEVEL
                        // Example: [ITEM] DIAMOND:5:&6Special Diamond 7
                        // Example: [ITEM] DIAMOND_SWORD:1:&6Legendary Sword:SHARPNESS:5,FIRE_ASPECT:2 30
                        String[] itemArgs = ac.split(" ");
                        if (itemArgs.length >= 2) {
                            try {
                                int durationDays = Integer.parseInt(itemArgs[itemArgs.length - 1]);
                                String itemString = ac.substring(0, ac.lastIndexOf(" ")).trim();
                                
                                ItemStack itemStack = PersistentItemRewardManager.parseItemStack(itemString);
                                if (itemStack != null) {
                                    PersistentItemRewardManager.addItemReward(requester.getUniqueId(), itemStack, durationDays);
                                } else {
                                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] Invalid item format: " + itemString));
                                }
                            } catch (NumberFormatException e) {
                                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] Invalid duration in [ITEM] action: " + action));
                            }
                        } else {
                            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] Invalid [ITEM] action format. Expected: [ITEM] <item_string> <days>"));
                        }
                        break;
                    }
                }
            }
        });
    }

    @Nullable
    public Group getGroup() {
        return group;
    }

    public Section getSection() {
        return section;
    }
}
