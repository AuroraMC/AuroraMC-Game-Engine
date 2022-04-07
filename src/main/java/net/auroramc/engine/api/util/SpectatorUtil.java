/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.util;

import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.EngineAPI;
import org.bukkit.Material;

public class SpectatorUtil {

    private final static GUIItem compassItem;

    static {
        compassItem = new GUIItem(Material.COMPASS, "&a&lPlayer Compass");
    }

    public static void giveItems(AuroraMCPlayer player) {
        player.getPlayer().getInventory().setItem(8, EngineAPI.getLobbyItem().getItem());
        player.getPlayer().getInventory().setItem(7, EngineAPI.getPrefsItem().getItem());
        player.getPlayer().getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItem());
        player.getPlayer().getInventory().setItem(0, compassItem.getItem());
    }

}
