/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
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
