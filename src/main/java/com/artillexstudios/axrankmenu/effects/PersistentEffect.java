package com.artillexstudios.axrankmenu.effects;

import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PersistentEffect {
    private final UUID playerId;
    private final PotionEffectType effectType;
    private final int amplifier;
    private final long expirationTime; // Unix timestamp in milliseconds

    public PersistentEffect(UUID playerId, PotionEffectType effectType, int amplifier, long expirationTime) {
        this.playerId = playerId;
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.expirationTime = expirationTime;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public PotionEffectType getEffectType() {
        return effectType;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    public long getRemainingTime() {
        return Math.max(0, expirationTime - System.currentTimeMillis());
    }

    public long getRemainingDays() {
        return getRemainingTime() / (1000 * 60 * 60 * 24);
    }

    public long getRemainingHours() {
        return getRemainingTime() / (1000 * 60 * 60);
    }

    public String getEffectName() {
        return effectType.getName();
    }

    @Override
    public String toString() {
        return "PersistentEffect{" +
                "playerId=" + playerId +
                ", effectType=" + effectType.getName() +
                ", amplifier=" + amplifier +
                ", expirationTime=" + expirationTime +
                ", expired=" + isExpired() +
                '}';
    }
}
