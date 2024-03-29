/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import net.auroramc.core.api.utils.gui.GUIItem;
import org.bukkit.Material;

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
    private final Material item;
    private final short data;


    public GameInfo(int id, String name, Class<? extends Game> gameClass, String description, String registryKey, boolean teamCommand, Map<String, GameVariationInfo> variations, Material item, short data) {
        this.id = id;
        this.name = name;
        this.gameClass = gameClass;
        this.description = description;
        this.registryKey = registryKey;
        this.teamCommand = teamCommand;
        this.variations = variations;
        this.item = item;
        this.data = data;
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

    public Material getItem() {
        return item;
    }

    public short getData() {
        return data;
    }
}
