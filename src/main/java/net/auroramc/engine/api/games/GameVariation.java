/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import net.auroramc.engine.api.players.AuroraMCGamePlayer;

import java.util.List;

public abstract class GameVariation<T extends Game> {

    private final T game;

    public GameVariation(T game) {
        this.game = game;
    }

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

    public T getGame() {
        return game;
    }

    public abstract void balanceTeams();
}
