package net.auroramc.engine.listeners;

import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

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
