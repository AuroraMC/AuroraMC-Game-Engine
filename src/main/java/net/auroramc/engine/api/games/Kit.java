/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

public abstract class Kit {

    private final int id;
    private final int gameId;
    private final String name;
    private final String description;

    public Kit(int id, int gameId, String name, String description) {
        this.id = id;
        this.gameId = gameId;
        this.name = name;
        this.description = description;
    }

    public abstract void onGameStart();

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
}
