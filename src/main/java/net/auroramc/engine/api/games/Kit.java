/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import net.auroramc.core.api.players.AuroraMCPlayer;
import org.bukkit.Material;

import java.util.Objects;

/**
 * The amount of kits per game is hard capped at 28 to prevent the kit GUI from breaking.
 */
public abstract class Kit {

    private final int id;
    private final int gameId;
    private final String name;
    private final String description;
    private final Material material;
    private final int cost;

    public Kit(int id, int gameId, String name, String description, Material material, int cost) {
        this.id = id;
        this.gameId = gameId;
        this.name = name;
        this.description = description;
        this.material = material;
        this.cost = cost;
    }

    public abstract void onGameStart(AuroraMCPlayer player);

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getGameId() {
        return gameId;
    }

    public String getDescription() {
        return description;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kit kit = (Kit) o;
        return id == kit.id && gameId == kit.gameId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gameId);
    }

    public abstract String getUpgradeReward(int level);
}
