/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.util.VoidGenerator;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.json.JSONObject;

public class WorldListener implements Listener {

    @EventHandler
    public void onWorldInit(WorldInitEvent e) {
        ((CraftWorld)e.getWorld()).getHandle().generator = new VoidGenerator(EngineAPI.getGameEngine());
        if (e.getWorld().getName().equalsIgnoreCase("world")) {
            e.getWorld().setGameRuleValue("doMobSpawning", "false");
            e.getWorld().setGameRuleValue("doDaylightCycle", "false");
            e.getWorld().setGameRuleValue("doFireTick", "false");
            e.getWorld().setGameRuleValue("randomTickSpeed", "0");
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        if (e.getWorld().getName().equalsIgnoreCase("world")) {
            e.getWorld().setGameRuleValue("doMobSpawning", "false");
            e.getWorld().setGameRuleValue("doDaylightCycle", "false");
            e.getWorld().setGameRuleValue("randomTickSpeed", "0");
            JSONObject mapData = EngineAPI.getWaitingLobbyMap().getMapData();
            if (mapData.has("time")) {
                e.getWorld().setTime(mapData.getInt("time"));
            } else {
                e.getWorld().setTime(6000);
            }
            e.getWorld().setThundering(false);
            e.getWorld().setStorm(false);

        }
    }
}
