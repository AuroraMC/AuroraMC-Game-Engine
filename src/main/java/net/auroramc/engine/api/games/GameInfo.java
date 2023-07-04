/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
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
    private final Map<String, GameVariationInfo> variations;
    private final boolean teamCommand;


    public GameInfo(int id, String name, Class<? extends Game> gameClass, String description, String registryKey, boolean teamCommand, Map<String, GameVariationInfo> variations) {
        this.id = id;
        this.name = name;
        this.gameClass = gameClass;
        this.description = description;
        this.registryKey = registryKey;
        this.teamCommand = teamCommand;
        this.variations = variations;
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

    public Map<String, GameVariationInfo> getVariations() {
        return variations;
    }

    public boolean hasTeamCommand() {
        return teamCommand;
    }
}