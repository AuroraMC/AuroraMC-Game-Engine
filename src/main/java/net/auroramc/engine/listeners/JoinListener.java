/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.events.player.PlayerObjectCreationEvent;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.players.PlayerScoreboard;
import net.auroramc.core.api.players.Team;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONArray;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        LobbyListener.updateHeaderFooter((CraftPlayer) e.getPlayer());
        e.getPlayer().setFlying(false);
        e.getPlayer().setAllowFlight(false);
        e.getPlayer().setGameMode(GameMode.SURVIVAL);
        e.getPlayer().setHealth(20);
        e.getPlayer().setFoodLevel(30);
        e.getPlayer().getInventory().clear();
        if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
            JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("PLAYERS");
            if (spawnLocations == null || spawnLocations.length() == 0) {
                EngineAPI.getGameEngine().getLogger().info("An invalid waiting lobby was supplied, assuming 0, 64, 0 spawn position.");
                e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), 0, 64, 0));
            } else {
                int x, y, z;
                x = spawnLocations.getJSONObject(0).getInt("x");
                y = spawnLocations.getJSONObject(0).getInt("y");
                z = spawnLocations.getJSONObject(0).getInt("z");
                float yaw = spawnLocations.getJSONObject(0).getFloat("yaw");
                e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), x, y, z, yaw, 0));
            }
        } else if (EngineAPI.getActiveGame() != null) {
            EngineAPI.getActiveGame().onPlayerJoin(e.getPlayer());
        }
    }

    @EventHandler
    public void onObjectCreate(PlayerObjectCreationEvent e) {
        AuroraMCGamePlayer player = new AuroraMCGamePlayer(e.getPlayer());
        e.setPlayer(player);
        if ((EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) && EngineAPI.getActiveGame() != null) {
            EngineAPI.getActiveGame().onPlayerJoin(player);
        } else {
            PlayerScoreboard scoreboard = player.getScoreboard();
            scoreboard.setTitle("&3&l-= &b&l" + EngineAPI.getServerState().getName().toUpperCase() + "&r &3&l=-");
            scoreboard.setLine(13, "&b&l«GAME»");
            scoreboard.setLine(12, ((EngineAPI.getActiveGameInfo() != null)?EngineAPI.getActiveGameInfo().getName():"None   "));
            scoreboard.setLine(11, " ");
            scoreboard.setLine(10, "&b&l«MAP»");
            scoreboard.setLine(9, ((EngineAPI.getActiveMap() != null)?EngineAPI.getActiveMap().getName():"None  "));
            scoreboard.setLine(8, "  ");
            scoreboard.setLine(7, "&b&l«KIT»");
            scoreboard.setLine(6, ((player.getKit() != null)?player.getKit().getName():"None "));
            scoreboard.setLine(5, "   ");
            scoreboard.setLine(4, "&b&l«SERVER»");
            scoreboard.setLine(3, AuroraMCAPI.getServerInfo().getName());
            scoreboard.setLine(2, "    ");
            scoreboard.setLine(1, "&7You are playing on auroramc.net");

            if (!player.isVanished() && EngineAPI.getServerState() != ServerState.STARTING && EngineAPI.getActiveGame() != null) {
                if (AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished()).count() >= AuroraMCAPI.getServerInfo().getServerType().getInt("min_players")) {
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
                    EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30));
                    EngineAPI.getGameStartingRunnable().runTaskTimer(AuroraMCAPI.getCore(), 0, 20);
                }
            }

            if (!player.isVanished() && EngineAPI.getServerState() == ServerState.STARTING) {
                if (!player.isSpectator()) {
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
                        leastPlayers.getPlayers().add(player);
                        player.setTeam(leastPlayers);
                        for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                            pl.updateNametag(player);
                        }
                    }
                    if (player.getKit() == null) {
                        player.setKit(EngineAPI.getActiveGame().getKits().get(0));
                    }
                }
            }

            player.getPlayer().getInventory().setItem(8, EngineAPI.getLobbyItem().getItem());
            player.getPlayer().getInventory().setItem(7, EngineAPI.getPrefsItem().getItem());
            player.getPlayer().getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItem());

            if (EngineAPI.getActiveGame() != null) {
                if (EngineAPI.getActiveGame().getKits().size() > 1) {
                    player.getPlayer().getInventory().setItem(0, EngineAPI.getKitItem().getItem());
                }
                if (EngineAPI.getActiveGame().getTeams().size() > 1 && !EngineAPI.getActiveGameInfo().hasTeamCommand()) {
                    player.getPlayer().getInventory().setItem(1, EngineAPI.getTeamItem().getItem());
                }
            }
        }
    }

}
