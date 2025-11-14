package com.artillexstudios.axrankmenu.effects;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.artillexstudios.axrankmenu.AxRankMenu.getInstance;

public class PersistentEffectManager {
    private static Config effectsData;
    private static final Map<UUID, List<PersistentEffect>> activeEffects = new ConcurrentHashMap<>();

    public static void init() {
        File dataFile = new File(getInstance().getDataFolder(), "effects.yml");
        effectsData = new Config(
                dataFile,
                null,
                GeneralSettings.builder().setUseDefaults(false).build(),
                LoaderSettings.builder().setAutoUpdate(false).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setKeepAll(true).build()
        );

        loadEffects();

        // Check and apply effects every 30 seconds
        Scheduler.get().runTimer(task -> {
            checkAndApplyEffects();
            removeExpiredEffects();
        }, 20L * 30L, 20L * 30L);

        // Auto-save every 5 minutes
        Scheduler.get().runTimer(task -> save(), 20L * 60L * 5L, 20L * 60L * 5L);

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] PersistentEffectManager initialized!"));
    }

    private static void loadEffects() {
        activeEffects.clear();
        
        for (String uuidStr : effectsData.getBackingDocument().getRoutesAsStrings(false)) {
            try {
                UUID playerId = UUID.fromString(uuidStr);
                List<PersistentEffect> playerEffects = new ArrayList<>();
                
                Section playerSection = effectsData.getSection(uuidStr);
                if (playerSection == null) continue;
                
                for (String effectKey : playerSection.getRoutesAsStrings(false)) {
                    String fullPath = uuidStr + "." + effectKey;
                    String effectName = effectsData.getString(fullPath + ".type");
                    int amplifier = effectsData.getInt(fullPath + ".amplifier", 0);
                    long expiration = effectsData.getLong(fullPath + ".expiration", 0L);
                    
                    PotionEffectType effectType = PotionEffectType.getByName(effectName);
                    if (effectType == null) continue;
                    
                    PersistentEffect effect = new PersistentEffect(playerId, effectType, amplifier, expiration);
                    if (!effect.isExpired()) {
                        playerEffects.add(effect);
                    }
                }
                
                if (!playerEffects.isEmpty()) {
                    activeEffects.put(playerId, playerEffects);
                }
            } catch (IllegalArgumentException e) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF3333[AxRankMenu] Invalid UUID in effects.yml: " + uuidStr));
            }
        }
        
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxRankMenu] Loaded " + activeEffects.size() + " players with active effects!"));
    }

    /**
     * Add a persistent effect to a player
     * @param playerId Player UUID
     * @param effectType Effect type
     * @param amplifier Effect amplifier (0 = level 1)
     * @param durationDays Duration in days
     */
    public static void addEffect(UUID playerId, PotionEffectType effectType, int amplifier, int durationDays) {
        long expirationTime = System.currentTimeMillis() + (durationDays * 24L * 60L * 60L * 1000L);
        addEffectWithCustomTime(playerId, effectType, amplifier, expirationTime);
    }

    /**
     * Add a persistent effect with custom expiration time (for testing)
     * @param playerId Player UUID
     * @param effectType Effect type
     * @param amplifier Effect amplifier (0 = level 1)
     * @param expirationTime Custom expiration timestamp in milliseconds
     */
    public static void addEffectWithCustomTime(UUID playerId, PotionEffectType effectType, int amplifier, long expirationTime) {
        PersistentEffect effect = new PersistentEffect(playerId, effectType, amplifier, expirationTime);
        
        List<PersistentEffect> playerEffects = activeEffects.computeIfAbsent(playerId, k -> new ArrayList<>());
        
        // Remove existing effect of the same type
        playerEffects.removeIf(e -> e.getEffectType().equals(effectType));
        
        // Add new effect
        playerEffects.add(effect);
        
        // Apply immediately if player is online
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            applyEffect(player, effect);
        }
        
        save();
        
        long remainingDays = effect.getRemainingDays();
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString(
            "&#33FF33[AxRankMenu] Added effect " + effectType.getName() + 
            " (Level " + (amplifier + 1) + ") to " + playerId + " for " + remainingDays + " days"
        ));
    }

    /**
     * Apply effect to online player
     */
    private static void applyEffect(Player player, PersistentEffect effect) {
        if (effect.isExpired()) return;
        
        // Remove existing effect of same type first
        player.removePotionEffect(effect.getEffectType());
        
        // Apply new effect with max duration (we'll reapply periodically)
        // Using Integer.MAX_VALUE ticks as "permanent" until expiration
        PotionEffect potionEffect = new PotionEffect(
                effect.getEffectType(),
                Integer.MAX_VALUE,
                effect.getAmplifier(),
                true, // ambient
                true, // particles
                true  // icon
        );
        player.addPotionEffect(potionEffect);
    }

    /**
     * Check and apply all effects to online players
     */
    private static void checkAndApplyEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<PersistentEffect> effects = activeEffects.get(player.getUniqueId());
            if (effects == null || effects.isEmpty()) continue;
            
            for (PersistentEffect effect : effects) {
                if (!effect.isExpired()) {
                    applyEffect(player, effect);
                }
            }
        }
    }

    /**
     * Apply effects when player joins
     */
    public static void applyEffectsOnJoin(Player player) {
        List<PersistentEffect> effects = activeEffects.get(player.getUniqueId());
        if (effects == null || effects.isEmpty()) return;
        
        for (PersistentEffect effect : effects) {
            if (!effect.isExpired()) {
                applyEffect(player, effect);
            }
        }
    }

    /**
     * Remove expired effects
     */
    private static void removeExpiredEffects() {
        boolean changed = false;
        
        for (UUID playerId : new ArrayList<>(activeEffects.keySet())) {
            List<PersistentEffect> effects = activeEffects.get(playerId);
            if (effects == null) continue;
            
            // Remove expired effects from online players BEFORE removing from list
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                for (PersistentEffect effect : new ArrayList<>(effects)) {
                    if (effect.isExpired()) {
                        player.removePotionEffect(effect.getEffectType());
                    }
                }
            }
            
            int beforeSize = effects.size();
            effects.removeIf(PersistentEffect::isExpired);
            
            if (effects.isEmpty()) {
                activeEffects.remove(playerId);
                changed = true;
            } else if (effects.size() != beforeSize) {
                changed = true;
            }
        }
        
        if (changed) {
            save();
        }
    }

    /**
     * Get all active effects for a player
     */
    public static List<PersistentEffect> getEffects(UUID playerId) {
        return activeEffects.getOrDefault(playerId, new ArrayList<>());
    }

    /**
     * Remove a specific effect from a player
     */
    public static void removeEffect(UUID playerId, PotionEffectType effectType) {
        List<PersistentEffect> effects = activeEffects.get(playerId);
        if (effects == null) return;
        
        effects.removeIf(e -> e.getEffectType().equals(effectType));
        
        if (effects.isEmpty()) {
            activeEffects.remove(playerId);
        }
        
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.removePotionEffect(effectType);
        }
        
        save();
    }

    /**
     * Clear all effects from a player
     */
    public static void clearEffects(UUID playerId) {
        List<PersistentEffect> effects = activeEffects.remove(playerId);
        if (effects == null) return;
        
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            for (PersistentEffect effect : effects) {
                player.removePotionEffect(effect.getEffectType());
            }
        }
        
        save();
    }

    /**
     * Save all effects to file
     */
    public static void save() {
        // Clear the config
        for (String key : effectsData.getBackingDocument().getRoutesAsStrings(false)) {
            effectsData.set(key, null);
        }
        
        // Save all active effects
        for (Map.Entry<UUID, List<PersistentEffect>> entry : activeEffects.entrySet()) {
            String uuidStr = entry.getKey().toString();
            List<PersistentEffect> effects = entry.getValue();
            
            int index = 0;
            for (PersistentEffect effect : effects) {
                if (effect.isExpired()) continue;
                
                String path = uuidStr + ".effect_" + index;
                effectsData.set(path + ".type", effect.getEffectType().getName());
                effectsData.set(path + ".amplifier", effect.getAmplifier());
                effectsData.set(path + ".expiration", effect.getExpirationTime());
                index++;
            }
        }
        
        effectsData.save();
    }

    public static void shutdown() {
        save();
    }
}
