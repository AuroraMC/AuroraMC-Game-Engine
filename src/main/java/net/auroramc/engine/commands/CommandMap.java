/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.commands;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.server.ServerState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandMap extends ServerCommand {


    public CommandMap() {
        super("map", Collections.singletonList("whatmap"), Collections.singletonList(Permission.PLAYER), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String s, List<String> list) {
        if (EngineAPI.getServerState() == ServerState.ENDING || EngineAPI.getServerState() == ServerState.IN_GAME) {
            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "The current map is: **" + EngineAPI.getActiveMap().getName() + "** by **" + EngineAPI.getActiveMap().getAuthor() + "**."));
        } else {
            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "The current map is: **" + EngineAPI.getWaitingLobbyMap().getName() + "** by **" + EngineAPI.getWaitingLobbyMap().getAuthor() + "**."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
