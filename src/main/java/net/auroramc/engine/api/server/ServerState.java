package net.auroramc.engine.api.server;

public enum ServerState {

    STARTING_UP("Starting up"),
    IDLE("Idle"),
    RELOADING_MAPS("Reloading Maps"),
    LOADING_GAME("Loading Game"),
    LOADING_MAP("Loading Map"),
    WAITING_FOR_PLAYERS("Waiting for players"),
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
