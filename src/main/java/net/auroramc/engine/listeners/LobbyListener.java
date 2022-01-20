package net.auroramc.engine.listeners;

import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.util.Vector;
import org.json.JSONArray;

/**
 * All of these listeners will take over the second the game ends or when the server is in the lobby.
 */
public class LobbyListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getEntity() instanceof Player) {
                if (e.getCause() == EntityDamageEvent.DamageCause.VOID && EngineAPI.getServerState() != ServerState.ENDING) {
                    JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("players");
                    int x, y, z;
                    x = spawnLocations.getJSONObject(0).getInt("x");
                    y = spawnLocations.getJSONObject(0).getInt("y");
                    z = spawnLocations.getJSONObject(0).getInt("z");
                    float yaw = spawnLocations.getJSONObject(0).getFloat("yaw");
                    e.getEntity().teleport(new Location(Bukkit.getWorld("world"), x, y, z, yaw, 0));
                    e.getEntity().setFallDistance(0);
                    e.getEntity().setVelocity(new Vector());
                } else if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                    e.getEntity().setFireTicks(0);
                }
                e.setCancelled(true);

            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getEntity() instanceof Player && e.getFoodLevel() < 25) {
                e.setCancelled(true);
                e.setFoodLevel(30);
            }
        }
    }

}
