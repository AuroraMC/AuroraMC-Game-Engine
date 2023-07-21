/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api.events;

import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameVariation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameLoadEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final Game game;
    private final GameVariation gameVariation;

    public GameLoadEvent(Game game, GameVariation gameVariation) {
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
