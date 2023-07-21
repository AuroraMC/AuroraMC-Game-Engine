/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.commands;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.backend.info.ServerInfo;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
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
                    player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You will no longer be a spectator in the next game."));
                } else if (EngineAPI.getServerState() == ServerState.IN_GAME && EngineAPI.getActiveGame().shouldSpawnWhenUnspectate() && EngineAPI.getActiveGame().getTeams().size() == 1) {
                    ((AuroraMCGamePlayer) player).setSpectator(false, false);
                    EngineAPI.getActiveGame().onPlayerJoin((AuroraMCGamePlayer) player);
                    player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You are no longer be a spectator. You have been spawned into the game."));
                } else {
                    player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You will no longer be a spectator in the next game."));
                }

                if (EngineAPI.getServerState() == ServerState.WAITING_FOR_PLAYERS) {
                    if (ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && ((AuroraMCGamePlayer)player1).isOptedSpec()).count() >= ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("min_players")) {
                        EngineAPI.setServerState(ServerState.STARTING);
                        EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30, false));
                        EngineAPI.getGameStartingRunnable().runTaskTimer(ServerAPI.getCore(), 0, 20);
                        int kitId = EngineDatabaseManager.getDefaultKit(player.getId(), EngineAPI.getActiveGameInfo().getId());
                        for (Kit kit : EngineAPI.getActiveGame().getKits()) {
                            if (kitId == kit.getId()) {
                                ((AuroraMCGamePlayer)player).setKit(kit);
                                break;
                            }
                        }
                        if (((AuroraMCGamePlayer)player).getKit() == null) {
                            ((AuroraMCGamePlayer)player).setKit(EngineAPI.getActiveGame().getKits().get(0));
                        }
                    }
                }
            } else {
                if (EngineAPI.getServerState() == ServerState.STARTING) {
                    if (EngineAPI.getGameStartingRunnable().getStartTime() <= 5) {
                        player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You cannot toggle spectator at this time."));
                    }
                }
                ((AuroraMCGamePlayer) player).setOptedSpec(true);
                if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
                    ((AuroraMCGamePlayer) player).setSpectator(true, false);
                }
                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You will now be a spectator in the next game."));
                if (EngineAPI.getServerState() == ServerState.STARTING) {
                    if (EngineAPI.getGameStartingRunnable().getStartTime() > 5) {
                        if (ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && !((AuroraMCGamePlayer)player1).isOptedSpec()).count() < ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("min_players")) {
                            if (EngineAPI.getGameStartingRunnable() != null) {
                                EngineAPI.getGameStartingRunnable().cancel();
                                EngineAPI.setGameStartingRunnable(null);
                            }
                            EngineAPI.setServerState(ServerState.WAITING_FOR_PLAYERS);
                            for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
                                pl.sendMessage(TextFormatter.pluginMessage("Server Manager", "A player has become a spectator so there are no longer enough players to start the game!"));
                            }
                        }
                    }
                }
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
