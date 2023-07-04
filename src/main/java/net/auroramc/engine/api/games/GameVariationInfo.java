/*
 * Copyright (c) 2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api.games;


public class GameVariationInfo {

    private final int id;
    private final String name;
    private final Class<? extends GameVariation> variationClass;
    private final String description;
    private final String registryKey;

    public GameVariationInfo(int id, String name, Class<? extends GameVariation> variationClass, String description, String registryKey) {
        this.id = id;
        this.name = name;
        this.variationClass = variationClass;
        this.description = description;
        this.registryKey = registryKey;
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

}
