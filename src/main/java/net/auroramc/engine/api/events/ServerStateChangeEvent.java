/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
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
