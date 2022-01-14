package net.auroramc.engine.api;

import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameInfo;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.GameVariation;
import net.auroramc.engine.api.server.ServerState;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class GameUtils {

    public static void loadGame(GameInfo info) {
        GameUtils.loadGame(info, null);
    }

    public static void loadGame(GameInfo gameInfo, GameVariation gameVariation) {
            List<GameMap> maps = EngineAPI.getMaps().get(gameInfo.getRegistryKey()).getMaps();
            GameMap map = maps.get(EngineAPI.randomNumber(maps.size()));
            GameUtils.loadGame(gameInfo, map, gameVariation);
    }

    public static void loadGame(GameInfo gameInfo, GameMap map, GameVariation gameVariation) {
        try {
            Game game = gameInfo.getGameClass().getConstructor(GameVariation.class).newInstance(gameVariation);
            EngineAPI.setServerState(ServerState.LOADING_GAME);
            EngineAPI.setActiveGame(game);
            game.preLoad();
            EngineAPI.setServerState(ServerState.LOADING_MAP);
            game.load(map);
            EngineAPI.setActiveMap(map);
            EngineAPI.setServerState(ServerState.WAITING_FOR_PLAYERS);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

}
