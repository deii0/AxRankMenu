package com.artillexstudios.axrankmenu.grindable;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.effects.PersistentEffectManager;
import com.artillexstudios.axrankmenu.utils.PlaytimeTracker;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.artillexstudios.axrankmenu.AxRankMenu.CONFIG;
import static com.artillexstudios.axrankmenu.AxRankMenu.MESSAGEUTILS;
import static com.artillexstudios.axrankmenu.AxRankMenu.RANKS;

public class GrindableRankManager {
    private static final Set<UUID> processedPlayers = new HashSet<>();
    private static LuckPerms luckPerms;

    public static void init() {
        luckPerms = LuckPermsProvider.get();
        
        // Check for grindable ranks every minute
        Scheduler.get().runTimer(task -> {
            checkGrindableRanks();
        }, 20L * 60L, 20L * 60L); // Every 60 seconds

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] GrindableRankManager initialized!"));
    }

    public static void checkGrindableRanks() {
        if (!CONFIG.getBoolean("grindable-ranks.enabled", false)) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayerRanks(player);
        }
    }

    public static void checkPlayerRanks(Player player) {
        if (!CONFIG.getBoolean("grindable-ranks.enabled", false)) return;

        UUID playerId = player.getUniqueId();
        List<GrindableRank> eligibleRanks = getEligibleRanks(player);

        for (GrindableRank grindableRank : eligibleRanks) {
            // Check if already processed (already has the rank)
            if (hasReceivedRank(playerId, grindableRank.getRankName())) {
                continue;
            }

            // Grant the rank
            grantRank(player, grindableRank);
        }
    }

    private static List<GrindableRank> getEligibleRanks(Player player) {
        List<GrindableRank> eligible = new ArrayList<>();
        UUID playerId = player.getUniqueId();

        for (String route : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
            Section section = RANKS.getSection(route);
            if (section == null) continue;

            // Check if this is a grindable rank
            if (!section.getBoolean("grindable", false)) continue;

            String rankName = section.getString("rank");
            if (rankName == null) continue;

            // Check playtime requirement
            int requiredDays = section.getInt("required-playtime-days", 0);
            int requiredHours = section.getInt("required-playtime-hours", 0);

            boolean meetsRequirement = false;
            if (requiredDays > 0) {
                meetsRequirement = PlaytimeTracker.hasPlayedForDays(playerId, requiredDays);
            } else if (requiredHours > 0) {
                meetsRequirement = PlaytimeTracker.hasPlayedForHours(playerId, requiredHours);
            }

            if (meetsRequirement && !hasReceivedRank(playerId, rankName)) {
                eligible.add(new GrindableRank(rankName, section));
            }
        }

        return eligible;
    }

    private static void grantRank(Player player, GrindableRank grindableRank) {
        String rankName = grindableRank.getRankName();
        Section section = grindableRank.getSection();

        // Set the rank using LuckPerms
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // Execute buy-actions
        var actions = section.getStringList("buy-actions");
        for (String action : actions) {
            String[] type = action.split(" ", 2);
            if (type.length < 2) continue;

            String ac = type[1];
            ac = ac.replace("%player%", player.getName());
            ac = ac.replace("%name%", section.getString("item.name", rankName));
            ac = ac.replace("%rank%", rankName);

            switch (type[0]) {
                case "[MESSAGE]": {
                    player.sendMessage(StringUtils.formatToString(ac));
                    break;
                }
                case "[CONSOLE]": {
                    String finalAc = ac;
                    Scheduler.get().execute(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalAc));
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
                                PersistentEffectManager.addEffect(player.getUniqueId(), effectType, amplifier, durationDays);
                                player.sendMessage(StringUtils.formatToString("&#33FF33You have received " + effectName + " (Level " + (amplifier + 1) + ") for " + durationDays + " days!"));
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    break;
                }
            }
        }

        // Send notification message
        MESSAGEUTILS.sendLang(player, "grindable-rank.received", 
            java.util.Map.of("%rank%", rankName));

        // Mark as received
        markRankAsReceived(player.getUniqueId(), rankName);

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString(
            "&#33FF33[AxRankMenu] Player " + player.getName() + " received grindable rank: " + rankName
        ));
    }

    private static boolean hasReceivedRank(UUID playerId, String rankName) {
        // Check if player already has this rank
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return false;

        // Check if player's primary group matches
        String primaryGroup = user.getPrimaryGroup();
        if (primaryGroup.equalsIgnoreCase(rankName)) {
            return true;
        }

        // Check if player has the group in their inherited groups
        return user.getInheritedGroups(user.getQueryOptions()).stream()
                .anyMatch(group -> group.getName().equalsIgnoreCase(rankName));
    }

    private static void markRankAsReceived(UUID playerId, String rankName) {
        processedPlayers.add(playerId);
    }

    /**
     * Inner class to represent a grindable rank
     */
    private static class GrindableRank {
        private final String rankName;
        private final Section section;

        public GrindableRank(String rankName, Section section) {
            this.rankName = rankName;
            this.section = section;
        }

        public String getRankName() {
            return rankName;
        }

        public Section getSection() {
            return section;
        }
    }
}
