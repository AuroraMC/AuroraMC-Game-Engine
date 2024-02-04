/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.events.block.BlockBreakEvent;
import net.auroramc.core.api.events.block.BlockPlaceEvent;
import net.auroramc.core.api.events.entity.EntityDamageByPlayerEvent;
import net.auroramc.core.api.events.player.PlayerDropItemEvent;
import net.auroramc.core.api.events.player.PlayerInteractEvent;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.core.gui.cosmetics.Cosmetics;
import net.auroramc.core.gui.preferences.Preferences;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.gui.PlayerTracker;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class SpectatorListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        AuroraMCGamePlayer pl = (AuroraMCGamePlayer) e.getPlayer();
        if (pl != null && (pl.isSpectator() || pl.isVanished())) {
            e.setCancelled(true);
            if (e.getItem() != null && e.getItem().getType() != Material.AIR) {
                switch (e.getItem().getType()) {
                    case EMERALD: {
                        e.setCancelled(true);
                        Cosmetics cosmetics = new Cosmetics(pl);
                        cosmetics.open(pl);
                        break;
                    }
                    case WOOD_DOOR: {
                        e.setCancelled(true);
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Lobby");
                        out.writeUTF(e.getPlayer().getUniqueId().toString());
                        e.getPlayer().sendPluginMessage(out.toByteArray());
                        break;
                    }
                    case REDSTONE_COMPARATOR: {
                        e.setCancelled(true);
                        Preferences prefs = new Preferences(pl);
                        prefs.open(pl);
                        break;
                    }
                    case COMPASS: {
                        e.setCancelled(true);
                        PlayerTracker tracker = new PlayerTracker(pl);
                        tracker.open(pl);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByPlayerEvent e) {
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) e.getPlayer();
            if (pl != null && (pl.isSpectator() || pl.isVanished())) {
                e.setCancelled(true);
            }
    }

    @EventHandler
    public void onTargetEvent(EntityTargetLivingEntityEvent e) {
        if (e.getTarget() instanceof Player) {
            Player player = (Player) e.getTarget();
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) ServerAPI.getPlayer(player);
            if (pl.isSpectator() || pl.isVanished()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        AuroraMCGamePlayer pl = (AuroraMCGamePlayer) e.getPlayer();
        if (pl.isSpectator() || pl.isVanished()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        AuroraMCGamePlayer pl = (AuroraMCGamePlayer) e.getPlayer();
        if (pl.isSpectator() || pl.isVanished()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        AuroraMCGamePlayer pl = (AuroraMCGamePlayer) e.getPlayer();
        if (pl.isSpectator() || pl.isVanished()) {
            e.setCancelled(true);
        }
    }


}
