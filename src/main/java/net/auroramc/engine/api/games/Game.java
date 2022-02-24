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
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
        StringBuilder startString = new StringBuilder();
        startString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");
        startString.append(" \n§b§lGame: §r");
        startString.append(EngineAPI.getActiveGameInfo().getName());
        if (gameVariation != null) {
            startString.append(" ");
            startString.append(gameVariation.getName());
        }
        startString.append("\n \n§r");
        startString.append(EngineAPI.getActiveGameInfo().getDescription());
        startString.append("\n \n");
        startString.append("§b§lMap: §r");;
        startString.append(map.getName());
        startString.append(" by ");
        startString.append(map.getAuthor());
        startString.append("\n");
        startString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getPlayer().setGameMode(GameMode.SURVIVAL);
            player.getPlayer().setHealth(20);
            player.getPlayer().setFoodLevel(30);
            player.getPlayer().getInventory().clear();
            player.getPlayer().setExp(0);
            player.getPlayer().setLevel(0);
            player.getPlayer().getEnderChest().clear();
            player.sendMessage(startString.toString());
            AuroraMCPlayer player1 = AuroraMCAPI.getPlayer(player);
            if (player1 instanceof AuroraMCGamePlayer) {
                AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player1;
                pl.setLastHitAt(-1);
                pl.setLastHitBy(null);
                pl.getLatestHits().clear();
                if (player1.isVanished() || ((AuroraMCGamePlayer) player1).isSpectator()) {
                    player.getPlayer().setAllowFlight(true);
                    player.getPlayer().setFlying(true);
                    pl.setSpectator(true, true);
                    for (Player player2 : Bukkit.getOnlinePlayers()) {
                        player2.hidePlayer(player1.getPlayer());
                    }
                } else {
                    player.getPlayer().setFlying(false);
                    player.getPlayer().setAllowFlight(false);
                }
            } else {
                player.getPlayer().setFlying(false);
                player.getPlayer().setAllowFlight(false);
            }
            if (map.getMapData().has("time")) {
                if (map.getMapData().getInt("time") <= 12000) {
                    player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                } else {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 0, true, false), false);
                }
            } else {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            }

        }
        starting = true;
        runnable = new InGameStartingRunnable(this);
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
        winnerString.append("§b§l");
        winnerString.append((winner == null)?"Nobody":winner.getPlayer().getName());
        winnerString.append(" won the game!");
        winnerString.append("\n \n \n");
        winnerString.append("§b§lMap: §r");
        winnerString.append(map.getName());
        winnerString.append(" by ");
        winnerString.append(map.getAuthor());
        winnerString.append("\n");
        winnerString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");

        for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
            player.getPlayer().sendMessage(winnerString.toString());
            player.sendTitle((winner == null)?"Nobody won the game":winner.getPlayer().getName() + " won the game!", "", 10, 160, 10, ChatColor.AQUA, ChatColor.AQUA, true, false);
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.LEVEL_UP, 100, 1);
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;
            if (pl.getRewards() != null && !pl.isVanished() && !pl.isSpectator()) {
                if (winner != null) {
                    pl.getStats().addGamePlayed(winner.equals(pl));
                    pl.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesPlayed", 1, true);
                    if (winner.equals(pl)) {
                        pl.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesWon", 1, true);
                    }
                }
                pl.getRewards().stop();
            }

        }

        if (winner != null) {
            Cosmetic cosmetic = winner.getActiveCosmetics().get(Cosmetic.CosmeticType.WIN_EFFECT);
            if (cosmetic != null) {
                WinEffect winEffect = (WinEffect) cosmetic;
                winEffect.onWin(winner);
            }
            AuroraMCGamePlayer player = (AuroraMCGamePlayer) winner;
            player.getRewards().addXp("Winner Bonus", 150);
            player.getRewards().addTickets(150);
            player.getRewards().addCrowns(150);
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
        winnerString.append("§");
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
        winnerString.append("§b§lMap: §r");
        winnerString.append(map.getName());
        winnerString.append(" by ");
        winnerString.append(map.getAuthor());
        winnerString.append("\n");
        winnerString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");

        for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
            player.getPlayer().sendMessage(winnerString.toString());
            player.sendTitle(winner.getName() + " won the game!", "", 10, 160, 10, ChatColor.getByChar(winner.getTeamColor()), ChatColor.AQUA, true, false);
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.LEVEL_UP, 100, 1);
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;
            pl.getStats().addGamePlayed(winner.getPlayers().contains(pl));
            if (!pl.isVanished() && !pl.isSpectator()) {
                pl.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesPlayed", 1, true);
                if (winner.getPlayers().contains(pl)) {
                    pl.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesWon", 1, true);
                }
                if (pl.getRewards() != null) {
                    pl.getRewards().stop();
                }
            }
        }

        for (AuroraMCPlayer amcPlayer : winner.getPlayers()) {
            AuroraMCGamePlayer player = (AuroraMCGamePlayer) amcPlayer;
            player.getRewards().addXp("Winner Bonus", 150);
            player.getRewards().addTickets(150);
            player.getRewards().addCrowns(150);
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
                        AuroraMCGamePlayer player1 = (AuroraMCGamePlayer) AuroraMCAPI.getPlayer(player);
                        if (player1 != null) {
                            if (player1.getJoinTimestamp() > gameSession.getStartTimestamp()) {
                                //The player joined after the game started, go from when they joined.
                                player1.getStats().addGameTime(gameSession.getEndTimestamp() - player1.getJoinTimestamp(), true);
                            } else {
                                player1.getStats().addGameTime(gameSession.getEndTimestamp() - gameSession.getStartTimestamp(), true);
                            }
                            if (player1.getRewards() != null) {
                                if (!player1.isSpectator() && !player1.isVanished()) {
                                    player1.getRewards().apply(true);
                                }
                                player1.gameOver();
                            }
                        }

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
                    pl.getPlayer().setGameMode(GameMode.SURVIVAL);
                    pl.getPlayer().setExp(0);
                    pl.getPlayer().setLevel(0);
                    pl.getPlayer().getEnderChest().clear();
                    for (PotionEffect pe : pl.getPlayer().getActivePotionEffects()) {
                        pl.getPlayer().removePotionEffect(pe.getType());
                    }

                    if (EngineAPI.getWaitingLobbyMap().getMapData().getInt("time") > 12000) {
                        pl.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 0, true, false), false);
                    }


                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
                    player.setLastHitAt(-1);
                    player.setLastHitBy(null);
                    player.getLatestHits().clear();
                    if (player.getJoinTimestamp() > gameSession.getStartTimestamp()) {
                        //The player joined after the game started, go from when they joined.
                        player.getStats().addGameTime(gameSession.getEndTimestamp() - player.getJoinTimestamp(), true);
                    } else {
                        player.getStats().addGameTime(gameSession.getEndTimestamp() - gameSession.getStartTimestamp(), true);
                    }
                    if (!player.isVanished() && !player.isOptedSpec()) {
                        player.setSpectator(false, false);
                    } else {
                        for (AuroraMCPlayer player1 : AuroraMCAPI.getPlayers()) {
                            if (player1.getRank().getId() >= player.getRank().getId()) {
                                player1.getPlayer().showPlayer(player.getPlayer());
                            }
                        }
                    }
                    for (Map.Entry<Cosmetic.CosmeticType, Cosmetic> entry : player.getActiveCosmetics().entrySet()) {
                        if (entry.getKey() == Cosmetic.CosmeticType.GADGET || entry.getKey() == Cosmetic.CosmeticType.BANNER || entry.getKey() == Cosmetic.CosmeticType.HAT || entry.getKey() == Cosmetic.CosmeticType.MORPH || entry.getKey() == Cosmetic.CosmeticType.PET || entry.getKey() == Cosmetic.CosmeticType.PARTICLE) {
                            entry.getValue().onEquip(player);
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Cosmetics", String.format("**%s** has been re-equipped.", entry.getValue().getName())));
                        }
                    }
                    if (player.getRewards() != null) {
                        if (!player.isSpectator() && !player.isVanished()) {
                            player.getRewards().apply(true);
                        }
                        player.gameOver();
                    }

                    player.setKit(null);
                    player.setTeam(null);
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
                    if (EngineAPI.getNextMap() != null) {
                        GameUtils.loadGame(EngineAPI.getNextGame(), EngineAPI.getNextMap(), EngineAPI.getNextVariation());
                    } else {
                        GameUtils.loadGame(EngineAPI.getNextGame(), EngineAPI.getNextVariation());
                    }

                    EngineAPI.setNextMap(null);
                    EngineAPI.setNextGame(null);
                    EngineAPI.setNextVariation(null);
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
                        player.getPlayer().getInventory().setItem(0, EngineAPI.getKitItem().getItem());
                        if (EngineAPI.getActiveGame().getTeams().size() > 1 && !EngineAPI.getActiveGameInfo().hasTeamCommand() && EngineAPI.isTeamBalancingEnabled()) {
                            player.getPlayer().getInventory().setItem(1, EngineAPI.getTeamItem().getItem());
                        }
                    }
                }
            }
        }.runTaskLater(AuroraMCAPI.getCore(), 200);
    }

    public abstract void generateTeam(AuroraMCPlayer player);

    public abstract void onPlayerJoin(Player player);

    public abstract void onPlayerJoin(AuroraMCGamePlayer player);

    public abstract void onPlayerLeave(AuroraMCGamePlayer player);

    public abstract void onRespawn(AuroraMCGamePlayer player);

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
