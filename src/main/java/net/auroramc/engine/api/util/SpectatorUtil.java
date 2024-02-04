/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.util;

import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.EngineAPI;
import org.bukkit.Material;

public class SpectatorUtil {

    private final static GUIItem compassItem;

    static {
        compassItem = new GUIItem(Material.COMPASS, "&a&lPlayer Compass");
    }

    public static void giveItems(AuroraMCServerPlayer player) {
        player.getInventory().setItem(8, EngineAPI.getLobbyItem().getItemStack());
        player.getInventory().setItem(7, EngineAPI.getPrefsItem().getItemStack());
        player.getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItemStack());
        player.getInventory().setItem(0, compassItem.getItemStack());
    }

}
