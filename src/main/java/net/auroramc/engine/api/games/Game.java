/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api.games;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.backend.info.ServerInfo;
import net.auroramc.api.cosmetics.Cosmetic;
import net.auroramc.api.cosmetics.WinEffect;
import net.auroramc.api.player.AuroraMCPlayer;
import net.auroramc.api.player.Team;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.backend.communication.CommunicationUtils;
import net.auroramc.core.api.backend.communication.Protocol;
import net.auroramc.core.api.backend.communication.ProtocolMessage;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.core.api.player.scoreboard.PlayerScoreboard;
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
            AuroraMCServerPlayer player1 = ServerAPI.getPlayer(player);
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
                    for (AuroraMCServerPlayer player2 : ServerAPI.getPlayers()) {
                        player2.hidePlayer(player1);
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
    public void end(AuroraMCServerPlayer winner) {
        EngineAPI.setServerState(ServerState.ENDING);
        if (runnable != null) {
            runnable.cancel();
        }
        gameSession.log(new GameSession.GameLogEntry(GameSession.GameEvent.END, new JSONObject().put("winner", ((winner == null)?"NONE":winner.getName()))));
        gameSession.end(voided);

        TextComponent winnerComponent = new TextComponent("");

        TextComponent lines = new TextComponent("▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆");
        lines.setBold(true);
        lines.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
        winnerComponent.addExtra(lines);

        winnerComponent.addExtra("\n \n \n");

        TextComponent cmp = new TextComponent(((winner == null)?"Nobody":winner.getByDisguiseName()) + " won the game!");
        cmp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        cmp.setBold(true);
        winnerComponent.addExtra(cmp);

        winnerComponent.addExtra("\n \n \n");

        cmp = new TextComponent("Map: ");
        cmp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        cmp.setBold(true);
        winnerComponent.addExtra(cmp);

        cmp = new TextComponent(map.getName() + " by " + map.getAuthor() + "\n");
        cmp.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        cmp.setBold(false);
        winnerComponent.addExtra(cmp);
        winnerComponent.addExtra(lines);

        for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
            if (player.equals(winner) && player.isDisguised() && player.getPreferences().isHideDisguiseNameEnabled()) {
                TextComponent winnerComponent2 = new TextComponent("");
                winnerComponent2.addExtra(lines);
                winnerComponent2.addExtra("\n \n \n");

                cmp = new TextComponent(winner.getName() + " won the game!");
                cmp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                cmp.setBold(true);
                winnerComponent2.addExtra(cmp);

                winnerComponent2.addExtra("\n \n \n");

                cmp = new TextComponent("Map: ");
                cmp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                cmp.setBold(true);
                winnerComponent2.addExtra(cmp);

                cmp = new TextComponent(map.getName() + " by " + map.getAuthor() + "\n");
                cmp.setColor(net.md_5.bungee.api.ChatColor.WHITE);
                cmp.setBold(false);
                winnerComponent2.addExtra(cmp);

                winnerComponent2.addExtra(lines);
                player.sendMessage(winnerComponent2);
            } else {
                player.sendMessage(winnerComponent);
            }

            TextComponent title = new TextComponent((winner == null)?"Nobody won the game":((player.equals(winner) && player.isDisguised() && player.getPreferences().isHideDisguiseNameEnabled())?winner.getName():winner.getByDisguiseName()) + " won the game!");
            title.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            title.setBold(true);



            player.sendTitle(title, new TextComponent(""), 10, 160, 10);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 100, 1);
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;
            if (pl.getRewards() != null && !pl.isVanished() && !pl.isOptedSpec()) {
                if (winner != null && !voided) {
                    pl.getStats().addGamePlayed(winner.equals(pl), true);
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

        TextComponent winnerComponent = new TextComponent("");

        TextComponent lines = new TextComponent("▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆");
        lines.setBold(true);
        lines.setColor(net.md_5.bungee.api.ChatColor.DARK_AQUA);
        winnerComponent.addExtra(lines);

        winnerComponent.addExtra("\n \n \n");

        TextComponent cmp = new TextComponent((winnerName != null)?winnerName:winner.getName() + " won the game!");
        cmp.setColor(winner.getTeamColor());
        cmp.setBold(true);
        winnerComponent.addExtra(cmp);

        cmp = new TextComponent("\nPlayers: ");
        cmp.setBold(false);
        cmp.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        winnerComponent.addExtra(cmp);

        TextComponent comma = new TextComponent(", ");
        comma.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        comma.setBold(false);

        for (Iterator<AuroraMCPlayer> iterator = winner.getPlayers().iterator(); iterator.hasNext(); ) {
            AuroraMCPlayer player = iterator.next();
            cmp = new TextComponent(player.getByDisguiseName());
            cmp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
            cmp.setBold(false);
            winnerComponent.addExtra(cmp);
            if (iterator.hasNext()) {
                winnerComponent.addExtra(comma);
            }
        }

        winnerComponent.addExtra("\n \n");

        cmp = new TextComponent("Map: ");
        cmp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        cmp.setBold(true);
        winnerComponent.addExtra(cmp);

        cmp = new TextComponent(map.getName() + " by " + map.getAuthor() + "\n");
        cmp.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        cmp.setBold(false);
        winnerComponent.addExtra(cmp);

        winnerComponent.addExtra(lines);

        for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
            if (winner.getPlayers().contains(player) && player.isDisguised() && player.getPreferences().isHideDisguiseNameEnabled()) {
                TextComponent winnerComponent2 = new TextComponent("");

                winnerComponent2.addExtra(lines);

                winnerComponent2.addExtra("\n \n \n");

                cmp = new TextComponent((winnerName != null)?winnerName:winner.getName() + " won the game!");
                cmp.setColor(winner.getTeamColor());
                cmp.setBold(true);
                winnerComponent2.addExtra(cmp);

                cmp = new TextComponent("\nPlayers: ");
                cmp.setBold(false);
                cmp.setColor(net.md_5.bungee.api.ChatColor.WHITE);
                winnerComponent2.addExtra(cmp);

                for (Iterator<AuroraMCPlayer> iterator = winner.getPlayers().iterator(); iterator.hasNext(); ) {
                    AuroraMCPlayer winner2 = iterator.next();
                    cmp = new TextComponent(((winner2.equals(player))?winner2.getName():winner2.getByDisguiseName()));
                    cmp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                    cmp.setBold(false);
                    winnerComponent2.addExtra(cmp);
                    if (iterator.hasNext()) {
                        winnerComponent2.addExtra(comma);
                    }
                }

                winnerComponent2.addExtra("\n \n");

                cmp = new TextComponent("Map: ");
                cmp.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                cmp.setBold(true);
                winnerComponent2.addExtra(cmp);

                cmp = new TextComponent(map.getName() + " by " + map.getAuthor() + "\n");
                cmp.setColor(net.md_5.bungee.api.ChatColor.WHITE);
                cmp.setBold(false);
                winnerComponent2.addExtra(cmp);

                winnerComponent2.addExtra(lines);

                player.sendMessage(winnerComponent2);
            } else {
                player.sendMessage(winnerComponent);
            }

            TextComponent component = new TextComponent(winner.getName() + " won the game!");
            component.setColor(winner.getTeamColor());
            component.setBold(true);

            player.sendTitle(component, new TextComponent(""), 10, 160, 10);
            player.playSound(player.getLocation(), Sound.LEVEL_UP, 100, 1);
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;
            if (!voided) {
                pl.getStats().addGamePlayed(winner.getPlayers().contains(pl), true);
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
                        AuroraMCGamePlayer player1 = (AuroraMCGamePlayer) ServerAPI.getPlayer(player);
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
                                player1.sendMessage(TextFormatter.pluginMessage("Game Manager", "This game was void, so any rewards or statistics earned during this game did not apply to your account."));
                            }

                        }

                        player.spigot().sendMessage(TextFormatter.pluginMessage("Server Manager", "This server is restarting for an update. You are being sent to a lobby."));
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Lobby");
                        out.writeUTF(player.getUniqueId().toString());
                        player.sendPluginMessage(ServerAPI.getCore(), "BungeeCord", out.toByteArray());
                    }
                    //Wait 10 seconds, then close the server
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.kickPlayer(TextFormatter.pluginMessageRaw("Server Manager", "This server is restarting.\n\nYou can reconnect to the network to continue playing!"));
                            }
                            CommunicationUtils.sendMessage(new ProtocolMessage(Protocol.CONFIRM_SHUTDOWN, "Mission Control", EngineAPI.getRestartType(), AuroraMCAPI.getInfo().getName(), AuroraMCAPI.getInfo().getNetwork().name()));
                            AuroraMCAPI.setShuttingDown(true);
                        }
                    }.runTaskLater(ServerAPI.getCore(), 200);
                    return;
                }

                for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
                    JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("PLAYERS");
                    int x, y, z;
                    x = spawnLocations.getJSONObject(0).getInt("x");
                    y = spawnLocations.getJSONObject(0).getInt("y");
                    z = spawnLocations.getJSONObject(0).getInt("z");
                    float yaw = spawnLocations.getJSONObject(0).getFloat("yaw");
                    pl.teleport(new Location(Bukkit.getWorld("world"), x, y, z, yaw, 0));
                    pl.setFallDistance(0);
                    pl.setVelocity(new Vector());
                    pl.setFlying(false);
                    pl.setAllowFlight(false);
                    pl.setHealth(20);
                    pl.setFoodLevel(30);
                    pl.getInventory().clear();
                    pl.getInventory().setArmorContents(new ItemStack[4]);
                    pl.setFireTicks(0);
                    pl.setGameMode(GameMode.SURVIVAL);
                    pl.setExp(0);
                    pl.setLevel(0);
                    pl.getEnderChest().clear();
                    for (PotionEffect pe : pl.getActivePotionEffects()) {
                        pl.removePotionEffect(pe.getType());
                    }

                    if (EngineAPI.getWaitingLobbyMap().getMapData().getInt("time") > 12000) {
                        pl.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 0, true, false), false);
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
                        for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
                            if (player1.getRank().getId() >= player.getRank().getId()) {
                                player1.showPlayer(player);
                            }
                        }
                    }
                    for (Map.Entry<Cosmetic.CosmeticType, Cosmetic> entry : player.getActiveCosmetics().entrySet()) {
                        if (entry.getKey() == Cosmetic.CosmeticType.GADGET || entry.getKey() == Cosmetic.CosmeticType.BANNER || entry.getKey() == Cosmetic.CosmeticType.HAT || entry.getKey() == Cosmetic.CosmeticType.PARTICLE) {
                            entry.getValue().onEquip(player);
                            player.sendMessage(TextFormatter.pluginMessage("Cosmetics", String.format("**%s** has been re-equipped.", entry.getValue().getName())));
                        }
                    }
                    if (player.getRewards() != null) {
                        if (!player.isSpectator() && !player.isVanished()) {
                            if (!voided) {
                                player.getRewards().apply(true);
                            } else {
                                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "This game was void, so any rewards or statistics earned during this game did not apply to your account."));
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
                        scoreboard.setLine(3, AuroraMCAPI.getInfo().getName());
                    }
                    scoreboard.setLine(2, "    ");
                    scoreboard.setLine(1, "&7auroramc.net");
                    player.getInventory().setItem(8, EngineAPI.getLobbyItem().getItemStack());
                    player.getInventory().setItem(7, EngineAPI.getPrefsItem().getItemStack());
                    player.getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItemStack());
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
                            store.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to visit the store!").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
                            store.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://store.auroramc.net"));
                            store.setColor(net.md_5.bungee.api.ChatColor.AQUA);
                            textComponent.addExtra(store);
                            textComponent.addExtra("\n");
                            textComponent.addExtra(lines);

                            TextComponent log = new TextComponent(TextFormatter.pluginMessage("Game Manager", "**The game you just played has generated a game log. Click here to view the game log online!**"));
                            log.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open game log!").color(ChatColor.GREEN.asBungee()).create()));
                            log.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gamelogs.auroramc.net/log?uuid=" + gameSession.getUuid()));
                            log.setColor(net.md_5.bungee.api.ChatColor.AQUA);

                            for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                                if (!player.hasPermission("elite") && !player.hasPermission("plus")) {
                                    player.sendMessage(textComponent);
                                }
                                player.sendMessage(log);
                            }
                        }
                    }.runTaskLater(ServerAPI.getCore(), 100);
                } else {
                    TextComponent log = new TextComponent(TextFormatter.pluginMessage("Game Manager", "**The game you just played has generated a game log. Click here to view the game log online!**"));
                    log.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to open game log!").color(ChatColor.GREEN.asBungee()).create()));
                    log.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://gamelogs.auroramc.net/log?uuid=" + gameSession.getUuid()));
                    log.setColor(net.md_5.bungee.api.ChatColor.AQUA);

                    for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                        player.sendMessage(log);
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
                    }.runTaskAsynchronously(ServerAPI.getCore());
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

                if (EngineAPI.getServerState() != ServerState.STARTING && EngineAPI.getActiveGame() != null && EngineAPI.getGameStartingRunnable() != null) {
                    if (ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && !((AuroraMCGamePlayer)player1).isOptedSpec()).count() >= ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("min_players")) {
                        EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30, false));
                        EngineAPI.getGameStartingRunnable().runTaskTimer(ServerAPI.getCore(), 0, 20);
                    }
                }

                for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
                    if (EngineAPI.getActiveGame() != null) {
                        player.getInventory().setItem(0, EngineAPI.getKitItem().getItemStack());
                        if (EngineAPI.getActiveGame().getTeams().size() > 1 && !EngineAPI.getActiveGameInfo().hasTeamCommand() && !EngineAPI.isTeamBalancingEnabled()) {
                            player.getInventory().setItem(1, EngineAPI.getTeamItem().getItemStack());
                        }
                    }
                    for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
                        if (player1.getRank().getId() >= player.getRank().getId() || !player.isVanished()) {
                            player1.showPlayer(player);
                            player1.updateNametag(player);
                        }
                        if (player.getRank().getId() >= player1.getRank().getId() || !player1.isVanished()) {
                            player.showPlayer(player1);
                            player.updateNametag(player1);
                        }
                    }
                }
            }
        }.runTaskLater(ServerAPI.getCore(), 200);
    }

    public abstract void generateTeam(AuroraMCServerPlayer player);

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
            for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "This game has now been voided" + ((reason != null)?" because " + reason:"") + ". Any statistics or rewards earned after this point will not apply to your account. Achievements earned up until this point in the game will still apply."));
            }
        }
    }
}
