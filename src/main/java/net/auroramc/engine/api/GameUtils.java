/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.*;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.VoidGenerator;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;

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
            EngineAPI.setActiveGameInfo(gameInfo);
            Game game = gameInfo.getGameClass().getConstructor(GameVariation.class).newInstance(gameVariation);
            EngineAPI.setServerState(ServerState.PREPARING_GAME);
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
            EngineAPI.setServerState(ServerState.WAITING_FOR_PLAYERS);
            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
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
                            player.getScoreboard().setLine(6, kit.getName() + " ");
                            Kit finalKit = kit;
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Your kit was set to **" + finalKit.getName() + "**."));
                                }
                            }.runTask(AuroraMCAPI.getCore());
                        }
                    }.runTaskAsynchronously(AuroraMCAPI.getCore());
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadNextGame() {
        GameInfo gameInfo = EngineAPI.getGameRotation().get(EngineAPI.randomNumber(EngineAPI.getGameRotation().size()));
        GameUtils.loadGame(gameInfo);
    }

}
