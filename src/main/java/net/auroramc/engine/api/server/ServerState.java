/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
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
