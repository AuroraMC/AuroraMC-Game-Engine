/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.backend;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.players.PlayerKitLevel;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EngineDatabaseManager {

    public static void downloadMaps() {
        try (Connection connection = AuroraMCAPI.getDbManager().getMySQLConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM maps WHERE parse_version = 'LIVE'");
            ResultSet set = statement.executeQuery();
            File file = new File(EngineAPI.getGameEngine().getDataFolder(), "zips");
            if (file.exists()) {
                FileUtils.deleteDirectory(file);
            }
            file.mkdirs();
            while (set.next()) {
                File zipFile = new File(file, set.getInt(1) + ".zip");
                FileOutputStream output = new FileOutputStream(zipFile);

                System.out.println("Writing to file " + zipFile.getAbsolutePath());
                InputStream input = set.getBinaryStream(6);
                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    output.write(buffer);
                }
                output.flush();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void uploadGameSession(UUID uuid, String game, JSONObject json) {
        try (Connection connection = AuroraMCAPI.getDbManager().getMySQLConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO game_session(game_uuid, game, server, game_data) VALUES (?,?,?,?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, game);
            statement.setString(3, AuroraMCAPI.getServerInfo().getName());
            statement.setString(4, json.toString());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getDefaultKit(int amcId, int gameId) {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            if (connection.hexists("defaultkits." + amcId, gameId + "")) {
                return Integer.parseInt(connection.hget("defaultkits." + amcId, gameId + ""));
            } else {
                return 0;
            }
        }
    }

    public static void setDefaultKit(int amcId, int gameId, int kitId) {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            connection.hset("defaultkits." + amcId, gameId + "", kitId + "");
        }
    }

    public static Map<Integer, List<Integer>> getUnlockedKits(int amcId) {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            Map<String, String> kits = connection.hgetAll("unlockedkits." + amcId);
            Map<Integer, List<Integer>> unlockedKits = new HashMap<>();
            for (Map.Entry<String, String> entry : kits.entrySet()) {
                List<Integer> unlockedGameKits = new ArrayList<>();
                String[] gameKits = entry.getValue().split(";");
                for (String gameKit : gameKits) {
                    unlockedGameKits.add(Integer.parseInt(gameKit));
                }
                unlockedKits.put(Integer.parseInt(entry.getKey()), unlockedGameKits);
            }
            return unlockedKits;
        }
    }

    public static void setUnlockedKits(int amcId, int gameId, List<Integer> kits) {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            List<String> kit = new ArrayList<>();
            for (int i : kits) {
                kit.add(i + "");
            }
            connection.hset("unlockedkits." + amcId, gameId + "", String.join(";", kit));
        }
    }

    public static void setKitLevel(int amcId, int gameId, int kitId, int level, long xpIntoLevel, long totalXpEarned, short latestUpgrade) {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            connection.hset("kitlevel." + amcId, gameId + "." + kitId, level + ";" + xpIntoLevel + ";" + totalXpEarned + ";" + latestUpgrade);
        }
    }

    public static PlayerKitLevel getKitLevel(AuroraMCGamePlayer player, int gameId, int kitId) {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            String level = connection.hget("kitlevel." + player.getId(), gameId + "." + kitId);
            if (level != null) {
                String[] split = level.split(";");
                return new PlayerKitLevel(player, gameId, kitId, Integer.parseInt(split[0]), Long.parseLong(split[1]), Long.parseLong(split[2]), Short.parseShort(split[3]));
            } else {
                return new PlayerKitLevel(player, gameId, kitId, 0, 0L, 0L, (short)0);
            }
        }
    }

}
