/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api.backend;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.backend.info.ServerInfo;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.engine.AuroraMCGameEngine;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.GameSession;
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
import java.util.logging.Level;

public class EngineDatabaseManager {

    public static List<Integer> downloadMaps() {
        try (Connection connection = AuroraMCAPI.getDbManager().getMySQLConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM maps WHERE parse_version = " + ((AuroraMCAPI.isTestServer())?"'TEST'":"'LIVE'") + "AND game != 'PATHFINDER' AND game != 'DUELS'");
            ResultSet set = statement.executeQuery();
            File file = new File(EngineAPI.getGameEngine().getDataFolder(), "zips");
            file.mkdirs();
            List<Integer> ints = new ArrayList<>();
            while (set.next()) {
                File zipFile = new File(file, set.getInt(2) + ".zip");
                if (zipFile.exists()) {
                    if (AuroraMCGameEngine.getMaps().contains(set.getInt(2) +"")) {
                        int parseVersion = AuroraMCGameEngine.getMaps().getInt(set.getInt(2) + ".parse-number");
                        if (parseVersion >= set.getInt(6)) {
                            //We do not need to update the map, continue;
                            continue;
                        }
                    }
                    zipFile.delete();
                }
                FileOutputStream output = new FileOutputStream(zipFile);

                System.out.println("Writing to file " + zipFile.getAbsolutePath());
                InputStream input = set.getBinaryStream(7);
                byte[] buffer = new byte[1024];
                while (input.read(buffer) > 0) {
                    output.write(buffer);
                }
                output.flush();
                AuroraMCGameEngine.getMaps().set(set.getInt(2) + ".name", set.getString(3));
                AuroraMCGameEngine.getMaps().set(set.getInt(2) + ".author", set.getString(4));
                AuroraMCGameEngine.getMaps().set(set.getInt(2) + ".game", set.getString(5));
                AuroraMCGameEngine.getMaps().set(set.getInt(2) + ".parse-number", set.getString(6));
                AuroraMCGameEngine.getMaps().set(set.getInt(2) + ".load-code", EngineAPI.getReloadCode());
                AuroraMCGameEngine.getMaps().save(AuroraMCGameEngine.getMapsFile());
                ints.add(set.getInt(2));
            }
            return ints;
        } catch (SQLException | IOException e) {
            AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
            return Collections.emptyList();
        }
    }

    public static void uploadGameSession(UUID uuid, String game, JSONObject json, List<GameSession.GamePlayer> players) {
        try (Connection connection = AuroraMCAPI.getDbManager().getMySQLConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO game_session(game_uuid, game, server, game_data, players) VALUES (?,?,?,?,?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, ((game == null)?"EVENT":game));
            statement.setString(3, AuroraMCAPI.getInfo().getName());
            statement.setString(4, json.toString());
            List<String> ints = new ArrayList<>();
            for (GameSession.GamePlayer player : players) {
                ints.add(player.getAmcId() + "");
            }
            statement.setString(5, String.join(",", ints));
            statement.execute();
        } catch (SQLException e) {
            AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
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

    public static PlayerKitLevel getKitLevel(int player, int gameId, int kitId) {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            String level = connection.hget("kitlevel." + player, gameId + "." + kitId);
            if (level != null) {
                String[] split = level.split(";");
                return new PlayerKitLevel(player, gameId, kitId, Integer.parseInt(split[0]), Long.parseLong(split[1]), Long.parseLong(split[2]), Short.parseShort(split[3]));
            } else {
                return new PlayerKitLevel(player, gameId, kitId, 0, 0L, 0L, (short)0);
            }
        }
    }

    public static float getXpMultiplier() {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            if (connection.hexists("xpboost", "multiplier")) {
                return Float.parseFloat(connection.hget("xpboost", "multiplier"));
            } else {
                return 1;
            }
        }
    }

    public static String getXpMessage() {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            return connection.hget("xpboost", "message");
        }
    }

    public static void activateXpMultiplier(int days, float multiplier, String message  ) {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            connection.hset("xpboost", "multiplier", multiplier + "");
            connection.hset("xpboost", "message", message);
            connection.expire("xpboost", days * 86400);
        }
    }

    public static void updateServerData() {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            connection.set("serverdata." + AuroraMCAPI.getInfo().getNetwork().name() + "." + AuroraMCAPI.getInfo().getName(), EngineAPI.getServerState().name() + ";" + ServerAPI.getPlayers().stream().filter(player -> !player.isVanished() && (player instanceof AuroraMCGamePlayer && !((AuroraMCGamePlayer) player).isOptedSpec())).count() + "/" + ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("max_players") + ";" + ((EngineAPI.getActiveGameInfo()==null)?"None":EngineAPI.getActiveGameInfo().getName()) + ";" + ((EngineAPI.getActiveMap()==null)?"None":EngineAPI.getActiveMap().getName()));
            connection.expire("serverdata." + AuroraMCAPI.getInfo().getNetwork().name() + "." + AuroraMCAPI.getInfo().getName(), 15);
        }
    }

    public static void gameStarted() {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            connection.hincrBy(String.format("stat.gamesstarted.%s", EngineAPI.getActiveGameInfo().getRegistryKey()), "DAILY", 1);
            connection.hincrBy(String.format("stat.gamesstarted.%s", EngineAPI.getActiveGameInfo().getRegistryKey()), "WEEKLY", 1);
            connection.hincrBy(String.format("stat.gamesstarted.%s", EngineAPI.getActiveGameInfo().getRegistryKey()), "ALLTIME", 1);

            int amount = (int) ServerAPI.getPlayers().stream().filter(player -> !player.isVanished() && !((AuroraMCGamePlayer)player).isSpectator()).count();
            connection.hincrBy(String.format("stat.playerspergame.%s", EngineAPI.getActiveGameInfo().getRegistryKey()), "DAILY", amount);
            connection.hincrBy(String.format("stat.playerspergame.%s", EngineAPI.getActiveGameInfo().getRegistryKey()), "WEEKLY", amount);
            connection.hincrBy(String.format("stat.playerspergame.%s", EngineAPI.getActiveGameInfo().getRegistryKey()), "ALLTIME", amount);
        }
    }

    public static Map<String, String> getVersionNumbers() {
        try (Jedis connection = AuroraMCAPI.getDbManager().getRedisConnection()) {
            return connection.hgetAll("versionnumbers");
        }
    }

}
