/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import net.auroramc.engine.api.players.AuroraMCGamePlayer;

import java.util.List;

public abstract class GameVariation {

    private final Game game;

    public GameVariation(Game game) {
        this.game = game;
    }

    public abstract String getName();

    public abstract String getRegistryKey();

    public abstract boolean preLoad();

    public abstract boolean load(GameMap map);

    public abstract boolean start();

    public abstract void inProgress();

    public abstract boolean end();

    public abstract boolean onPlayerJoin(AuroraMCGamePlayer player);

    public abstract void onPlayerLeave(AuroraMCGamePlayer player);

    public abstract void onRespawn(AuroraMCGamePlayer player);

    public abstract boolean onDeath(AuroraMCGamePlayer player, AuroraMCGamePlayer killer);

    public abstract void onFinalKill(AuroraMCGamePlayer player);

    public Game getGame() {
        return game;
    }
}
