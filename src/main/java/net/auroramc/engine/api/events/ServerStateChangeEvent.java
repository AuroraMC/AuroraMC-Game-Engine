/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.events;

import net.auroramc.engine.api.server.ServerState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerStateChangeEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final ServerState state;

    public ServerStateChangeEvent(ServerState state) {
        this.state = state;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public ServerState getState() {
        return state;
    }

}
