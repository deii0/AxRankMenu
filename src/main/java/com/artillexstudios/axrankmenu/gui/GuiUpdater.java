package com.artillexstudios.axrankmenu.gui;

import com.artillexstudios.axrankmenu.gui.impl.MainMenuGui;
import com.artillexstudios.axrankmenu.gui.impl.PurchasableRanksGui;
import com.artillexstudios.axrankmenu.gui.impl.PaidRanksGui;
import com.artillexstudios.axrankmenu.gui.impl.RankGui;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuiUpdater {
    private static ScheduledExecutorService service = null;

    public static void start() {
        if (service != null) service.shutdown();

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                for (RankGui gui : RankGui.getOpenMenus()) {
                    gui.open();
                }
                for (MainMenuGui gui : MainMenuGui.getOpenMenus()) {
                    gui.open();
                }
                for (PurchasableRanksGui gui : PurchasableRanksGui.getOpenMenus()) {
                    gui.open();
                }
                for (PaidRanksGui gui : PaidRanksGui.getOpenMenus()) {
                    gui.open();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (service == null) return;
        service.shutdown();
    }
}
