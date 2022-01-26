/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.gui;

import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.utils.gui.GUI;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class Teams extends GUI {


    public Teams(AuroraMCPlayer player) {
        super("&3&lSelect a team!", 5, true);
        border("&3&lSelect a team!","");
    }

    @Override
    public void onClick(int column, int row, ItemStack itemClicked, ClickType clickType) {

    }
}
