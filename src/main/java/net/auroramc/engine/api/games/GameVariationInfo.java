/*
 * Copyright (c) 2023-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.games;


import org.bukkit.Material;

public class GameVariationInfo {

    private final int id;
    private final String name;
    private final Class<? extends GameVariation> variationClass;
    private final String description;
    private final String registryKey;
    private final Material item;
    private final short data;

    public GameVariationInfo(int id, String name, Class<? extends GameVariation> variationClass, String description, String registryKey, Material item, short data) {
        this.id = id;
        this.name = name;
        this.variationClass = variationClass;
        this.description = description;
        this.registryKey = registryKey;
        this.item = item;
        this.data = data;
    }


    public String getRegistryKey() {
        return registryKey;
    }

    public String getDescription() {
        return description;
    }

    public Class<? extends GameVariation> getVariationClass() {
        return variationClass;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Material getItem() {
        return item;
    }

    public short getData() {
        return data;
    }
}
