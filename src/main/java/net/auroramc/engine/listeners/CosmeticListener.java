/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.listeners;

import net.auroramc.api.cosmetics.Cosmetic;
import net.auroramc.api.cosmetics.ProjectileTrail;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import org.bukkit.entity.Egg;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class CosmeticListener implements Listener {

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player && !(e.getEntity() instanceof FishHook) && !(e.getEntity() instanceof Egg) && !(e.getEntity() instanceof Snowball)) {
            AuroraMCServerPlayer player = ServerAPI.getPlayer((Player)e.getEntity().getShooter());
            if (player.getActiveCosmetics().containsKey(Cosmetic.CosmeticType.PROJECTILE_TRAIL)) {
                ((ProjectileTrail)player.getActiveCosmetics().get(Cosmetic.CosmeticType.PROJECTILE_TRAIL)).onShoot(e.getEntity());
            }
        }
    }

}
