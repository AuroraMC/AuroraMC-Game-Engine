/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.core.api.utils.ZipUtil;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.MapRegistry;
import net.auroramc.engine.api.players.Reward;
import net.auroramc.engine.commands.*;
import net.auroramc.engine.commands.admin.*;
import net.auroramc.engine.commands.admin.game.CommandGame;
import net.auroramc.engine.listeners.*;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class AuroraMCGameEngine extends JavaPlugin {

    private static FileConfiguration maps;
    private static File mapsFile;

    @Override
    public void onEnable() {
        mapsFile = new File(getDataFolder(), "maps.yml");
        if (!mapsFile.exists()) {
            mapsFile.getParentFile().mkdirs();
            copy(getResource("maps.yml"), mapsFile);
        }

        maps = new YamlConfiguration();
        try {
            maps.load(mapsFile);
        } catch (Exception e) {
            AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
        }
        maps.options().copyHeader(true);
        getLogger().info("Loading AuroraMC Game Engine...");
        EngineAPI.init(this);

        getLogger().info("Downloading all live maps...");
        List<Integer> ints = EngineDatabaseManager.downloadMaps();
        File zips = new File(getDataFolder(), "zips");

        getLogger().info(ints.size() + " zips downloaded. Extracting maps...");
        File mapsFolder = new File(getDataFolder(), "maps");
        mapsFolder.mkdirs();
        for (int zip : ints) {
            File file = new File(zips, zip + ".zip");
            File dest = new File(mapsFolder.toPath().toAbsolutePath() + "/" + zip);
            if (dest.exists()) {
                try {
                    FileUtils.deleteDirectory(dest);
                } catch (IOException e) {
                    AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
                }
            }
            try {
                ZipUtil.unzip(file.toPath().toAbsolutePath().toString(), dest.toPath().toAbsolutePath().toString());
            } catch (IOException e) {
                AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
            }
        }
        File[] maps = mapsFolder.listFiles();
        assert maps != null;

        getLogger().info(ints.size() + " maps extracted. Removing old maps...");

        int i = 0;
        for (File map : maps) {
            String mapId = map.getName();
            if (AuroraMCGameEngine.maps.contains(mapId + ".load-code")) {
                if (!UUID.fromString(AuroraMCGameEngine.maps.getString(mapId + ".load-code")).equals(EngineAPI.getReloadCode())) {
                    map.delete();
                    i++;
                }
            } else {
                map.delete();
                i++;
            }
        }

        maps = mapsFolder.listFiles();

        getLogger().info(i + " maps removed. Loading map registry...");
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
                AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
                getLogger().info("Map loading for a map failed, skipping...");
                continue;
            }

            String gameType = jsonObject.getString("game_type");
            int id = Integer.parseInt(map.getName().split("\\.")[0]);
            String name = jsonObject.getString("name");
            String author = jsonObject.getString("author");
            String game = jsonObject.getString("game_type");
            if (EngineAPI.getMaps().containsKey(gameType)) {
                EngineAPI.getMaps().get(gameType).getMaps().add(new GameMap(map, id, name, author, game, jsonObject));
            } else {
                MapRegistry registry = new MapRegistry(gameType);
                registry.getMaps().add(new GameMap(map, id, name, author, game, jsonObject));
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
                AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
            }
        }

        getLogger().info("Waiting lobby copied. Registering listeners and commands...");
        Bukkit.getPluginManager().registerEvents(new JoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new LobbyListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
        Bukkit.getPluginManager().registerEvents(new ServerCloseRequestListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpectatorListener(), this);
        Bukkit.getPluginManager().registerEvents(new LeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new CosmeticListener(), this);

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
        AuroraMCAPI.registerCommand(new CommandKitXP());
        AuroraMCAPI.registerCommand(new CommandHub());
        AuroraMCAPI.registerCommand(new CommandXPBoost());
        AuroraMCAPI.registerCommand(new CommandLoadEvent());

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
        EngineAPI.getKitLevelRewards().put(11, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(12, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(13, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(14, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(15, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(16, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(17, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(18, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(19, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(20, new Reward("1000 Tickets;&bLevel 1 Kit Upgrade Available", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(21, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(22, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(23, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(24, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(25, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(26, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(27, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(28, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(29, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(30, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(31, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(32, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(33, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(34, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(35, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(36, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(37, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(38, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(39, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(40, new Reward("1000 Tickets;&bLevel 2 Kit Upgrade Available", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(41, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(42, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(43, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(44, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(45, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(46, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(47, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(48, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(49, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(50, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(51, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(52, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(53, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(54, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(55, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(56, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(57, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(58, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(59, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(60, new Reward("1000 Tickets;&bLevel 3 Kit Upgrade Available", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(61, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(62, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(63, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(64, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(65, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(66, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(67, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(68, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(69, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(70, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(71, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(72, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(73, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(74, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(75, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(76, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(77, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(78, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(79, new Reward("1000 Tickets;&bHalf Way There Kill Messages", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(80, new Reward("1000 Tickets;&bLevel 4 Kit Upgrade Available", 0, 1000, 0, Collections.emptyMap(), Collections.singletonList(501)));
        EngineAPI.getKitLevelRewards().put(81, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(82, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(83, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(84, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(85, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(86, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(87, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(88, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(89, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(90, new Reward("1000 Tickets;&bCosmetic Reward", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(91, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(92, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(93, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(94, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(95, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(96, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(97, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(98, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(99, new Reward("1000 Tickets", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));
        EngineAPI.getKitLevelRewards().put(100, new Reward("1000 Tickets;&bTwerk Apocalypse Win Effect;&bLevel 5 Kit Upgrade Available", 0, 1000, 0, Collections.emptyMap(), Collections.emptyList()));


        getLogger().info("Listeners registered. Waiting for games to be registered...");

    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
        }
    }

    public static FileConfiguration getMaps() {
        return maps;
    }

    public static File getMapsFile() {
        return mapsFile;
    }

}

