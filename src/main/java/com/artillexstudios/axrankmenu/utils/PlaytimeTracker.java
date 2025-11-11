package com.artillexstudios.axrankmenu.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axrankmenu.AxRankMenu.getInstance;

public class PlaytimeTracker {
    private static final Map<UUID, Long> cachedPlaytime = new HashMap<>();

    public static void init() {
        // Check playtime every 5 minutes to update cache
        Scheduler.get().runTimer(task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateCache(player.getUniqueId());
            }
        }, 20L * 60L * 5L, 20L * 60L * 5L); // Every 5 minutes

        Bukkit.getConsoleSender().sendMessage("§a[AxRankMenu] PlaytimeTracker initialized - Reading from world stats!");
    }

    public static void startTracking(Player player) {
        UUID uuid = player.getUniqueId();
        updateCache(uuid);
    }

    public static void stopTracking(Player player) {
        // No need to do anything, stats are managed by Minecraft
    }

    private static void updateCache(UUID uuid) {
        long playtime = readPlaytimeFromStats(uuid);
        cachedPlaytime.put(uuid, playtime);
    }

    /**
     * Read playtime from Minecraft's stats file (world/stats/uuid.json)
     * Returns playtime in ticks (20 ticks = 1 second)
     */
    private static long readPlaytimeFromStats(UUID uuid) {
        try {
            // Get the world folder
            File worldFolder = Bukkit.getWorlds().get(0).getWorldFolder();
            File statsFile = new File(worldFolder, "stats" + File.separator + uuid.toString() + ".json");

            if (!statsFile.exists()) {
                return 0L;
            }

            // Read the JSON file
            FileReader reader = new FileReader(statsFile);
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            // Get the stats object
            if (!json.has("stats")) {
                return 0L;
            }

            JsonObject stats = json.getAsJsonObject("stats");

            // Get custom stats (minecraft:custom)
            if (!stats.has("minecraft:custom")) {
                return 0L;
            }

            JsonObject customStats = stats.getAsJsonObject("minecraft:custom");

            // Get play_time (formerly known as play_one_minute)
            long playTimeTicks = 0L;
            
            // Try different stat names (for different Minecraft versions)
            if (customStats.has("minecraft:play_time")) {
                playTimeTicks = customStats.get("minecraft:play_time").getAsLong();
            } else if (customStats.has("minecraft:play_one_minute")) {
                playTimeTicks = customStats.get("minecraft:play_one_minute").getAsLong();
            }

            return playTimeTicks;

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§c[AxRankMenu] Error reading stats for " + uuid + ": " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Get total playtime in milliseconds
     */
    public static long getPlaytime(UUID uuid) {
        // Check cache first
        if (cachedPlaytime.containsKey(uuid)) {
            return ticksToMilliseconds(cachedPlaytime.get(uuid));
        }
        
        // Otherwise read from stats
        long ticks = readPlaytimeFromStats(uuid);
        cachedPlaytime.put(uuid, ticks);
        return ticksToMilliseconds(ticks);
    }

    /**
     * Convert ticks to milliseconds (20 ticks = 1 second = 1000ms)
     */
    private static long ticksToMilliseconds(long ticks) {
        return (ticks * 1000L) / 20L;
    }

    /**
     * Get total playtime in days
     */
    public static long getPlaytimeDays(UUID uuid) {
        long millis = getPlaytime(uuid);
        return TimeUnit.MILLISECONDS.toDays(millis);
    }

    /**
     * Get total playtime in hours
     */
    public static long getPlaytimeHours(UUID uuid) {
        long millis = getPlaytime(uuid);
        return TimeUnit.MILLISECONDS.toHours(millis);
    }

    /**
     * Get total playtime in minutes
     */
    public static long getPlaytimeMinutes(UUID uuid) {
        long millis = getPlaytime(uuid);
        return TimeUnit.MILLISECONDS.toMinutes(millis);
    }

    /**
     * Check if player has played for at least the specified days
     */
    public static boolean hasPlayedForDays(UUID uuid, int days) {
        return getPlaytimeDays(uuid) >= days;
    }

    /**
     * Check if player has played for at least the specified hours
     */
    public static boolean hasPlayedForHours(UUID uuid, int hours) {
        return getPlaytimeHours(uuid) >= hours;
    }

    /**
     * Set playtime manually (for testing purposes)
     * @param uuid Player UUID
     * @param playtimeMillis Playtime in milliseconds
     */
    public static void setPlaytime(UUID uuid, long playtimeMillis) {
        // Convert milliseconds to ticks for cache consistency
        long ticks = (playtimeMillis * 20L) / 1000L;
        cachedPlaytime.put(uuid, ticks);
        Bukkit.getConsoleSender().sendMessage("§a[AxRankMenu] Set playtime for " + uuid + " to " + playtimeMillis + "ms (" + ticks + " ticks)");
    }

    public static void save() {
        // No need to save, stats are managed by Minecraft
    }

    public static void shutdown() {
        // No cleanup needed
    }
}
