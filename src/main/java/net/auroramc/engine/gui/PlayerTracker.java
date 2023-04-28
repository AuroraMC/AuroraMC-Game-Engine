/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.gui;

import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.core.api.utils.gui.GUI;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class PlayerTracker extends GUI {

    private final AuroraMCServerPlayer player;

    public PlayerTracker(AuroraMCServerPlayer player) {
        super("&3&lPlayer Tracker", 5, true);
        this.player = player;
        border("&3&lPlayer Tracker", "");

        int column = 1;
        int row = 1;

        for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
            if (player1 instanceof AuroraMCGamePlayer) {
                if (!player1.isVanished() && !((AuroraMCGamePlayer) player1).isSpectator() && !player1.isDead()) {
                    this.setItem(row, column, new GUIItem(Material.SKULL_ITEM, "&3&l" + player1.getByDisguiseName(), 1, ";&r&fKit: **" + ((AuroraMCGamePlayer) player1).getKit().getName() + "**;;&aClick to teleport!", (short)3, false, player1.getByDisguiseName()));
                    column++;
                    if (column == 8) {
                        row++;
                        column = 1;
                        if (row == 5) {
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onClick(int row, int column, ItemStack itemStack, ClickType clickType) {
        if (itemStack.getType() == Material.SKULL_ITEM) {
            String name = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            AuroraMCServerPlayer pl = ServerAPI.getDisguisedPlayer(name);
            if (pl == null)  {
                pl = ServerAPI.getPlayer(name);
                if (pl == null) {
                    player.closeInventory();
                    return;
                }
            }

            player.closeInventory();
            player.teleport(pl.getLocation());
            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You are now spectating **" + pl.getByDisguiseName() + "**."));
        } else {
            player.playSound(player.getLocation(), Sound.ITEM_BREAK, 100, 0);
            return;
        }
    }
}
