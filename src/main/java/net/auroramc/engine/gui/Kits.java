/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.gui;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.utils.gui.GUI;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class Kits extends GUI {

    private final AuroraMCGamePlayer player;

    public Kits(AuroraMCGamePlayer player) {
        super("&3&lSelect a kit!", (EngineAPI.getActiveGame().getKits().size() / 7) + 2, true);
        border("&3&lSelect a kit!","");

        this.player = player;

        int column = 1;
        int row = 1;
        for (Kit kit : EngineAPI.getActiveGame().getKits()) {
            this.setItem(row, column, new GUIItem(kit.getMaterial(), "&3&l" + kit.getName(), 1, "&7" + WordUtils.wrap(kit.getDescription(), 40, ";&7", false) + ";;&r Click to equip the **" + kit.getName() + "** kit.", (short)0, ((AuroraMCGamePlayer)player).getKit().equals(kit)));
        }
    }

    @Override
    public void onClick(int column, int row, ItemStack item, ClickType clickType) {
        if (item.getType() == Material.STAINED_GLASS_PANE) {
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ITEM_BREAK, 100, 0);
            return;
        }
        Kit kit = EngineAPI.getActiveGame().getKits().get(((row - 1) * 7) + (column - 1));
        if (kit.equals(player.getKit())) {
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ITEM_BREAK, 100, 0);
        } else {
            player.setKit(kit);
            player.getPlayer().closeInventory();
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You set your kit to **" + kit.getName() + "**."));
            player.getScoreboard().setLine(4, player.getKit().getName() + " ");
        }
    }
}
