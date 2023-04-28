/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands;

import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.core.commands.admin.CommandDisguise;

import java.util.List;

public class CommandDisguiseOverride extends CommandDisguise {

    @Override
    public void execute(AuroraMCServerPlayer player, String aliasUsed, List<String> args) {
        player.sendMessage(TextFormatter.pluginMessage("Disguise", "Disguise can only be used in Lobby servers!"));
    }
}
