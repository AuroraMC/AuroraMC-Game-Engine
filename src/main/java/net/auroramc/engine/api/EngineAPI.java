package net.auroramc.engine.api;

import net.auroramc.engine.AuroraMCGameEngine;
import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.Map;
import net.auroramc.engine.api.server.ServerState;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class EngineAPI {

    private static AuroraMCGameEngine gameEngine;
    private static final List<Class<? extends Game>> games;
    private static Map waitingLobbyMap;
    private static ServerState serverState;
    private static Game activeGame;
    private static Map activeMap;

    static {
        games = new ArrayList<>();
        serverState = ServerState.STARTING_UP;
    }

    public static void init(AuroraMCGameEngine gameEngine) {
        EngineAPI.gameEngine = gameEngine;
    }

    public static List<Class<? extends Game>> getGames() {
        return games;
    }

    public static void registerGame(Class<? extends Game> game) {
        EngineAPI.games.add(game);
    }

    public static Class<? extends Game> getGame(String name) {
        for (Class<? extends Game> game : games) {
            if (game.getSimpleName().equalsIgnoreCase(name)) {
               return game;
            }
        }
        return null;
    }

    public static Game initiateGame(String name) {
        for (Class<? extends Game> game : games) {
            if (game.getSimpleName().equalsIgnoreCase(name)) {
                try {
                    return game.getConstructor().newInstance();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    public static Map getWaitingLobbyMap() {
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

    public static Map getActiveMap() {
        return activeMap;
    }

    public static void setActiveGame(Game activeGame) {
        EngineAPI.activeGame = activeGame;
    }

    public static void setActiveMap(Map activeMap) {
        EngineAPI.activeMap = activeMap;
    }
}
