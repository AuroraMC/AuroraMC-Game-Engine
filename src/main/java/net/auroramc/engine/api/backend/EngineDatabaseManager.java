package net.auroramc.engine.api.backend;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.engine.api.EngineAPI;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

}
