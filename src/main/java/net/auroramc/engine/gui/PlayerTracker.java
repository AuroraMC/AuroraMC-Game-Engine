/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.gui;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.utils.gui.GUI;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PlayerTracker extends GUI {

    private final AuroraMCPlayer player;

    public PlayerTracker(AuroraMCPlayer player) {
        super("&3&lPlayer Tracker", 5, true);
        this.player = player;
        border("&3&lPlayer Tracker", "");

        int column = 1;
        int row = 1;

        for (AuroraMCPlayer player1 : AuroraMCAPI.getPlayers()) {
            if (player1 instanceof AuroraMCGamePlayer) {
                if (!player1.isVanished() && !((AuroraMCGamePlayer) player1).isSpectator() && !player1.isDead()) {
                    this.setItem(row, column, new GUIItem(Material.SKULL_ITEM, "&3&l" + player1.getPlayer().getName(), 1, ";&rKit: **" + ((AuroraMCGamePlayer) player1).getKit().getName() + "**;;&aClick to teleport!", (short)3, false, player1.getPlayer().getName()));
                }
            }
        }
    }

    @Override
    public void onClick(int row, int column, ItemStack itemStack, ClickType clickType) {
        if (itemStack.getType() == Material.SKULL_ITEM) {
            String name = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            AuroraMCPlayer pl = AuroraMCAPI.getDisguisedPlayer(name);
            if (pl == null)  {
                pl = AuroraMCAPI.getPlayer(name);
                if (pl == null) {
                    player.getPlayer().closeInventory();
                    return;
                }
            }

            player.getPlayer().closeInventory();
            player.getPlayer().teleport(pl.getPlayer().getLocation());
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You are now spectating **" + player.getPlayer().getName() + "**."));
        } else {
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ITEM_BREAK, 100, 0);
            return;
        }
    }
}
