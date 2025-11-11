package com.artillexstudios.axrankmenu;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.metrics.AxMetrics;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axrankmenu.commands.Commands;
import com.artillexstudios.axrankmenu.effects.PersistentEffectManager;
// import com.artillexstudios.axrankmenu.grindable.GrindableRankManager; // Disabled: ranks now require purchase
import com.artillexstudios.axrankmenu.gui.GuiUpdater;
import com.artillexstudios.axrankmenu.hooks.HookManager;
import com.artillexstudios.axrankmenu.items.PersistentItemRewardManager;
import com.artillexstudios.axrankmenu.listeners.PlayerListener;
import com.artillexstudios.axrankmenu.utils.PlaytimeTracker;
import com.artillexstudios.axrankmenu.utils.PurchaseTracker;
import com.artillexstudios.axrankmenu.utils.UpdateNotifier;
import org.bstats.bukkit.Metrics;

import java.io.File;

public final class AxRankMenu extends AxPlugin {
    public static Config CONFIG;
    public static Config LANG;
    public static Config RANKS;
    public static MessageUtils MESSAGEUTILS;
    private static AxPlugin instance;
    private static AxMetrics metrics;

    public static AxPlugin getInstance() {
        return instance;
    }

    public void enable() {
        instance = this;

        new Metrics(this, 20079);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        LANG = new Config(new File(getDataFolder(), "lang.yml"), getResource("lang.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.builder().setScalarStyle(ScalarStyle.DOUBLE_QUOTED).build(), UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        RANKS = new Config(new File(getDataFolder(), "ranks.yml"), getResource("ranks.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setKeepAll(true).setVersioning(new BasicVersioning("version")).build());
        if (CONFIG.getSection("menu") != null) {
            new ConfigMigrator();
        }

        MESSAGEUTILS = new MessageUtils(LANG.getBackingDocument(), "prefix", CONFIG.getBackingDocument());

        Commands.registerCommand();

        Scheduler.get().run(task -> new HookManager().updateHooks());
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        // Initialize new systems
        PlaytimeTracker.init();
        PurchaseTracker.init();
        
        if (CONFIG.getBoolean("persistent-effects.enabled", true)) {
            PersistentEffectManager.init();
        }
        
        if (CONFIG.getBoolean("persistent-item-rewards.enabled", true)) {
            PersistentItemRewardManager.init();
        }
        
        // Grindable ranks now require playtime + purchase (no automatic granting)
        // if (CONFIG.getBoolean("grindable-ranks.enabled", true)) {
        //     GrindableRankManager.init();
        // }

        GuiUpdater.start();

        metrics = new AxMetrics(this, 16);
        metrics.start();

        // Update notifier disabled
        // if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 5071);
    }

    public void disable() {
        if (metrics != null) metrics.cancel();
        GuiUpdater.stop();
        
        // Shutdown new systems
        PlaytimeTracker.shutdown();
        PurchaseTracker.shutdown();
        
        if (CONFIG.getBoolean("persistent-effects.enabled", true)) {
            PersistentEffectManager.shutdown();
        }
        
        if (CONFIG.getBoolean("persistent-item-rewards.enabled", true)) {
            PersistentItemRewardManager.shutdown();
        }
    }

    public void updateFlags() {
        FeatureFlags.USE_LEGACY_HEX_FORMATTER.set(true);
    }
}
