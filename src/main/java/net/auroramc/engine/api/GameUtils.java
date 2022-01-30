/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api;

import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameInfo;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.GameVariation;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.VoidGenerator;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
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
            EngineAPI.setActiveGameInfo(gameInfo);
            Game game = gameInfo.getGameClass().getConstructor(GameVariation.class).newInstance(gameVariation);
            EngineAPI.setServerState(ServerState.LOADING_GAME);
            EngineAPI.setActiveGame(game);
            game.preLoad();
            EngineAPI.setServerState(ServerState.LOADING_MAP);

            if (EngineAPI.getMapWorld() != null) {
                Bukkit.unloadWorld(EngineAPI.getMapWorld(), false);
            }

            File file = new File(Bukkit.getWorldContainer(), "map_world/region");
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
            file.mkdirs();
            FileUtils.copyDirectory(map.getRegionFolder(), file);
            World world = Bukkit.createWorld(new WorldCreator("map_world").generator(new VoidGenerator(EngineAPI.getGameEngine())));
            EngineAPI.setMapWorld(world);
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
