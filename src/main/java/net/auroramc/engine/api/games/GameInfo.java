package net.auroramc.engine.api.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameInfo {

    private final int id;
    private final String name;
    private final Class<? extends Game> gameClass;
    private final String description;
    private final String registryKey;
    private final Map<String, GameVariation> variations;


    public GameInfo(int id, String name, Class<? extends Game> gameClass, String description, String registryKey) {
        this.id = id;
        this.name = name;
        this.gameClass = gameClass;
        this.description = description;
        this.registryKey = registryKey;
        this.variations = new HashMap<>();
    }


    public String getRegistryKey() {
        return registryKey;
    }

    public String getDescription() {
        return description;
    }

    public Class<? extends Game> getGameClass() {
        return gameClass;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Map<String, GameVariation> getVariations() {
        return variations;
    }
}
