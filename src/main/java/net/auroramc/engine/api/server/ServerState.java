/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api.server;

public enum ServerState {

    STARTING_UP("Starting up"),
    IDLE("Idle"),
    RELOADING_MAPS("Reloading Maps"),
    PREPARING_GAME("Preparing Game"),
    WAITING_FOR_PLAYERS("Ready"),
    STARTING("Starting"),
    IN_GAME ("In-Game"),
    ENDING("Ending");

    private String name;

    ServerState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
