/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api;

import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameInfo;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.GameVariation;
import net.auroramc.engine.api.server.ServerState;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
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
            File file = new File(EngineAPI.getMapWorld().getWorldFolder(), "region");
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
            file.mkdirs();
            FileUtils.copyDirectory(map.getRegionFolder(), file);
            game.load(map);
            EngineAPI.setActiveMap(map);
            EngineAPI.setServerState(ServerState.WAITING_FOR_PLAYERS);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadNextGame() {
        GameInfo gameInfo = EngineAPI.getGameRotation().get(EngineAPI.randomNumber(EngineAPI.getGameRotation().size()));
        GameUtils.loadGame(gameInfo);
    }

}
