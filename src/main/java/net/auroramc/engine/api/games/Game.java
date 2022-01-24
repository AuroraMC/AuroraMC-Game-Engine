/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.backend.communication.CommunicationUtils;
import net.auroramc.core.api.backend.communication.Protocol;
import net.auroramc.core.api.backend.communication.ProtocolMessage;
import net.auroramc.core.api.cosmetics.Cosmetic;
import net.auroramc.core.api.cosmetics.WinEffect;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.players.PlayerScoreboard;
import net.auroramc.core.api.players.Team;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.GameUtils;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Game {

    protected GameVariation gameVariation;
    protected GameMap map;
    protected Map<String, Team> teams;


    public Game(GameVariation gameVariation) {
        this.gameVariation = gameVariation;
        this.teams = new HashMap<>();
    }

    public abstract void preLoad();

    public abstract void load(GameMap map);

    /**
     * When executed by the Game Engine, this indicates that the Engine is handing over control to the game and that the game is now started.
     */
    public abstract void start();

    /**
     * When executed by the game, it should indicate that the game is handing control back to the Game Engine and the game is no longer in progress.
     */
    public void end(AuroraMCPlayer winner) {
        StringBuilder winnerString = new StringBuilder();
        winnerString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");
        winnerString.append(" \n");
        winnerString.append(" §b§l");
        winnerString.append((winner == null)?"Nobody":winner.getPlayer().getName());
        winnerString.append(" won the game!");
        winnerString.append("\n \n");
        winnerString.append("§r§lMap: §b§l");
        winnerString.append(map.getName());
        winnerString.append(" by ");
        winnerString.append(map.getAuthor());
        winnerString.append("\n");
        winnerString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");

        for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
            player.getPlayer().sendMessage(winnerString.toString());
            player.sendTitle((winner == null)?"Nobody":winner.getPlayer().getName() + " won the game!", "", 10, 100, 10, ChatColor.AQUA, ChatColor.AQUA, true, false);
        }

        if (winner != null) {
            Cosmetic cosmetic = winner.getActiveCosmetics().get(Cosmetic.CosmeticType.WIN_EFFECT);
            if (cosmetic != null) {
                WinEffect winEffect = (WinEffect) cosmetic;
                winEffect.onWin(winner);
            }
        }
        startEndRunnable();
    }

    /**
     * When executed by the game, it should indicate that the game is handing control back to the Game Engine and the game is no longer in progress.
     */
    public void end(Team winner, String winnerName) {
        StringBuilder winnerString = new StringBuilder();
        winnerString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");
        winnerString.append(" \n");
        winnerString.append(" §");
        winnerString.append(winner.getTeamColor());
        winnerString.append("§l");
        winnerString.append((winnerName != null)?winnerName:winner.getName());
        winnerString.append(" won the game!");
        winnerString.append("\n§rPlayers: §b");
        List<String> winners = new ArrayList<>();
        for (AuroraMCPlayer player : winner.getPlayers()) {
            winners.add(player.getPlayer().getName());
        }
        winnerString.append(String.join("§r, §b", winners));
        winnerString.append("\n \n");
        winnerString.append("§r§lMap: §b§l");
        winnerString.append(map.getName());
        winnerString.append(" by ");
        winnerString.append(map.getAuthor());
        winnerString.append("\n");
        winnerString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");

        for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
            player.getPlayer().sendMessage(winnerString.toString());
            player.sendTitle(winner.getName() + " won the game!", "", 10, 100, 10, ChatColor.getByChar(winner.getTeamColor()), ChatColor.AQUA, true, false);
        }

        for (AuroraMCPlayer amcPlayer : winner.getPlayers()) {
            AuroraMCGamePlayer player = (AuroraMCGamePlayer) amcPlayer;
            if (!player.isSpectator()) {
                Cosmetic cosmetic = player.getActiveCosmetics().get(Cosmetic.CosmeticType.WIN_EFFECT);
                if (cosmetic != null) {
                    WinEffect winEffect = (WinEffect) cosmetic;
                    winEffect.onWin(player);
                }
            }
        }

        startEndRunnable();
    }

    private void startEndRunnable() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (EngineAPI.isAwaitingRestart()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Server Manager", "This server is restarting for an update. You are being sent to a lobby."));
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Lobby");
                        out.writeUTF(player.getUniqueId().toString());
                        player.sendPluginMessage(AuroraMCAPI.getCore(), "BungeeCord", out.toByteArray());
                    }
                    //Wait 10 seconds, then close the server
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.kickPlayer(AuroraMCAPI.getFormatter().pluginMessage("Server Manager", "This server is restarting.\n\nYou can reconnect to the network to continue playing!"));
                            }
                            CommunicationUtils.sendMessage(new ProtocolMessage(Protocol.CONFIRM_SHUTDOWN, "Mission Control", EngineAPI.getRestartType(), AuroraMCAPI.getServerInfo().getName(), AuroraMCAPI.getServerInfo().getNetwork().name()));
                            CommunicationUtils.shutdown();
                        }
                    }.runTaskLater(AuroraMCAPI.getCore(), 200);
                    return;
                }

                for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                    JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("players");
                    int x, y, z;
                    x = spawnLocations.getJSONObject(0).getInt("x");
                    y = spawnLocations.getJSONObject(0).getInt("y");
                    z = spawnLocations.getJSONObject(0).getInt("z");
                    float yaw = spawnLocations.getJSONObject(0).getFloat("yaw");
                    pl.getPlayer().teleport(new Location(Bukkit.getWorld("world"), x, y, z, yaw, 0));
                    pl.getPlayer().setFallDistance(0);
                    pl.getPlayer().setVelocity(new Vector());

                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
                    player.setKit(null);
                    player.setTeam(null);
                    PlayerScoreboard scoreboard = player.getScoreboard();
                    scoreboard.setTitle("&3&l-= &b&l" + EngineAPI.getServerState().getName().toUpperCase() + "&r &3&l=-");
                    scoreboard.setLine(11, "&b&l«GAME»");
                    scoreboard.setLine(10, ((EngineAPI.getActiveGameInfo() != null)?EngineAPI.getActiveGameInfo().getName():"None   "));
                    scoreboard.setLine(9, " ");
                    scoreboard.setLine(8, "&b&l«MAP»");
                    scoreboard.setLine(7, ((EngineAPI.getActiveMap() != null)?EngineAPI.getActiveMap().getName():"None  "));
                    scoreboard.setLine(6, "  ");
                    scoreboard.setLine(5, "&b&l«KIT»");
                    scoreboard.setLine(4, ((player.getKit() != null)?player.getKit().getName():"None "));
                    scoreboard.setLine(3, "   ");
                    scoreboard.setLine(2, "&b&l«SERVER»");
                    scoreboard.setLine(1, AuroraMCAPI.getServerInfo().getName());

                    if (!player.isVanished() && EngineAPI.getServerState() != ServerState.STARTING && EngineAPI.getActiveGame() != null) {
                        if (AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished()).count() >= AuroraMCAPI.getServerInfo().getServerType().getInt("min_players")) {
                            EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30));
                            EngineAPI.getGameStartingRunnable().runTaskTimer(AuroraMCAPI.getCore(), 0, 20);
                        }
                    }

                    player.getPlayer().getInventory().setItem(8, EngineAPI.getLobbyItem().getItem());
                    player.getPlayer().getInventory().setItem(7, EngineAPI.getPrefsItem().getItem());
                    player.getPlayer().getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItem());
                }



                if (EngineAPI.getNextGame() != null) {
                    GameUtils.loadGame(EngineAPI.getNextGame(), EngineAPI.getNextMap(), EngineAPI.getNextVariation());
                } else if (EngineAPI.getGameRotation().size() > 0) {
                    GameUtils.loadNextGame();
                } else {
                    EngineAPI.setServerState(ServerState.IDLE);
                }

                for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
                    if (EngineAPI.getActiveGame() != null) {
                        player.getPlayer().getInventory().setItem(0, EngineAPI.getKitItem().getItem());
                        if (EngineAPI.getActiveGame().getTeams().size() > 1) {
                            player.getPlayer().getInventory().setItem(0, EngineAPI.getTeamItem().getItem());
                        }
                    }
                }
            }
        }.runTaskLater(AuroraMCAPI.getCore(), 200);
    }

    public abstract void onPlayerJoin(Player player);

    public abstract void onPlayerJoin(AuroraMCGamePlayer player);

    public abstract List<Kit> getKits();

    public Map<String, Team> getTeams() {
        return teams;
    }
}
