package net.auroramc.engine.api;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.engine.AuroraMCGameEngine;
import net.auroramc.engine.api.events.ServerStateChangeEvent;
import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameInfo;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.MapRegistry;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.Bukkit;
import org.bukkit.World;

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

    private static final Random random;

    static {
        games = new HashMap<>();
        maps = new HashMap<>();
        gameRotation = new ArrayList<>();
        serverState = ServerState.STARTING_UP;

        random = new Random();

        activeGame = null;
        activeGameInfo = null;
        activeMap = null;
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
        for (Object object : AuroraMCAPI.getServerInfo().getServerType().getJSONArray("rotation")) {
            String string = (String) object;
            gameRotation.add(games.get(string));
        }
        gameEngine.getLogger().info(EngineAPI.getGameRotation().size() + " games loaded into rotation.");
        if (EngineAPI.getGameRotation().size() > 0) {
            gameEngine.getLogger().info("Loading a random game...");
            GameInfo gameInfo = EngineAPI.getGameRotation().get(EngineAPI.randomNumber(EngineAPI.getGameRotation().size()));
            GameUtils.loadGame(gameInfo, null);
        } else {
            gameEngine.getLogger().info("Map world generated. Game rotation is empty, entering idle state.");
            EngineAPI.setServerState(ServerState.IDLE);
        }

    }
}
