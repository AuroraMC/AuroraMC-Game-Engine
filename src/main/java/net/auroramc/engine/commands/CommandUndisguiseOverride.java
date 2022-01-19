package net.auroramc.engine.commands;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.commands.admin.CommandDisguise;
import net.auroramc.core.commands.admin.CommandUndisguise;

import java.util.List;

public class CommandUndisguiseOverride extends CommandUndisguise {

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Disguise", "Disguise can only be used in Lobby servers!"));
    }
}
