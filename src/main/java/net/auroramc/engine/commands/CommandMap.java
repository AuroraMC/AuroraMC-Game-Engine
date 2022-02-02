/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.server.ServerState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandMap extends Command {


    public CommandMap() {
        super("map", Collections.singletonList("whatmap"), Collections.singletonList(Permission.PLAYER), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String s, List<String> list) {
        if (EngineAPI.getServerState() == ServerState.ENDING || EngineAPI.getServerState() == ServerState.IN_GAME) {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "The current map is: **" + EngineAPI.getActiveMap().getName() + "** by **" + EngineAPI.getActiveMap().getAuthor() + "**."));
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "The current map is: **" + EngineAPI.getWaitingLobbyMap().getName() + "** by **" + EngineAPI.getWaitingLobbyMap().getAuthor() + "**."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
