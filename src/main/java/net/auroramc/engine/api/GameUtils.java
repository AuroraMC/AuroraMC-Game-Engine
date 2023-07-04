/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api;

import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.*;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.VoidGenerator;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameUtils {

    public static void loadGame(GameInfo info) {
        GameUtils.loadGame(info, null);
    }

    public static void loadGame(GameInfo gameInfo, GameVariationInfo gameVariation) {
        List<GameMap> maps;
        if (gameInfo.getRegistryKey() != null) {
            if (gameVariation != null && gameVariation.getRegistryKey() != null) {
                maps = EngineAPI.getMaps().get(gameVariation.getRegistryKey()).getMaps();
            } else {
                maps = EngineAPI.getMaps().get(gameInfo.getRegistryKey()).getMaps();
            }
        } else {
            int key = EngineAPI.randomNumber(EngineAPI.getMaps().keySet().size());
            maps = EngineAPI.getMaps().get(new ArrayList<>(EngineAPI.getMaps().keySet()).get(key)).getMaps();
        }
        GameMap map = maps.get(EngineAPI.randomNumber(maps.size()));;
        GameUtils.loadGame(gameInfo, map, gameVariation);
    }

    public static void loadGame(GameInfo gameInfo, GameMap map, GameVariationInfo gameVariation) {
        try {
            EngineAPI.setServerState(ServerState.PREPARING_GAME);
            EngineAPI.setActiveGameInfo(gameInfo);
            Game game = gameInfo.getGameClass().getConstructor(GameVariation.class).newInstance(gameVariation);
            EngineAPI.setActiveGame(game);
            game.preLoad();

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
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doMobSpawning", "false");
            world.setGameRuleValue("doFireTick", "false");
            world.setGameRuleValue("randomTickSpeed", "0");
            if (map.getMapData().has("time")) {
                world.setTime(map.getMapData().getInt("time"));
            }
            EngineAPI.setMapWorld(world);
            game.load(map);
            EngineAPI.setActiveMap(map);
            for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                if (!player.isVanished() && !((AuroraMCGamePlayer)player).isSpectator()) {
                    AuroraMCGamePlayer gp = (AuroraMCGamePlayer) player;
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            Kit kit = game.getKits().get(0);
                            int id = EngineDatabaseManager.getDefaultKit(player.getId(), gameInfo.getId());
                            for (Kit kit2 : game.getKits()) {
                                if (kit2.getId() == id) {
                                    kit = kit2;
                                }
                            }
                            gp.setKit(kit);
                            player.getScoreboard().setLine(6, ChatColor.stripColor(kit.getName()) + " ");
                            Kit finalKit = kit;
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Your kit was set to **" + TextFormatter.convert(finalKit.getName()) + "**."));
                                }
                            }.runTask(ServerAPI.getCore());
                        }
                    }.runTaskAsynchronously(ServerAPI.getCore());
                }
            }
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
