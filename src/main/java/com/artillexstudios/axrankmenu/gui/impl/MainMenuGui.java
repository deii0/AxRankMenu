package com.artillexstudios.axrankmenu.gui.impl;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.gui.GuiFrame;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.artillexstudios.axrankmenu.AxRankMenu.CONFIG;

public class MainMenuGui extends GuiFrame {
    private static final Set<MainMenuGui> openMenus = Collections.newSetFromMap(new WeakHashMap<>());

    public MainMenuGui(@NotNull Player player) {
        super(CONFIG, player);
        
        this.gui = Gui.gui(GuiType.valueOf(CONFIG.getString("main-menu.type", "CHEST")))
                .disableAllInteractions()
                .title(StringUtils.format(CONFIG.getString("main-menu.title", "&8&lRank Menu")))
                .rows(CONFIG.getInt("main-menu.rows", 3))
                .create();
        
        setGui(gui);
    }

    public void open() {
        // Add decorative items from main-menu.items section
        for (String str : CONFIG.getBackingDocument().getRoutesAsStrings(false)) {
            if (str.startsWith("main-menu.items.")) {
                createItem(str);
            }
        }

        // Add purchasable ranks button
        if (CONFIG.getSection("main-menu.buttons.purchasable-ranks") != null) {
            ItemStack item = createItemStack("main-menu.buttons.purchasable-ranks");
            if (item != null) {
                GuiItem guiItem = new GuiItem(item, event -> {
                    new PurchasableRanksGui(player).open();
                });
                
                Object slotObj = CONFIG.get("main-menu.buttons.purchasable-ranks.slot");
                if (slotObj instanceof Integer) {
                    gui.setItem((Integer) slotObj, guiItem);
                } else if (slotObj instanceof java.util.List) {
                    for (Object slot : (java.util.List<?>) slotObj) {
                        if (slot instanceof Integer) {
                            gui.setItem((Integer) slot, guiItem);
                        }
                    }
                }
            }
        }

        // Add paid ranks button
        if (CONFIG.getSection("main-menu.buttons.paid-ranks") != null) {
            ItemStack item = createItemStack("main-menu.buttons.paid-ranks");
            if (item != null) {
                GuiItem guiItem = new GuiItem(item, event -> {
                    new PaidRanksGui(player).open();
                });
                
                Object slotObj = CONFIG.get("main-menu.buttons.paid-ranks.slot");
                if (slotObj instanceof Integer) {
                    gui.setItem((Integer) slotObj, guiItem);
                } else if (slotObj instanceof java.util.List) {
                    for (Object slot : (java.util.List<?>) slotObj) {
                        if (slot instanceof Integer) {
                            gui.setItem((Integer) slot, guiItem);
                        }
                    }
                }
            }
        }

        if (openMenus.contains(this)) {
            gui.update();
            return;
        }
        openMenus.add(this);

        gui.open(player);
    }

    private ItemStack createItemStack(String path) {
        if (CONFIG.getSection(path + ".item") != null) {
            return com.artillexstudios.axrankmenu.utils.ItemBuilderUtil.newBuilder(
                    CONFIG.getSection(path + ".item"), player
            ).get();
        }
        return null;
    }

    public static Set<MainMenuGui> getOpenMenus() {
        return openMenus;
    }
}
