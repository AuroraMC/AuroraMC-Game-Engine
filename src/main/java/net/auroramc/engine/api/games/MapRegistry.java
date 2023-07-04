/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api.games;

import java.util.ArrayList;
import java.util.List;

public class MapRegistry {

    private final String game;
    private final List<GameMap> maps;

    public MapRegistry(String game) {
        this.game = game;
        this.maps = new ArrayList<>();
    }

    public List<GameMap> getMaps() {
        return maps;
    }

    public GameMap getMap(String mapName) {
        for (GameMap map : maps) {
            if (map.getName().replace(" ","").equalsIgnoreCase(mapName)) {
                return map;
            }
        }
        return null;
    }

    public String getGame() {
        return game;
    }
}
