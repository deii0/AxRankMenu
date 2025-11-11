package com.artillexstudios.axrankmenu.utils;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.*;

import static com.artillexstudios.axrankmenu.AxRankMenu.getInstance;

public class PurchaseTracker {
    private static Config purchaseData;
    private static final Map<UUID, Set<String>> purchasedRanks = new HashMap<>();

    public static void init() {
        File dataFile = new File(getInstance().getDataFolder(), "purchases.yml");
        purchaseData = new Config(
                dataFile,
                null,
                GeneralSettings.builder().setUseDefaults(false).build(),
                LoaderSettings.builder().setAutoUpdate(false).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setKeepAll(true).build()
        );

        // Load existing purchase data
        for (String key : purchaseData.getBackingDocument().getRoutesAsStrings(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<String> ranks = purchaseData.getStringList(key);
                purchasedRanks.put(uuid, new HashSet<>(ranks));
            } catch (IllegalArgumentException ignored) {
            }
        }

        Bukkit.getConsoleSender().sendMessage("§a[AxRankMenu] PurchaseTracker initialized - Loaded " + purchasedRanks.size() + " players!");
    }

    /**
     * Check if a player has already purchased a rank
     */
    public static boolean hasPurchased(UUID playerId, String rankName) {
        Set<String> ranks = purchasedRanks.get(playerId);
        return ranks != null && ranks.contains(rankName);
    }

    /**
     * Mark a rank as purchased for a player
     */
    public static void markAsPurchased(UUID playerId, String rankName) {
        Set<String> ranks = purchasedRanks.computeIfAbsent(playerId, k -> new HashSet<>());
        ranks.add(rankName);
        save();
    }

    /**
     * Get all purchased ranks for a player
     */
    public static Set<String> getPurchasedRanks(UUID playerId) {
        return purchasedRanks.getOrDefault(playerId, new HashSet<>());
    }

    /**
     * Remove a purchase record (for admin use)
     */
    public static void removePurchase(UUID playerId, String rankName) {
        Set<String> ranks = purchasedRanks.get(playerId);
        if (ranks != null) {
            ranks.remove(rankName);
            if (ranks.isEmpty()) {
                purchasedRanks.remove(playerId);
            }
            save();
        }
    }

    /**
     * Clear all purchases for a player (for admin use)
     */
    public static void clearPurchases(UUID playerId) {
        purchasedRanks.remove(playerId);
        purchaseData.set(playerId.toString(), null);
        save();
    }

    private static void save() {
        for (Map.Entry<UUID, Set<String>> entry : purchasedRanks.entrySet()) {
            purchaseData.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }
        purchaseData.save();
    }

    public static void shutdown() {
        save();
    }
}
