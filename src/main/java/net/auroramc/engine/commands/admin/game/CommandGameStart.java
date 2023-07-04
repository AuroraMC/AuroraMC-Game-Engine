/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.commands.admin.game;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGameStart extends ServerCommand {


    public CommandGameStart() {
        super("start", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String aliasUsed, List<String> args) {
        if (EngineAPI.getActiveGame() != null && EngineAPI.getServerState() != ServerState.IDLE && EngineAPI.getServerState() != ServerState.ENDING  && EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished()).count() <= 1) {
                player.sendMessage(TextFormatter.pluginMessage("Game Manager","You cannot start the game with 1 or less players ready to start."));
                return;
            }
            if (args.size() >= 1) {
                int i;
                try {
                    i = Integer.parseInt(args.get(0));
                } catch (NumberFormatException e) {
                    player.sendMessage(TextFormatter.pluginMessage("Game Manager","Invalid syntax. Correct syntax: **/game start [time]**"));
                    return;
                }
                if (i > 9999) {
                    player.sendMessage(TextFormatter.pluginMessage("Game Manager","You cannot start the game with a time of more than 9999 seconds."));
                    return;
                }
                if (EngineAPI.getServerState() == ServerState.STARTING || EngineAPI.getGameStartingRunnable() != null) {
                    EngineAPI.getGameStartingRunnable().setStartTime(i);
                } else {
                    EngineAPI.setServerState(ServerState.STARTING);
                    EngineAPI.setGameStartingRunnable(new GameStartingRunnable(i, true));
                    EngineAPI.getGameStartingRunnable().runTaskTimer(ServerAPI.getCore(), 0, 20);
                }
            } else {
                if (EngineAPI.getServerState() == ServerState.STARTING || EngineAPI.getGameStartingRunnable() != null) {
                    EngineAPI.getGameStartingRunnable().setStartTime(EngineAPI.getGameStartingRunnable().getStartTime() - 2);
                } else {
                    EngineAPI.setServerState(ServerState.STARTING);
                    EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30, true));
                    EngineAPI.getGameStartingRunnable().runTaskTimer(ServerAPI.getCore(), 0, 20);
                }
            }
            BaseComponent message = TextFormatter.pluginMessage("Game Manager", "The game has been started by an admin.");
            for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
                player1.sendMessage(message);

            }
        } else {
            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You cannot start a game at this time."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
