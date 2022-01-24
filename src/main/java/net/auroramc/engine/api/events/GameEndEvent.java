/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.events;

import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameVariation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEndEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final Game game;
    private final GameVariation gameVariation;

    public GameEndEvent(Game game, GameVariation gameVariation) {
        this.game = game;
        this.gameVariation = gameVariation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlersList() {
        return handlerList;
    }

    public Game getGame() {
        return game;
    }

    public GameVariation getGameVariation() {
        return gameVariation;
    }
}
