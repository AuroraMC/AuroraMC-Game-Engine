package net.auroramc.engine.api;

import java.util.List;

public class GameEngineAPI {

    private static List<Game> games;
    private static Map waitingLobbyMap;

    public static List<Game> getGames() {
        return games;
    }

    public static Map getWaitingLobbyMap() {
        return waitingLobbyMap;
    }
}
