package com.artillexstudios.axrankmenu.gui.impl;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.gui.GuiFrame;
import com.artillexstudios.axrankmenu.rank.Rank;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static com.artillexstudios.axrankmenu.AxRankMenu.RANKS;

public class PurchasableRanksGui extends GuiFrame {
    private static final Set<PurchasableRanksGui> openMenus = Collections.newSetFromMap(new WeakHashMap<>());

    public PurchasableRanksGui(@NotNull Player player) {
        super(RANKS, player);
        
        this.gui = Gui.gui(GuiType.valueOf(RANKS.getString("purchasable-ranks.type", "CHEST")))
                .disableAllInteractions()
                .title(StringUtils.format(RANKS.getString("purchasable-ranks.title", "&0&lPurchasable Ranks")))
                .rows(RANKS.getInt("purchasable-ranks.rows", 3))
                .create();
        
        setGui(gui);
    }

    public void open() {
        // Add decorative items from purchasable-ranks.items section
        for (String str : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
            if (str.startsWith("purchasable-ranks.items.")) {
                String remainder = str.substring("purchasable-ranks.items.".length());
                if (!remainder.contains(".")) {
                    createItem(str);
                }
            }
        }

        // Add back button if configured
        if (RANKS.getSection("purchasable-ranks.back-button") != null) {
            ItemStack backItem = createBackButton();
            if (backItem != null) {
                GuiItem guiItem = new GuiItem(backItem, event -> {
                    new MainMenuGui(player).open();
                });
                
                int slot = RANKS.getInt("purchasable-ranks.back-button.slot", 0);
                gui.setItem(slot, guiItem);
            }
        }

        // Add all purchasable ranks (those without paid-only flag or with paid-only: false)
        for (String route : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
            if (route.startsWith("purchasable-ranks.")) continue;
            if (route.startsWith("paid-ranks.")) continue;
            
            if (RANKS.getString(route + ".rank", null) == null) continue;
            if (RANKS.getBoolean(route + ".paid-only", false)) continue;

            final Rank rank = new Rank(RANKS.getSection(route), player);
            if (rank.getGroup() == null) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] The group %group% does not exist!".replace("%group%", RANKS.getString(route + ".rank", "---"))));
                continue;
            }

            super.addItem(rank.getItem(), route);
        }

        if (openMenus.contains(this)) {
            gui.update();
            return;
        }
        openMenus.add(this);

        gui.open(player);
    }

    private ItemStack createBackButton() {
        if (RANKS.getSection("purchasable-ranks.back-button.item") != null) {
            return com.artillexstudios.axrankmenu.utils.ItemBuilderUtil.newBuilder(
                    RANKS.getSection("purchasable-ranks.back-button.item"), player
            ).get();
        }
        
        // Default back button
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(StringUtils.formatToString("&c&lBack"));
            meta.setLore(Arrays.asList(StringUtils.formatToString("&7Click to return to main menu")));
            item.setItemMeta(meta);
        }
        return item;
    }

    public static Set<PurchasableRanksGui> getOpenMenus() {
        return openMenus;
    }
}
