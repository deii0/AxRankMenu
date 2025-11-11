package com.artillexstudios.axrankmenu.listeners;

import com.artillexstudios.axrankmenu.effects.PersistentEffectManager;
import com.artillexstudios.axrankmenu.grindable.GrindableRankManager;
import com.artillexstudios.axrankmenu.items.PersistentItemRewardManager;
import com.artillexstudios.axrankmenu.utils.PlaytimeTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import static com.artillexstudios.axrankmenu.AxRankMenu.CONFIG;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Start tracking playtime
        PlaytimeTracker.startTracking(player);
        
        // Apply persistent effects if enabled
        if (CONFIG.getBoolean("persistent-effects.enabled", true) && 
            CONFIG.getBoolean("persistent-effects.apply-on-join", true)) {
            PersistentEffectManager.applyEffectsOnJoin(player);
        }
        
        // Give pending daily item rewards if enabled
        if (CONFIG.getBoolean("persistent-item-rewards.enabled", true)) {
            PersistentItemRewardManager.onPlayerJoin(player.getUniqueId());
        }
        
        // Check for grindable ranks if enabled
        if (CONFIG.getBoolean("grindable-ranks.enabled", true)) {
            GrindableRankManager.checkPlayerRanks(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Stop tracking playtime and save
        PlaytimeTracker.stopTracking(player);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Reapply persistent effects after death/respawn
        if (CONFIG.getBoolean("persistent-effects.enabled", true)) {
            // Delay by 1 tick to ensure player is fully respawned
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                com.artillexstudios.axrankmenu.AxRankMenu.getInstance(),
                () -> PersistentEffectManager.applyEffectsOnJoin(player),
                1L
            );
        }
    }
}
