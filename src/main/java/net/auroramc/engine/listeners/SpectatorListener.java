/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpectatorListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        AuroraMCGamePlayer pl = (AuroraMCGamePlayer) AuroraMCAPI.getPlayer(e.getPlayer());
        if (pl != null && (pl.isSpectator() || pl.isVanished())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTargetEvent(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player) {
            Player player = (Player) e.getTarget();
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) AuroraMCAPI.getPlayer(player);
            if (pl.isSpectator() || pl.isVanished()) {
                e.setCancelled(true);
            }
        }
    }

}
