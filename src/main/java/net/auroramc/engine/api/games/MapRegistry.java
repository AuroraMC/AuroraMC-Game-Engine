package net.auroramc.engine.api.games;

import java.util.ArrayList;
import java.util.List;

public class MapRegistry {

    private final String game;
    private final List<GameMap> maps;

    public MapRegistry(String game) {
        this.game = game;
        this.maps = new ArrayList<>();
    }

    public List<GameMap> getMaps() {
        return maps;
    }

    public String getGame() {
        return game;
    }
}
