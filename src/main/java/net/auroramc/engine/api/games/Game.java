package net.auroramc.engine.api.games;

import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class Game {

    protected GameVariation gameVariation;
    protected GameMap map;


    public Game(GameVariation gameVariation) {
        this.gameVariation = gameVariation;
    }

    public abstract void preLoad();

    public abstract void load(GameMap map);

    public abstract void start();

    public abstract void end();

    public abstract void onPlayerJoin(Player player);

    public abstract void onPlayerJoin(AuroraMCGamePlayer player);

    public abstract List<Kit> getKits();

}
