package net.auroramc.engine.api;

import net.auroramc.engine.AuroraMCGameEngine;
import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameInfo;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.MapRegistry;
import net.auroramc.engine.api.server.ServerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EngineAPI {

    private static AuroraMCGameEngine gameEngine;
    private static final Map<String, GameInfo> games;
    private static GameMap waitingLobbyMap;
    private static ServerState serverState;
    private static Game activeGame;
    private static GameMap activeMap;
    private static final Map<String, MapRegistry> maps;
    private static final List<GameInfo> gameRotation;

    static {
        games = new HashMap<>();
        maps = new HashMap<>();
        gameRotation = new ArrayList<>();
        serverState = ServerState.STARTING_UP;

        activeGame = null;
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

    public static void setServerState(ServerState serverState) {
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
}
