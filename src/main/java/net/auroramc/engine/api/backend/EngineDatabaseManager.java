/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.backend;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.GameSession;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class EngineDatabaseManager {

    public static void downloadMaps() {
        try (Connection connection = AuroraMCAPI.getDbManager().getMySQLConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM maps WHERE parse_version = 'LIVE'");
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                File file = new File(EngineAPI.getGameEngine().getDataFolder(), "zips");
                if (file.exists()) {
                    FileUtils.deleteDirectory(file);
                }
                file.mkdirs();
                file = new File(file, set.getInt(1) + ".zip");
                FileOutputStream output = new FileOutputStream(file);

                System.out.println("Writing to file " + file.getAbsolutePath());
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

}
