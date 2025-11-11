package com.artillexstudios.axrankmenu.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PersistentItemReward {
    private final UUID playerId;
    private final ItemStack itemStack;
    private final int durationDays;
    private final long startTime; // Unix timestamp when reward started
    private long lastClaimTime; // Unix timestamp of last daily claim

    public PersistentItemReward(UUID playerId, ItemStack itemStack, int durationDays, long startTime, long lastClaimTime) {
        this.playerId = playerId;
        this.itemStack = itemStack;
        this.durationDays = durationDays;
        this.startTime = startTime;
        this.lastClaimTime = lastClaimTime;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public int getDurationDays() {
        return durationDays;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastClaimTime() {
        return lastClaimTime;
    }

    public void setLastClaimTime(long time) {
        this.lastClaimTime = time;
    }

    public long getExpirationTime() {
        return startTime + (durationDays * 24L * 60L * 60L * 1000L);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= getExpirationTime();
    }

    public boolean canClaimToday() {
        long currentTime = System.currentTimeMillis();
        long daysSinceLastClaim = (currentTime - lastClaimTime) / (1000 * 60 * 60 * 24);
        return daysSinceLastClaim >= 1;
    }

    public long getRemainingDays() {
        long remainingTime = getExpirationTime() - System.currentTimeMillis();
        return Math.max(0, remainingTime / (1000 * 60 * 60 * 24));
    }

    public int getDaysClaimed() {
        long timeSinceStart = System.currentTimeMillis() - startTime;
        return (int) Math.min(durationDays, timeSinceStart / (1000 * 60 * 60 * 24));
    }

    public String getItemName() {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }
        return itemStack.getType().name();
    }

    @Override
    public String toString() {
        return "PersistentItemReward{" +
                "playerId=" + playerId +
                ", item=" + getItemName() +
                ", amount=" + itemStack.getAmount() +
                ", durationDays=" + durationDays +
                ", daysClaimed=" + getDaysClaimed() +
                ", expired=" + isExpired() +
                '}';
    }
}
