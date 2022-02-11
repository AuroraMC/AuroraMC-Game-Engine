/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.utils.ZipUtil;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.MapRegistry;
import net.auroramc.engine.api.players.Reward;
import net.auroramc.engine.commands.CommandDisguiseOverride;
import net.auroramc.engine.commands.CommandMap;
import net.auroramc.engine.commands.CommandSpectator;
import net.auroramc.engine.commands.CommandUndisguiseOverride;
import net.auroramc.engine.commands.admin.CommandEffect;
import net.auroramc.engine.commands.admin.CommandGameMode;
import net.auroramc.engine.commands.admin.CommandGive;
import net.auroramc.engine.commands.admin.CommandMob;
import net.auroramc.engine.commands.admin.game.CommandGame;
import net.auroramc.engine.commands.admin.CommandTeleport;
import net.auroramc.engine.listeners.*;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;

public class AuroraMCGameEngine extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Loading AuroraMC Game Engine...");
        EngineAPI.init(this);

        getLogger().info("Downloading all live maps...");
        EngineDatabaseManager.downloadMaps();
        File[] zips = new File(getDataFolder(), "zips").listFiles();
        assert zips != null;

        getLogger().info(zips.length + " zips downloaded. Extracting maps...");
        File mapsFolder = new File(getDataFolder(), "maps");
        if (mapsFolder.exists()) {
            try {
                FileUtils.deleteDirectory(mapsFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mapsFolder.mkdirs();
        for (File zip : zips) {
            try {
                ZipUtil.unzip(zip.toPath().toAbsolutePath().toString(), mapsFolder.toPath().toAbsolutePath() + "/" + zip.getName().split("\\.")[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File[] maps = new File(getDataFolder(), "maps").listFiles();
        assert maps != null;

        getLogger().info(maps.length + " maps extracted. Loading map registry...");
        for (File map : maps) {
            File data = new File(map, "map.json");
            JSONParser parser = new JSONParser();
            Object object;
            JSONObject jsonObject;
            try {
                FileReader fileReader = new FileReader(data);
                object = parser.parse(fileReader);
                jsonObject = new JSONObject(((org.json.simple.JSONObject)  object).toJSONString());
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                getLogger().info("Map loading for a map failed, skipping...");
                continue;
            }

            String gameType = jsonObject.getString("game_type");
            int id = Integer.parseInt(map.getName().split("\\.")[0]);
            String name = jsonObject.getString("name");
            String author = jsonObject.getString("author");
            if (EngineAPI.getMaps().containsKey(gameType)) {
                EngineAPI.getMaps().get(gameType).getMaps().add(new GameMap(map, id, name, author, jsonObject));
            } else {
                MapRegistry registry = new MapRegistry(gameType);
                registry.getMaps().add(new GameMap(map, id, name, author, jsonObject));
                EngineAPI.getMaps().put(gameType, registry);
            }
        }

        getLogger().info("Maps loaded. Copying waiting lobby...");
        if (EngineAPI.getMaps().containsKey("WAITING_LOBBY")) {
            GameMap map = EngineAPI.getMaps().get("WAITING_LOBBY").getMaps().get(0);
            EngineAPI.setWaitingLobbyMap(map);
            try {
                File file = new File(Bukkit.getWorldContainer(), "world/region");

                if (file.exists()) {
                    FileUtils.deleteDirectory(file);
                }
                file.mkdirs();
                FileUtils.copyDirectory(map.getRegionFolder(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getLogger().info("Waiting lobby copied. Registering listeners and commands...");
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new LobbyListener(), this);
        Bukkit.getPluginManager().registerEvents(new PingListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
        Bukkit.getPluginManager().registerEvents(new ServerCloseRequestListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpectatorListener(), this);
        Bukkit.getPluginManager().registerEvents(new LeaveListener(), this);

        AuroraMCAPI.registerCommand(new CommandUndisguiseOverride());
        AuroraMCAPI.registerCommand(new CommandDisguiseOverride());
        AuroraMCAPI.registerCommand(new CommandGame());
        AuroraMCAPI.registerCommand(new CommandGameMode());
        AuroraMCAPI.registerCommand(new CommandGive());
        AuroraMCAPI.registerCommand(new CommandMob());
        AuroraMCAPI.registerCommand(new CommandMap());
        AuroraMCAPI.registerCommand(new CommandEffect());
        AuroraMCAPI.registerCommand(new CommandTeleport());
        AuroraMCAPI.registerCommand(new CommandSpectator());

        EngineAPI.getKitLevelRewards().put(1, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(2, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(3, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(4, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(5, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(6, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(7, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(8, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(9, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(10, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));


        getLogger().info("Listeners registered. Waiting for games to be registered...");

    }

}

