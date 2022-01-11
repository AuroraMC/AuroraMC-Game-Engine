package net.auroramc.engine;

import net.auroramc.core.api.utils.ZipUtil;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

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
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
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



    }

}
