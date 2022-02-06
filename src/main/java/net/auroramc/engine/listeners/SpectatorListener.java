/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.gui.cosmetics.Cosmetics;
import net.auroramc.core.gui.preferences.Preferences;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpectatorListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) AuroraMCAPI.getPlayer(e.getPlayer());
            if (pl != null && (pl.isSpectator() || pl.isVanished())) {
                e.setCancelled(true);
                if (e.getItem() != null && e.getItem().getType() != Material.AIR) {
                    switch (e.getItem().getType()) {
                        case EMERALD: {
                            e.setCancelled(true);
                            AuroraMCPlayer player = AuroraMCAPI.getPlayer(e.getPlayer());
                            Cosmetics cosmetics = new Cosmetics(player);
                            cosmetics.open(player);
                            AuroraMCAPI.openGUI(player, cosmetics);
                            break;
                        }
                        case WOOD_DOOR: {
                            e.setCancelled(true);
                            ByteArrayDataOutput out = ByteStreams.newDataOutput();
                            out.writeUTF("Lobby");
                            out.writeUTF(e.getPlayer().getUniqueId().toString());
                            e.getPlayer().sendPluginMessage(AuroraMCAPI.getCore(), "BungeeCord", out.toByteArray());
                            break;
                        }
                        case REDSTONE_COMPARATOR: {
                            e.setCancelled(true);
                            AuroraMCPlayer player = AuroraMCAPI.getPlayer(e.getPlayer());
                            Preferences prefs = new Preferences(player);
                            prefs.open(player);
                            AuroraMCAPI.openGUI(player, prefs);
                            break;
                        }
                    }
                }
            }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) AuroraMCAPI.getPlayer((Player) e.getDamager());
            if (pl != null && (pl.isSpectator() || pl.isVanished())) {
                e.setCancelled(true);
            }
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
