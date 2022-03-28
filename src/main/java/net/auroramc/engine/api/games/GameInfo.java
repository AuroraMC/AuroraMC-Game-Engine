/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import java.util.HashMap;
import java.util.Map;

public class GameInfo {

    private final int id;
    private final String name;
    private final Class<? extends Game> gameClass;
    private final String description;
    private final String registryKey;
    private final Map<String, GameVariation> variations;
    private final boolean teamCommand;
    private final String version;


    public GameInfo(int id, String name, Class<? extends Game> gameClass, String description, String registryKey, boolean teamCommand, String version) {
        this.id = id;
        this.name = name;
        this.gameClass = gameClass;
        this.description = description;
        this.registryKey = registryKey;
        this.teamCommand = teamCommand;
        this.variations = new HashMap<>();
        this.version = version;
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

    public boolean hasTeamCommand() {
        return teamCommand;
    }

    public String getVersion() {
        return version;
    }
}
