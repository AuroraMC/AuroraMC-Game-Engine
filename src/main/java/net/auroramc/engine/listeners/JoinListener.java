package net.auroramc.engine.listeners;

import net.auroramc.core.api.events.player.PlayerObjectCreationEvent;
import net.auroramc.core.api.players.PlayerScoreboard;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONArray;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
            JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("players");
            if (spawnLocations == null || spawnLocations.length() > 0) {
                EngineAPI.getGameEngine().getLogger().info("An invalid waiting lobby was supplied, assuming 0, 64, 0 spawn position.");
                e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), 0, 64, 0));
            } else {
                int x, y, z;
                x = spawnLocations.getJSONObject(0).getInt("x");
                y = spawnLocations.getJSONObject(0).getInt("y");
                z = spawnLocations.getJSONObject(0).getInt("z");
                e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), x, y, z));
            }
        } else if (EngineAPI.getActiveGame() != null) {
            EngineAPI.getActiveGame().onPlayerJoin(e.getPlayer());
        }
    }

    @EventHandler
    public void onObjectCreate(PlayerObjectCreationEvent e) {
        AuroraMCGamePlayer player = new AuroraMCGamePlayer(e.getPlayer());
        e.setPlayer(player);
        if ((EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) && EngineAPI.getActiveGame() != null) {
            EngineAPI.getActiveGame().onPlayerJoin(player);
        } else {
            PlayerScoreboard scoreboard = player.getScoreboard();
            scoreboard.setTitle("&3&l-= &b&l&n" + EngineAPI.getServerState().getName() + "&r &3&l=-");
            scoreboard.setLine(11, "&b&l«GAME»");
            scoreboard.setLine(10, ((EngineAPI.getActiveGameInfo() != null)?EngineAPI.getActiveGameInfo().getName():"None"));
            scoreboard.setLine(9, " ");
            scoreboard.setLine(8, "&b&l«MAP»");
            scoreboard.setLine(7, ((EngineAPI.getActiveMap() != null)?EngineAPI.getActiveMap().getName():"None"));
            scoreboard.setLine(6, "  ");
            scoreboard.setLine(5, "&b&l«KIT»");
            scoreboard.setLine(4, ((player.getKit() != null)?player.getKit().getName():"None"));
            scoreboard.setLine(3, "   ");
            scoreboard.setLine(2, "&b&l«TEAM»");
            scoreboard.setLine(1, ((player.getTeam() != null)?"&" + player.getTeam().getTeamColor() + "&l" + player.getTeam().getName():"None"));

        }
    }

}
