/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.AuroraMCGameEngine;
import net.auroramc.engine.api.events.ServerStateChangeEvent;
import net.auroramc.engine.api.games.*;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import net.auroramc.engine.api.util.TitleBarRunnable;
import net.auroramc.engine.api.util.VoidGenerator;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;

import java.io.File;
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

    static {
        games = new HashMap<>();
        maps = new HashMap<>();
        gameRotation = new ArrayList<>();
        serverState = ServerState.STARTING_UP;

        random = new Random();

        activeGame = null;
        activeGameInfo = null;
        activeMap = null;

        lobbyItem = new GUIItem(Material.WOOD_DOOR, "&a&lReturn to Lobby");
        prefsItem = new GUIItem(Material.REDSTONE_COMPARATOR, "&a&lView Preferences");
        cosmeticsItem = new GUIItem(Material.EMERALD, "&a&lView Cosmetics");
        kitItem = new GUIItem(Material.CHEST, "&a&lSelect Kit");
        teamItem = new GUIItem(Material.BOOK, "&a&lSelect Team");
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
        Bukkit.getPluginManager().callEvent(new ServerStateChangeEvent(serverState));
        EngineAPI.serverState = serverState;
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
        world.setKeepSpawnInMemory(false);
        for (Chunk chunk : Arrays.asList(world.getLoadedChunks())) {
            world.unloadChunk(chunk);
        }
        EngineAPI.setMapWorld(world);

        gameEngine.getLogger().info("Map world loaded. Loading rotation...");

        for (Object object : AuroraMCAPI.getServerInfo().getServerType().getJSONArray("rotation")) {
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

    public static void setRestartType(String restartType) {
        EngineAPI.restartType = restartType;
    }

    public static String getRestartType() {
        return restartType;
    }
}
