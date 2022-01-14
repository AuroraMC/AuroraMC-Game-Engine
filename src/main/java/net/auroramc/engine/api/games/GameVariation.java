package net.auroramc.engine.api.games;

import net.auroramc.engine.api.players.AuroraMCGamePlayer;

import java.util.List;

public abstract class GameVariation {

    private final String name;
    private int id;
    private final String registryKey;

    public GameVariation(String name, int id, String registryKey) {
        this.id = id;
        this.name = name;
        this.registryKey = registryKey;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRegistryKey() {
        return registryKey;
    }

    public abstract boolean preLoad();

    public abstract boolean load(GameMap map);

    public abstract boolean start();

    public abstract boolean end();

    public abstract boolean onPlayerJoin(AuroraMCGamePlayer player);

    public abstract List<Kit> getKits();
}
