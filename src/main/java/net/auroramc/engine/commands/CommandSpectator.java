/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandSpectator extends Command {


    public CommandSpectator() {
        super("spectator", Collections.singletonList("spec"), Collections.singletonList(Permission.PLAYER), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (!player.isVanished()) {
            if (((AuroraMCGamePlayer)player).isOptedSpec()) {
                ((AuroraMCGamePlayer) player).setOptedSpec(false);
                if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
                    ((AuroraMCGamePlayer) player).setSpectator(false, false);
                }
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You will not be a spectator in the next game."));
            } else {
                ((AuroraMCGamePlayer) player).setOptedSpec(true);
                if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
                    ((AuroraMCGamePlayer) player).setSpectator(true, false);
                }
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You will now be a spectator in the next game."));
            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You cannot un-spectate while in vanish."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer player, String aliasUsed, List<String> args, String lastToken, int noOfTokens) {
        return new ArrayList<>();
    }
}
