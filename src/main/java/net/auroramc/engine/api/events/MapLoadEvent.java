/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api.events;

import net.auroramc.engine.api.games.GameMap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MapLoadEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final GameMap map;

    public MapLoadEvent(GameMap map) {
        this.map = map;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlersList() {
        return handlerList;
    }

    public GameMap getMap() {
        return map;
    }

}
