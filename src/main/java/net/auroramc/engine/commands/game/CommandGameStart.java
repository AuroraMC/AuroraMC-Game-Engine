/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.game;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.players.Team;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGameStart extends Command {


    public CommandGameStart() {
        super("start", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (EngineAPI.getActiveGame() != null && EngineAPI.getServerState() != ServerState.IDLE && EngineAPI.getServerState() != ServerState.ENDING  && EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished()).count() <= 1) {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager","You cannot start the game with 1 or less players ready to start."));
                return;
            }
            if (args.size() >= 1) {
                int i;
                try {
                    i = Integer.parseInt(args.get(0));
                } catch (NumberFormatException e) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager","Invalid syntax. Correct syntax: **/game start [time]**"));
                    return;
                }
                if (i > 9999) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager","You cannot start the game with a time of more than 9999 seconds."));
                    return;
                }
                if (EngineAPI.getServerState() == ServerState.STARTING || EngineAPI.getGameStartingRunnable() != null) {
                    EngineAPI.getGameStartingRunnable().setStartTime(i);
                } else {
                    EngineAPI.setGameStartingRunnable(new GameStartingRunnable(i));
                    EngineAPI.getGameStartingRunnable().runTaskTimer(AuroraMCAPI.getCore(), 0, 20);
                }
            } else {
                if (EngineAPI.getServerState() == ServerState.STARTING || EngineAPI.getGameStartingRunnable() != null) {
                    EngineAPI.getGameStartingRunnable().setStartTime(EngineAPI.getGameStartingRunnable().getStartTime() - 2);
                } else {
                    EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30));
                    EngineAPI.getGameStartingRunnable().runTaskTimer(AuroraMCAPI.getCore(), 0, 20);
                }
            }
            String message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "The game has been started by an admin.");
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                player1.sendMessage(message);
            }
            for (AuroraMCPlayer player1 : AuroraMCAPI.getPlayers()) {
                AuroraMCGamePlayer gp = (AuroraMCGamePlayer) player1;
                if (player1.getTeam() == null && !gp.isSpectator()) {
                    Team leastPlayers = null;
                    for (Team team : EngineAPI.getActiveGame().getTeams().values()) {
                        if (leastPlayers == null) {
                            leastPlayers = team;
                            continue;
                        }
                        if (leastPlayers.getPlayers().size() > team.getPlayers().size()) {
                            leastPlayers = team;
                        }
                    }
                    if (leastPlayers != null) {
                        leastPlayers.getPlayers().add(player1);
                        player1.setTeam(leastPlayers);
                        for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                            pl.updateNametag(player1);
                        }
                    }
                }

                if (gp.getKit() == null && !gp.isSpectator()) {
                    gp.setKit(EngineAPI.getActiveGame().getKits().get(0));
                }
            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You cannot start a game at this time."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
