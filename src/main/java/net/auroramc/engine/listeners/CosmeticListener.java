/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.cosmetics.Cosmetic;
import net.auroramc.core.api.cosmetics.ProjectileTrail;
import net.auroramc.core.api.players.AuroraMCPlayer;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class CosmeticListener implements Listener {

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player && !(e.getEntity() instanceof FishHook)) {
            AuroraMCPlayer player = AuroraMCAPI.getPlayer((Player)e.getEntity().getShooter());
            if (player.getActiveCosmetics().containsKey(Cosmetic.CosmeticType.PROJECTILE_TRAIL)) {
                ((ProjectileTrail)player.getActiveCosmetics().get(Cosmetic.CosmeticType.PROJECTILE_TRAIL)).onShoot(e.getEntity());
            }
        }
    }

}
