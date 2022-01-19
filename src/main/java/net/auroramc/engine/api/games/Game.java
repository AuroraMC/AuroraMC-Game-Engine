package net.auroramc.engine.api.games;

import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.players.Team;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Game {

    protected GameVariation gameVariation;
    protected GameMap map;
    protected Map<String, Team> teams;


    public Game(GameVariation gameVariation) {
        this.gameVariation = gameVariation;
        this.teams = new HashMap<>();
    }

    public abstract void preLoad();

    public abstract void load(GameMap map);

    /**
     * When executed by the Game Engine, this indicates that the Engine is handing over control to the game and that the game is now started.
     */
    public abstract void start();

    /**
     * When executed by the game, it should indicate that the game is handing control back to the Game Engine and the game is no longer in progress.
     */
    public void end(AuroraMCPlayer winner) {

    }

    /**
     * When executed by the game, it should indicate that the game is handing control back to the Game Engine and the game is no longer in progress.
     */
    public void end(Team winner, String winnerName) {

    }

    public abstract void onPlayerJoin(Player player);

    public abstract void onPlayerJoin(AuroraMCGamePlayer player);

    public abstract List<Kit> getKits();

    public Map<String, Team> getTeams() {
        return teams;
    }
}
