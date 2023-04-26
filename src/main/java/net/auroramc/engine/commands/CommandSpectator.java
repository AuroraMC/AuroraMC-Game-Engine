/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandSpectator extends ServerCommand {


    public CommandSpectator() {
        super("spectator", Collections.singletonList("spec"), Collections.singletonList(Permission.PLAYER), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String aliasUsed, List<String> args) {
        if (!player.isVanished()) {
            if (((AuroraMCGamePlayer)player).isOptedSpec()) {
                ((AuroraMCGamePlayer) player).setOptedSpec(false);
                if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
                    ((AuroraMCGamePlayer) player).setSpectator(false, false);
                }
                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You will no longer be a spectator in the next game."));
            } else {
                ((AuroraMCGamePlayer) player).setOptedSpec(true);
                if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
                    ((AuroraMCGamePlayer) player).setSpectator(true, false);
                }
                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You will now be a spectator in the next game."));
            }
        } else {
            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You cannot toggle spectator mode while in vanish."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer player, String aliasUsed, List<String> args, String lastToken, int noOfTokens) {
        return new ArrayList<>();
    }
}
