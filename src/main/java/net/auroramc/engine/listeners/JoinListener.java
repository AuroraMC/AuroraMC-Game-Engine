package net.auroramc.engine.listeners;

import net.auroramc.core.api.events.player.PlayerObjectCreationEvent;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONArray;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
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

    }

    @EventHandler
    public void onObjectCreate(PlayerObjectCreationEvent e) {
        e.setPlayer(new AuroraMCGamePlayer(e.getPlayer()));
    }

}
