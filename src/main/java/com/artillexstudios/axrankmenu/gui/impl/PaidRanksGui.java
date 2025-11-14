package com.artillexstudios.axrankmenu.gui.impl;

import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.gui.GuiFrame;
import com.artillexstudios.axrankmenu.utils.ItemBuilderUtil;
import com.artillexstudios.axrankmenu.utils.PlaceholderUtils;
import dev.triumphteam.gui.components.GuiType;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import static com.artillexstudios.axrankmenu.AxRankMenu.CONFIG;
import static com.artillexstudios.axrankmenu.AxRankMenu.LANG;
import static com.artillexstudios.axrankmenu.AxRankMenu.RANKS;

public class PaidRanksGui extends GuiFrame {
    private static final Set<PaidRanksGui> openMenus = Collections.newSetFromMap(new WeakHashMap<>());
    private static final LuckPerms luckPerms = LuckPermsProvider.get();

    public PaidRanksGui(@NotNull Player player) {
        super(RANKS, player);
        
        this.gui = Gui.gui(GuiType.valueOf(RANKS.getString("paid-ranks.type", "CHEST")))
                .disableAllInteractions()
                .title(StringUtils.format(RANKS.getString("paid-ranks.title", "&0&lPaid Ranks (Discord Only)")))
                .rows(RANKS.getInt("paid-ranks.rows", 3))
                .create();
        
        setGui(gui);
    }

    public void open() {
        // Add decorative items from paid-ranks.items section
        for (String str : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
            if (str.startsWith("paid-ranks.items.")) {
                String remainder = str.substring("paid-ranks.items.".length());
                if (!remainder.contains(".")) {
                    createItem(str);
                }
            }
        }

        // Add back button if configured
        if (RANKS.getSection("paid-ranks.back-button") != null) {
            ItemStack backItem = createBackButton();
            if (backItem != null) {
                GuiItem guiItem = new GuiItem(backItem, event -> {
                    new MainMenuGui(player).open();
                });
                
                int slot = RANKS.getInt("paid-ranks.back-button.slot", 0);
                gui.setItem(slot, guiItem);
            }
        }

        // Add all paid ranks (those with paid-only: true)
        for (String route : RANKS.getBackingDocument().getRoutesAsStrings(false)) {
            if (route.startsWith("purchasable-ranks.")) continue;
            if (route.startsWith("paid-ranks.")) continue;
            if (route.startsWith("main-menu.")) continue;
            
            if (RANKS.getString(route + ".rank", null) == null) continue;
            if (!RANKS.getBoolean(route + ".paid-only", false)) continue;

            Section section = RANKS.getSection(route);
            Group group = luckPerms.getGroupManager().getGroup(section.getString("rank"));
            
            if (group == null) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxRankMenu] The group %group% does not exist!".replace("%group%", RANKS.getString(route + ".rank", "---"))));
                continue;
            }

            GuiItem item = createPaidRankItem(section);
            super.addItem(item, route);
        }

        if (openMenus.contains(this)) {
            gui.update();
            return;
        }
        openMenus.add(this);

        gui.open(player);
    }

    private GuiItem createPaidRankItem(Section section) {
        final List<String> lore = new ArrayList<>();
        for (String line : section.getStringList("item.lore")) {
            lore.add(PlaceholderUtils.parsePlaceholders(player, line, section));
        }

        final ItemStack it = ItemBuilderUtil.newBuilder(section.getSection("item"), player).setLore(lore).get();

        return new GuiItem(it, event -> {
            // Get Discord link from config
            String discordLink = CONFIG.getString("paid-ranks.discord-link", "https://discord.gg/yourserver");
            String purchaseMessage = LANG.getString("paid-ranks.purchase-message", 
                "&#FFD700This rank can only be purchased through Discord! &#FFFFFFVisit: &#00AAFF%link%");
            
            purchaseMessage = purchaseMessage.replace("%link%", discordLink);
            purchaseMessage = purchaseMessage.replace("%rank%", section.getString("rank", "Unknown"));
            
            player.closeInventory();
            player.sendMessage(StringUtils.formatToString(purchaseMessage));
            
            // Send clickable link if available
            if (CONFIG.getBoolean("paid-ranks.send-clickable-link", true)) {
                player.sendMessage(StringUtils.formatToString("&#00FF00Click here: &#00AAFF" + discordLink));
            }
        });
    }

    private ItemStack createBackButton() {
        if (RANKS.getSection("paid-ranks.back-button.item") != null) {
            return com.artillexstudios.axrankmenu.utils.ItemBuilderUtil.newBuilder(
                    RANKS.getSection("paid-ranks.back-button.item"), player
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

    public static Set<PaidRanksGui> getOpenMenus() {
        return openMenus;
    }
}
