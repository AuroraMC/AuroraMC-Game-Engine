/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.backend.info.ServerInfo;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.core.api.utils.ZipUtil;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.AuroraMCGameEngine;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.events.ServerStateChangeEvent;
import net.auroramc.engine.api.games.*;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.players.Reward;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import net.auroramc.engine.api.util.TitleBarRunnable;
import net.auroramc.engine.api.util.VoidGenerator;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class EngineAPI {

    private static AuroraMCGameEngine gameEngine;
    private static final Map<String, GameInfo> games;
    private static GameMap waitingLobbyMap;
    private static ServerState serverState;
    private static Game activeGame;
    private static GameInfo activeGameInfo;
    private static GameMap activeMap;
    private static final Map<String, MapRegistry> maps;
    private static final List<GameInfo> gameRotation;
    private static World mapWorld;
    private static GameStartingRunnable gameStartingRunnable;

    private static boolean teamBalancingEnabled;

    private final static GUIItem lobbyItem;
    private final static GUIItem prefsItem;
    private final static GUIItem cosmeticsItem;
    private final static GUIItem kitItem;
    private final static GUIItem teamItem;

    private static final Random random;

    private static GameInfo nextGame;
    private static GameMap nextMap;
    private static GameVariation nextVariation;

    private static boolean awaitingRestart;
    private static String restartType;
    private static boolean awaitingMapReload;

    private final static Map<Integer, Reward> kitLevelRewards;

    private static String xpBoostMessage;
    private static float xpBoostMultiplier;

    static {
        games = new HashMap<>();
        maps = new HashMap<>();
        kitLevelRewards = new HashMap<>();
        gameRotation = new ArrayList<>();
        serverState = ServerState.STARTING_UP;

        teamBalancingEnabled = true;

        random = new Random();

        activeGame = null;
        activeGameInfo = null;
        activeMap = null;

        lobbyItem = new GUIItem(Material.WOOD_DOOR, "&a&lReturn to Lobby");
        prefsItem = new GUIItem(Material.REDSTONE_COMPARATOR, "&a&lView Preferences");
        cosmeticsItem = new GUIItem(Material.EMERALD, "&a&lView Cosmetics");
        kitItem = new GUIItem(Material.CHEST, "&a&lSelect Kit");
        teamItem = new GUIItem(Material.BOOK, "&a&lSelect Team");

       xpBoostMessage = EngineDatabaseManager.getXpMessage();
       xpBoostMultiplier = EngineDatabaseManager.getXpMultiplier();
    }

    public static void init(AuroraMCGameEngine gameEngine) {
        EngineAPI.gameEngine = gameEngine;
    }

    public static Map<String, GameInfo> getGames() {
        return games;
    }

    public static void registerGame(GameInfo game) {
        EngineAPI.games.put(game.getRegistryKey(), game);
    }

    public static GameMap getWaitingLobbyMap() {
        return waitingLobbyMap;
    }

    public static ServerState getServerState() {
        return serverState;
    }

    public static void setMapWorld(World mapWorld) {
        EngineAPI.mapWorld = mapWorld;
    }

    public static World getMapWorld() {
        return mapWorld;
    }

    public static void setServerState(ServerState serverState) {
        EngineAPI.serverState = serverState;
        Bukkit.getPluginManager().callEvent(new ServerStateChangeEvent(serverState));
    }

    public static AuroraMCGameEngine getGameEngine() {
        return gameEngine;
    }

    public static Game getActiveGame() {
        return activeGame;
    }

    public static GameMap getActiveMap() {
        return activeMap;
    }

    public static void setActiveGame(Game activeGame) {
        EngineAPI.activeGame = activeGame;
    }

    public static void setActiveMap(GameMap activeGameMap) {
        EngineAPI.activeMap = activeGameMap;
    }

    public static Map<String, MapRegistry> getMaps() {
        return maps;
    }

    public static void setWaitingLobbyMap(GameMap waitingLobbyMap) {
        EngineAPI.waitingLobbyMap = waitingLobbyMap;
    }

    public static List<GameInfo> getGameRotation() {
        return gameRotation;
    }

    public static int randomNumber(int start, int end) {
        return random.nextInt((end - start) + 1) + start;
    }

    public static int randomNumber(int end) {
        return random.nextInt(end);
    }

    public static GameInfo getActiveGameInfo() {
        return activeGameInfo;
    }

    public static void setActiveGameInfo(GameInfo activeGameInfo) {
        EngineAPI.activeGameInfo = activeGameInfo;
    }

    public static void loadRotation() {
        gameEngine.getLogger().info("Loading map world...");
        File file = new File(Bukkit.getWorldContainer(), "map_world");
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        World world = Bukkit.createWorld(new WorldCreator("map_world").generator(new VoidGenerator(gameEngine)));
        Bukkit.unloadWorld(world, false);

        gameEngine.getLogger().info("Map world loaded. Loading rotation...");

        for (Object object : ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getJSONArray("rotation")) {
            String string = (String) object;
            gameRotation.add(games.get(string));
        }
        gameEngine.getLogger().info(EngineAPI.getGameRotation().size() + " games loaded into rotation.");
        if (EngineAPI.getGameRotation().size() > 0) {
            gameEngine.getLogger().info("Loading a random game...");
            GameUtils.loadNextGame();
        } else {
            gameEngine.getLogger().info("Game rotation is empty, entering idle state.");
            EngineAPI.setServerState(ServerState.IDLE);
        }

        TitleBarRunnable runnable = new TitleBarRunnable();
        runnable.runTaskTimerAsynchronously(EngineAPI.getGameEngine(), 0, 20);
        gameEngine.getLogger().info("Loading complete.");
    }

    public static GameStartingRunnable getGameStartingRunnable() {
        return gameStartingRunnable;
    }

    public static void setGameStartingRunnable(GameStartingRunnable gameStartingRunnable) {
        EngineAPI.gameStartingRunnable = gameStartingRunnable;
    }

    public static GUIItem getCosmeticsItem() {
        return cosmeticsItem;
    }

    public static GUIItem getKitItem() {
        return kitItem;
    }

    public static GUIItem getLobbyItem() {
        return lobbyItem;
    }

    public static GUIItem getPrefsItem() {
        return prefsItem;
    }

    public static GUIItem getTeamItem() {
        return teamItem;
    }

    public static void setNextGame(GameInfo nextGame) {
        EngineAPI.nextGame = nextGame;
    }

    public static GameInfo getNextGame() {
        return nextGame;
    }

    public static GameMap getNextMap() {
        return nextMap;
    }

    public static GameVariation getNextVariation() {
        return nextVariation;
    }

    public static void setNextMap(GameMap nextMap) {
        EngineAPI.nextMap = nextMap;
    }

    public static void setNextVariation(GameVariation nextVariation) {
        EngineAPI.nextVariation = nextVariation;
    }

    public static boolean isAwaitingRestart() {
        return awaitingRestart;
    }

    public static void setAwaitingRestart(boolean awaitingRestart) {
        EngineAPI.awaitingRestart = awaitingRestart;
    }

    public static boolean isAwaitingMapReload() {
        return awaitingMapReload;
    }

    public static void setAwaitingMapReload(boolean awaitingMapReload) {
        EngineAPI.awaitingMapReload = awaitingMapReload;
    }

    public static void reloadMaps() {
        for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
            pl.sendMessage(TextFormatter.pluginMessage("Game Manager", "The server is currently updating its map register. Please wait..."));
        }
        setServerState(ServerState.RELOADING_MAPS);
        EngineAPI.maps.clear();
        gameEngine.getLogger().info("Downloading all live maps...");
        File zipFolder = new File(gameEngine.getDataFolder(), "zips");
        if (zipFolder.exists()) {
            try {
                FileUtils.deleteDirectory(zipFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        EngineDatabaseManager.downloadMaps();
        File[] zips = new File(gameEngine.getDataFolder(), "zips").listFiles();
        assert zips != null;

        gameEngine.getLogger().info(zips.length + " zips downloaded. Extracting maps...");
        File mapsFolder = new File(gameEngine.getDataFolder(), "maps");
        if (mapsFolder.exists()) {
            try {
                FileUtils.deleteDirectory(mapsFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mapsFolder.mkdirs();
        for (File zip : zips) {
            try {
                ZipUtil.unzip(zip.toPath().toAbsolutePath().toString(), mapsFolder.toPath().toAbsolutePath() + "/" + zip.getName().split("\\.")[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File[] maps = new File(gameEngine.getDataFolder(), "maps").listFiles();
        assert maps != null;

        gameEngine.getLogger().info(maps.length + " maps extracted. Loading map registry...");
        for (File map : maps) {
            File data = new File(map, "map.json");
            JSONParser parser = new JSONParser();
            Object object;
            JSONObject jsonObject;
            try {
                FileReader fileReader = new FileReader(data);
                object = parser.parse(fileReader);
                jsonObject = new JSONObject(((org.json.simple.JSONObject)  object).toJSONString());
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                gameEngine.getLogger().info("Map loading for a map failed, skipping...");
                continue;
            }

            String gameType = jsonObject.getString("game_type");
            int id = Integer.parseInt(map.getName().split("\\.")[0]);
            String name = jsonObject.getString("name");
            String author = jsonObject.getString("author");
            if (EngineAPI.maps.containsKey(gameType)) {
                EngineAPI.maps.get(gameType).getMaps().add(new GameMap(map, id, name, author, jsonObject));
            } else {
                MapRegistry registry = new MapRegistry(gameType);
                registry.getMaps().add(new GameMap(map, id, name, author, jsonObject));
                EngineAPI.maps.put(gameType, registry);
            }
        }
        new BukkitRunnable(){
            @Override
            public void run() {
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
                if (EngineAPI.getServerState() != ServerState.STARTING && EngineAPI.getActiveGame() != null && EngineAPI.getGameStartingRunnable() == null) {
                    if (ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && !((AuroraMCGamePlayer)player1).isOptedSpec()).count() >= ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("min_players")) {
                        EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30, false));
                        EngineAPI.getGameStartingRunnable().runTaskTimer(ServerAPI.getCore(), 0, 20);
                    }
                }

                for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
                    pl.sendMessage(TextFormatter.pluginMessage("Game Manager", "Maps have finished loading! Play will now continue!"));
                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
                    if (EngineAPI.getActiveGame() != null) {
                        player.getInventory().setItem(0, EngineAPI.getKitItem().getItemStack());
                        if (EngineAPI.getActiveGame().getTeams().size() > 1 && !EngineAPI.getActiveGameInfo().hasTeamCommand() && !EngineAPI.isTeamBalancingEnabled()) {
                            player.getInventory().setItem(1, EngineAPI.getTeamItem().getItemStack());
                        }
                    }
                }
            }
        }.runTask(ServerAPI.getCore());
    }

    public static void setRestartType(String restartType) {
        EngineAPI.restartType = restartType;
    }

    public static String getRestartType() {
        return restartType;
    }

    public static boolean isTeamBalancingEnabled() {
        return teamBalancingEnabled;
    }

    public static void setTeamBalancingEnabled(boolean teamBalancingEnabled) {
        EngineAPI.teamBalancingEnabled = teamBalancingEnabled;
    }

    public static Map<Integer, Reward> getKitLevelRewards() {
        return kitLevelRewards;
    }

    public static float getXpBoostMultiplier() {
        return xpBoostMultiplier;
    }

    public static String getXpBoostMessage() {
        return xpBoostMessage;
    }

    public static void setXpBoostMessage(String xpBoostMessage) {
        EngineAPI.xpBoostMessage = xpBoostMessage;
    }

    public static void setXpBoostMultiplier(float xpBoostMultiplier) {
        EngineAPI.xpBoostMultiplier = xpBoostMultiplier;
    }
}
