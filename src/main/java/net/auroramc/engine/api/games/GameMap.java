/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api.games;

import net.auroramc.engine.api.EngineAPI;
import org.bukkit.Location;
import org.json.JSONObject;

import java.io.File;

public class GameMap {

    private final File regionFolder;
    private final int id;
    private final String name;
    private final String author;
    private final String game;
    private final JSONObject mapData;
    private final int lowX,highX,lowY,highY,lowZ,highZ;

    public GameMap(File regionFolder, int id, String name, String author, String game, JSONObject mapData) {
        this.regionFolder = regionFolder;
        this.id = id;
        this.name = name;
        this.author = author;
        this.mapData = mapData;
        this.game = game;

        JSONObject a = mapData.getJSONObject("border_a");
        JSONObject b = mapData.getJSONObject("border_b");
        if (a.getInt("x") > b.getInt("x")) {
            highX = a.getInt("x");
            lowX = b.getInt("x");
        } else {
            highX = b.getInt("x");
            lowX = a.getInt("x");
        }

        if (a.getInt("y") > b.getInt("y")) {
            highY = a.getInt("y");
            lowY = b.getInt("y");
        } else {
            highY = b.getInt("y");
            lowY = a.getInt("y");
        }

        if (a.getInt("z") > b.getInt("z")) {
            highZ = a.getInt("z");
            lowZ = b.getInt("z");
        } else {
            highZ = b.getInt("z");
            lowZ = a.getInt("z");
        }
    }

    public File getRegionFolder() {
        return regionFolder;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public JSONObject getMapData() {
        return mapData;
    }

    public boolean isInBorder(Location location) {
        return location.getX() >= lowX && location.getX() <= highX && location.getY() >= lowY && location.getY() <= highY && location.getZ() >= lowZ && location.getZ() <= highZ;
    }

    public int getHighX() {
        return highX;
    }

    public int getHighY() {
        return highY;
    }

    public int getHighZ() {
        return highZ;
    }

    public int getLowX() {
        return lowX;
    }

    public int getLowY() {
        return lowY;
    }

    public int getLowZ() {
        return lowZ;
    }
}
