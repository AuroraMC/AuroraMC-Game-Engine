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
import net.auroramc.core.api.players.Team;
import net.auroramc.core.api.players.scoreboard.PlayerScoreboard;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.GameUtils;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import net.auroramc.engine.api.util.InGameStartingRunnable;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public abstract class Game {

    protected GameVariation gameVariation;
    protected GameMap map;
    protected Map<String, Team> teams;
    protected List<Kit> kits;
    protected GameSession gameSession;
    protected boolean starting;
    protected InGameStartingRunnable runnable;

    protected boolean voided;


    public Game(GameVariation gameVariation) {
        this.gameVariation = gameVariation;
        this.teams = new HashMap<>();
        this.kits = new ArrayList<>();
        gameSession = new GameSession(EngineAPI.getActiveGameInfo().getRegistryKey(),gameVariation);
        starting = false;
        voided = AuroraMCAPI.isTestServer();
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
            player.getPlayer().setFallDistance(0);
            player.getPlayer().setVelocity(new Vector());
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getPlayer().setExp(0);
            player.getPlayer().setLevel(0);
            player.getPlayer().getEnderChest().clear();
            player.sendMessage(startString.toString());
            AuroraMCPlayer player1 = AuroraMCAPI.getPlayer(player);
            if (player1 instanceof AuroraMCGamePlayer) {
                AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player1;
                pl.getGameData().clear();
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
                    if (!player1.getStats().getAchievementsGained().containsKey(AuroraMCAPI.getAchievement(8))) {
                        player1.getStats().achievementGained(AuroraMCAPI.getAchievement(8), 1, true);
                    }
                    player1.getStats().addProgress(AuroraMCAPI.getAchievement(9), 1, player1.getStats().getAchievementsGained().getOrDefault(AuroraMCAPI.getAchievement(9), 0), true);
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
        gameSession.log(new GameSession.GameLogEntry(GameSession.GameEvent.START, new JSONObject()));
        new BukkitRunnable(){
            @Override
            public void run() {
                EngineDatabaseManager.gameStarted();
            }
        }.runTaskAsynchronously(EngineAPI.getGameEngine());
    }

    public void inProgress() {
        starting = false;
        runnable = null;
        gameSession.log(new GameSession.GameLogEntry(GameSession.GameEvent.RELEASED, new JSONObject()));
    }

    /**
     * When executed by the game, it should indicate that the game is handing control back to the Game Engine and the game is no longer in progress. Should execute super.
     */
    public void end(AuroraMCPlayer winner) {
        EngineAPI.setServerState(ServerState.ENDING);
        if (runnable != null) {
            runnable.cancel();
        }
        gameSession.log(new GameSession.GameLogEntry(GameSession.GameEvent.END, new JSONObject().put("winner", ((winner == null)?"NONE":winner.getName()))));
        gameSession.end(voided);
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
            if (player.equals(winner) && player.isDisguised() && player.getPreferences().isHideDisguiseNameEnabled()) {
                String winnerString2 = "§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n" +
                        " \n \n" +
                        "§b§l" +
                        winner.getName() +
                        " won the game!" +
                        "\n \n \n" +
                        "§b§lMap: §r" +
                        map.getName() +
                        " by " +
                        map.getAuthor() +
                        "\n" +
                        "§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n";
                player.getPlayer().sendMessage(winnerString2);
            } else {
                player.getPlayer().sendMessage(winnerString.toString());
            }
            player.sendTitle((winner == null)?"Nobody won the game":((player.equals(winner) && player.isDisguised() && player.getPreferences().isHideDisguiseNameEnabled())?winner.getName():winner.getPlayer().getName()) + " won the game!", "", 10, 160, 10, ChatColor.AQUA, ChatColor.AQUA, true, false);
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.LEVEL_UP, 100, 1);
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;
            if (pl.getRewards() != null && !pl.isVanished() && !pl.isOptedSpec()) {
                if (winner != null && !voided) {
                    pl.getStats().addGamePlayed(winner.equals(pl));
                    pl.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesPlayed", 1, true);
                    if (winner.equals(pl)) {
                        pl.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesWon", 1, true);
                        if (!pl.getStats().getAchievementsGained().containsKey(AuroraMCAPI.getAchievement(21))) {
                            pl.getStats().achievementGained(AuroraMCAPI.getAchievement(21), 1, true);
                        }
                    } else {
                        if (!pl.getStats().getAchievementsGained().containsKey(AuroraMCAPI.getAchievement(20))) {
                            pl.getStats().achievementGained(AuroraMCAPI.getAchievement(20), 1, true);
                        }
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
            if (!voided) {
                player.getRewards().addXp("Winner Bonus", 150);
                player.getRewards().addTickets(150);
                player.getRewards().addCrowns(150);
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
        gameSession.log(new GameSession.GameLogEntry(GameSession.GameEvent.END, new JSONObject().put("winner", winner.getName())));
        gameSession.end(voided);
        StringBuilder winnerString = new StringBuilder();
        winnerString.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");
        winnerString.append(" \n \n");
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
            if (winner.getPlayers().contains(player) && player.isDisguised() && player.getPreferences().isHideDisguiseNameEnabled()) {
                StringBuilder winnerString2 = new StringBuilder();
                winnerString2.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");
                winnerString2.append(" \n \n");
                winnerString2.append("§");
                winnerString2.append(winner.getTeamColor());
                winnerString2.append("§l");
                winnerString2.append((winnerName != null)?winnerName:winner.getName());
                winnerString2.append(" won the game!");
                winnerString2.append("\n§rPlayers: §b");
                winners = new ArrayList<>();
                for (AuroraMCPlayer player2 : winner.getPlayers()) {
                    if (player2.equals(player)) {
                        winners.add(player2.getName());
                    } else {
                        winners.add(player2.getPlayer().getName());
                    }
                }
                winnerString2.append(String.join("§r, §b", winners));
                winnerString2.append("\n \n");
                winnerString2.append("§b§lMap: §r");
                winnerString2.append(map.getName());
                winnerString2.append(" by ");
                winnerString2.append(map.getAuthor());
                winnerString2.append("\n");
                winnerString2.append("§3§l▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆\n");
                player.getPlayer().sendMessage(winnerString2.toString());
            } else {
                player.getPlayer().sendMessage(winnerString.toString());
            }
            player.sendTitle(winner.getName() + " won the game!", "", 10, 160, 10, ChatColor.getByChar(winner.getTeamColor()), ChatColor.AQUA, true, false);
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.LEVEL_UP, 100, 1);
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;
            if (!voided) {
                pl.getStats().addGamePlayed(winner.getPlayers().contains(pl));
                if (!pl.isVanished() && !pl.isOptedSpec() && pl.getRewards() != null) {
                    pl.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesPlayed", 1, true);
                    if (winner.getPlayers().contains(pl)) {
                        pl.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesWon", 1, true);
                        if (!pl.getStats().getAchievementsGained().containsKey(AuroraMCAPI.getAchievement(21))) {
                            pl.getStats().achievementGained(AuroraMCAPI.getAchievement(21), 1, true);
                        }
                    } else {
                        if (!pl.getStats().getAchievementsGained().containsKey(AuroraMCAPI.getAchievement(20))) {
                            pl.getStats().achievementGained(AuroraMCAPI.getAchievement(20), 1, true);
                        }
                    }
                    if (pl.getRewards() != null) {
                        pl.getRewards().stop();
                    }
                }
            }
        }


            for (AuroraMCPlayer amcPlayer : winner.getPlayers()) {
                AuroraMCGamePlayer player = (AuroraMCGamePlayer) amcPlayer;
                if (!voided) {
                    player.getRewards().addXp("Winner Bonus", 150);
                    player.getRewards().addTickets(150);
                    player.getRewards().addCrowns(150);
                }
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
                        if (player1 != null && !voided) {
                            if (!voided) {
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
                            } else {
                                player1.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "This game was void, so any rewards or statistics earned during this game did not apply to your account."));
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
                    pl.getPlayer().getInventory().setArmorContents(new ItemStack[4]);
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
                    player.getGameData().clear();
                    player.setLastHitAt(-1);
                    player.setLastHitBy(null);
                    player.getLatestHits().clear();
                    if (!voided) {
                        if (player.getJoinTimestamp() > gameSession.getStartTimestamp()) {
                            //The player joined after the game started, go from when they joined.
                            player.getStats().addGameTime(gameSession.getEndTimestamp() - player.getJoinTimestamp(), true);
                        } else {
                            player.getStats().addGameTime(gameSession.getEndTimestamp() - gameSession.getStartTimestamp(), true);
                        }
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
                        if (entry.getKey() == Cosmetic.CosmeticType.GADGET || entry.getKey() == Cosmetic.CosmeticType.BANNER || entry.getKey() == Cosmetic.CosmeticType.HAT || entry.getKey() == Cosmetic.CosmeticType.PARTICLE) {
                            entry.getValue().onEquip(player);
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Cosmetics", String.format("**%s** has been re-equipped.", entry.getValue().getName())));
                        }
                    }
                    if (player.getRewards() != null) {
                        if (!player.isSpectator() && !player.isVanished()) {
                            if (!voided) {
                                player.getRewards().apply(true);
                            } else {
                                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "This game was void, so any rewards or statistics earned during this game did not apply to your account."));
                            }
                        }
                        player.gameOver();
                    }



                    player.setKit(null);
                    pl.setTeam(null);
                    PlayerScoreboard scoreboard = player.getScoreboard();
                    scoreboard.clear();
                    scoreboard.setTitle("&3-= &b&l" + EngineAPI.getServerState().getName().toUpperCase() + "&r &3=-");
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
                    if (player.getPreferences().isHideDisguiseNameEnabled() && player.isDisguised()) {
                        scoreboard.setLine(3, "&oHidden");
                    } else {
                        scoreboard.setLine(3, AuroraMCAPI.getServerInfo().getName());
                    }
                    scoreboard.setLine(2, "    ");
                    scoreboard.setLine(1, "&7auroramc.net");
                    player.getPlayer().getInventory().setItem(8, EngineAPI.getLobbyItem().getItem());
                    player.getPlayer().getInventory().setItem(7, EngineAPI.getPrefsItem().getItem());
                    player.getPlayer().getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItem());
                }

                if (new Random().nextBoolean()) {
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            TextComponent textComponent = new TextComponent("");

                            TextComponent lines = new TextComponent("-----------------------------------------------------");
                            lines.setStrikethrough(true);
                            lines.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
                            textComponent.addExtra(lines);

                            textComponent.addExtra("\n");

                            String title;

                            String msg;

                            switch (new Random().nextInt(5)) {
                                case 1:
                                    title = "Support the network by subscribing to AuroraMC Plus!";
                                    msg = "A Plus subscription gives you some awesome new features, and supports us in the process! View all of our packages at ";
                                    break;
                                case 2:
                                    title = "Take your AuroraMC experience to the next level!";
                                    msg = "Purchase the AuroraMC Starter Pack to get a head start, it comes with Elite and 11 exclusive cosmetics! Purchase the bundle at ";
                                    break;
                                case 3:
                                    title = "Want some cool limited-time cosmetics and other extras?!";
                                    msg = "Our Grand Celebration bundle is on sale for a limited-time only! This bundle is only live for 30 days, so grab it while you can! Purchase the bundle at ";
                                    break;
                                case 4:
                                    title = "You seem cool... and cool people deserve ranks!";
                                    msg = "Ranks are a great way to support the network, and you get loads of benefits too! See all rank benefits at ";
                                    break;
                                default:
                                    title = "Did you enjoy this game?";
                                    msg = "Consider supporting AuroraMC by purchasing a premium rank! Check out our latest offerings at ";
                                    break;
                            }

                            TextComponent enjoy = new TextComponent(title);
                            enjoy.setBold(true);
                            enjoy.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                            textComponent.addExtra(enjoy);

                            textComponent.addExtra("\n \n");

                            TextComponent purchase = new TextComponent(msg);
                            textComponent.addExtra(purchase);

                            TextComponent store = new TextComponent("store.auroramc.net");
                            store.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(AuroraMCAPI.getFormatter().convert("&aClickt to visit the store!")).create()));
                            store.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://store.auroramc.net"));
                            store.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                            textComponent.addExtra(store);
                            textComponent.addExtra(lines);

                            TextComponent log = new TextComponent(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "**The game you just played has generated a game log. Click here to view the game log online!**"));
                            log.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open game log!").color(ChatColor.GREEN.asBungee()).create()));
                            log.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gamelogs.auroramc.net/log?uuid=" + gameSession.getUuid()));
                            log.setColor(net.md_5.bungee.api.ChatColor.AQUA);

                            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                                if (!player.hasPermission("elite") && !player.hasPermission("plus")) {
                                    player.getPlayer().spigot().sendMessage(textComponent);
                                }
                                player.getPlayer().spigot().sendMessage(log);
                            }
                        }
                    }.runTaskLater(AuroraMCAPI.getCore(), 100);
                } else {
                    TextComponent log = new TextComponent(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "**The game you just played has generated a game log. Click here to view the game log online!**"));
                    log.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open game log!").color(ChatColor.GREEN.asBungee()).create()));
                    log.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gamelogs.auroramc.net/log?uuid=" + gameSession.getUuid()));
                    log.setColor(net.md_5.bungee.api.ChatColor.AQUA);

                    for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                        player.getPlayer().spigot().sendMessage(log);
                    }
                }

                if (EngineAPI.isAwaitingMapReload()) {
                    EngineAPI.setActiveGameInfo(null);
                    EngineAPI.setActiveGame(null);
                    EngineAPI.setActiveMap(null);
                    EngineAPI.setServerState(ServerState.IDLE);
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            EngineAPI.reloadMaps();
                        }
                    }.runTaskAsynchronously(AuroraMCAPI.getCore());
                } else {
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
                }

                if (EngineAPI.getServerState() != ServerState.STARTING && EngineAPI.getActiveGame() != null) {
                    if (AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && !((AuroraMCGamePlayer)player1).isOptedSpec()).count() >= AuroraMCAPI.getServerInfo().getServerType().getInt("min_players")) {
                        EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30));
                        EngineAPI.getGameStartingRunnable().runTaskTimer(AuroraMCAPI.getCore(), 0, 20);
                    }
                }

                for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
                    if (EngineAPI.getActiveGame() != null) {
                        player.getPlayer().getInventory().setItem(0, EngineAPI.getKitItem().getItem());
                        if (EngineAPI.getActiveGame().getTeams().size() > 1 && !EngineAPI.getActiveGameInfo().hasTeamCommand() && !EngineAPI.isTeamBalancingEnabled()) {
                            player.getPlayer().getInventory().setItem(1, EngineAPI.getTeamItem().getItem());
                        }
                    }
                    for (AuroraMCPlayer player1 : AuroraMCAPI.getPlayers()) {
                        if (player1.getRank().getId() >= player.getRank().getId() || !player.isVanished()) {
                            player1.getPlayer().showPlayer(player.getPlayer());
                            player1.updateNametag(player);
                        }
                        if (player.getRank().getId() >= player1.getRank().getId() || !player1.isVanished()) {
                            player.getPlayer().showPlayer(player1.getPlayer());
                            player.updateNametag(player1);
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

    public abstract boolean onDeath(AuroraMCGamePlayer player, AuroraMCGamePlayer killer);

    public abstract void onFinalKill(AuroraMCGamePlayer player);

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

    public void voidGame(String reason) {
        if (!voided) {
            voided = true;
            gameSession.log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", "This game has now been voided" + ((reason != null)?" because " + reason:"") + ".")));
            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "This game has now been voided" + ((reason != null)?" because " + reason:"") + ". Any statistics or rewards earned after this point will not apply to your account. Achievements earned up until this point in the game will still apply."));
            }
        }
    }
}
