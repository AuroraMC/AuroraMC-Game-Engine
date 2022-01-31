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
import net.auroramc.engine.api.util.InGameStartingRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Game {

    protected GameVariation gameVariation;
    protected GameMap map;
    protected Map<String, Team> teams;
    protected List<Kit> kits;
    protected GameSession gameSession;
    protected boolean starting;
    protected InGameStartingRunnable runnable;


    public Game(GameVariation gameVariation) {
        this.gameVariation = gameVariation;
        this.teams = new HashMap<>();
        this.kits = new ArrayList<>();
        gameSession = new GameSession(EngineAPI.getActiveGameInfo().getRegistryKey(),gameVariation);
        starting = false;
    }

    public abstract void preLoad();

    public abstract void load(GameMap map);

    /**
     * When executed by the Game Engine, this indicates that the Engine is handing over control to the game and that the game is now started. Should be overridden and should execute super.
     */
    public void start() {
        starting = true;
        InGameStartingRunnable runnable = new InGameStartingRunnable(this);
        runnable.runTaskTimerAsynchronously(EngineAPI.getGameEngine(), 0, 20);
        gameSession.start();
    }

    public void inProgress() {
        starting = false;
        runnable = null;
    }

    /**
     * When executed by the game, it should indicate that the game is handing control back to the Game Engine and the game is no longer in progress. Should execute super.
     */
    public void end(AuroraMCPlayer winner) {
        EngineAPI.setServerState(ServerState.ENDING);
        if (runnable != null) {
            runnable.cancel();
        }
        gameSession.log(new GameSession.GameLogEntry(new JSONObject().put("event", "END").put("winner", ((winner == null)?"NONE":winner.getName()))));
        gameSession.end(false);
        StringBuilder winnerString = new StringBuilder();
        winnerString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");
        winnerString.append(" \n \n");
        winnerString.append(" §b§l");
        winnerString.append((winner == null)?"Nobody":winner.getPlayer().getName());
        winnerString.append(" won the game!");
        winnerString.append("\n \n \n");
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
        EngineAPI.setServerState(ServerState.ENDING);
        if (runnable != null) {
            runnable.cancel();
        }
        gameSession.log(new GameSession.GameLogEntry(new JSONObject().put("event", "END").put("winner", winner.getName())));
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
                            AuroraMCAPI.setShuttingDown(true);
                        }
                    }.runTaskLater(AuroraMCAPI.getCore(), 200);
                    return;
                }

                for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                    JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("PLAYERS");
                    int x, y, z;
                    x = spawnLocations.getJSONObject(0).getInt("x");
                    y = spawnLocations.getJSONObject(0).getInt("y");
                    z = spawnLocations.getJSONObject(0).getInt("z");
                    float yaw = spawnLocations.getJSONObject(0).getFloat("yaw");
                    pl.getPlayer().teleport(new Location(Bukkit.getWorld("world"), x, y, z, yaw, 0));
                    pl.getPlayer().setFallDistance(0);
                    pl.getPlayer().setVelocity(new Vector());
                    pl.getPlayer().setFlying(false);
                    pl.getPlayer().setAllowFlight(false);
                    pl.getPlayer().setHealth(20);
                    pl.getPlayer().setFoodLevel(30);
                    pl.getPlayer().getInventory().clear();
                    pl.getPlayer().setFireTicks(0);

                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
                    if (!player.isVanished()) {
                        player.setSpectator(false);
                    }
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


                    for (AuroraMCPlayer player1 : AuroraMCAPI.getPlayers()) {
                        if (player1.getRank().getId() >= player.getRank().getId()) {
                            player1.getPlayer().showPlayer(player.getPlayer());
                            player1.updateNametag(pl);
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
                    EngineAPI.setActiveGameInfo(null);
                    EngineAPI.setActiveGame(null);
                    EngineAPI.setActiveMap(null);
                    EngineAPI.setServerState(ServerState.IDLE);
                }

                if (EngineAPI.getServerState() != ServerState.STARTING && EngineAPI.getActiveGame() != null) {
                    if (AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished()).count() >= AuroraMCAPI.getServerInfo().getServerType().getInt("min_players")) {
                        EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30));
                        EngineAPI.getGameStartingRunnable().runTaskTimer(AuroraMCAPI.getCore(), 0, 20);
                    }
                }

                for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
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
        }.runTaskLater(AuroraMCAPI.getCore(), 200);
    }

    public abstract void onPlayerJoin(Player player);

    public abstract void onPlayerJoin(AuroraMCGamePlayer player);

    public List<Kit> getKits() {
        return kits;
    }

    public Map<String, Team> getTeams() {
        return teams;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public boolean isStarting() {
        return starting;
    }
}
