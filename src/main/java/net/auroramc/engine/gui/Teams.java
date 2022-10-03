/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.gui;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.players.Team;
import net.auroramc.core.api.utils.gui.GUI;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class Teams extends GUI {

    private final AuroraMCGamePlayer player;

    public Teams(AuroraMCGamePlayer player) {
        super("&3&lSelect a team!", 2, true);
        border("&3&lSelect a team!","");

        this.player = player;

        switch (EngineAPI.getActiveGame().getTeams().size()) {
            case 3: {
                this.setItem(1, 4, new GUIItem(Material.STAINED_CLAY, "&a&lGreen Team", 1, ";&r&fClick here to join &aGreen&r&f team!", (short) 5, (player.getTeam().getName().equalsIgnoreCase("green"))));
            }
            case 2: {
                this.setItem(1, 2, new GUIItem(Material.STAINED_CLAY, "&c&lRed Team", 1, ";&r&fClick here to join &cRed&r&f team!", (short)14, (player.getTeam().getName().equalsIgnoreCase("red"))));
                this.setItem(1, 6, new GUIItem(Material.STAINED_CLAY, "&b&lBlue Team", 1, ";&r&fClick here to join &bBlue&r&f team!", (short)11, (player.getTeam().getName().equalsIgnoreCase("blue"))));
                break;
            }
            case 4: {
                this.setItem(1, 1, new GUIItem(Material.STAINED_CLAY, "&c&lRed Team", 1, ";&r&fClick here to join &cRed&r&f team!", (short)14, (player.getTeam().getName().equalsIgnoreCase("red"))));
                this.setItem(1, 3, new GUIItem(Material.STAINED_CLAY, "&a&lGreen Team", 1, ";&r&fClick here to join &aGreen&r&f team!", (short)5, (player.getTeam().getName().equalsIgnoreCase("green"))));
                this.setItem(1, 5, new GUIItem(Material.STAINED_CLAY, "&b&lBlue Team", 1, ";&r&fClick here to join &bBlue&r&f team!", (short)11, (player.getTeam().getName().equalsIgnoreCase("blue"))));
                this.setItem(1, 7, new GUIItem(Material.STAINED_CLAY, "&e&lYellow Team", 1, ";&r&fClick here to join &eYellow&r&f team!", (short)4, (player.getTeam().getName().equalsIgnoreCase("yellow"))));
                break;
            }
        }
    }

    @Override
    public void onClick(int column, int row, ItemStack item, ClickType clickType) {
        if (item.getType() == Material.STAINED_GLASS_PANE) {
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ITEM_BREAK, 100, 0);
            return;
        }
        String teamString = ChatColor.stripColor(item.getItemMeta().getDisplayName()).split(" ")[0];
        if (player.getTeam().getName().equalsIgnoreCase(teamString)) {
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ITEM_BREAK, 100, 0);
        } else {
            Team team = EngineAPI.getActiveGame().getTeams().get(teamString);
            player.setTeam(team);
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You joined team &" + team.getTeamColor() + team.getName() + "&r&f."));
            player.getPlayer().closeInventory();
            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                player.updateNametag(this.player);
            }
        }
    }
}
