package net.auroramc.engine.listeners;

import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.util.VoidGenerator;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {

    @EventHandler
    public void onWorldInit(WorldInitEvent e) {
        ((CraftWorld)e.getWorld()).getHandle().generator = new VoidGenerator(EngineAPI.getGameEngine());
        if (e.getWorld().getName().equalsIgnoreCase("world")) {
            e.getWorld().setGameRuleValue("doMobSpawning", "false");
            e.getWorld().setGameRuleValue("doDaylightCycle", "false");
        }
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        if (e.getWorld().getName().equalsIgnoreCase("world")) {
            e.getWorld().setGameRuleValue("doMobSpawning", "false");
            e.getWorld().setGameRuleValue("doDaylightCycle", "false");
            e.getWorld().setTime(6000);
        }
    }
}
