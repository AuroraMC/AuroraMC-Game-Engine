package net.auroramc.engine.api.games;

import java.util.List;

public abstract class Game {

    int id;
    String name;

    public Game(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public abstract void preLoad();

    public abstract void load(Map map);

    public abstract void start();

    public abstract void end();

    public abstract List<Kit> getKits();

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
