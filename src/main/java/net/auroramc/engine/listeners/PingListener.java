/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.engine.api.EngineAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class PingListener implements Listener {


    @EventHandler
    public void onPing(ServerListPingEvent e) {
        e.setMotd(AuroraMCAPI.getPlayers().stream().filter(player -> !player.isVanished()).count() + "/" + AuroraMCAPI.getServerInfo().getServerType().getInt("max_players") + ";" + ((EngineAPI.getActiveGameInfo()==null)?"None":EngineAPI.getActiveGameInfo().getName()) + ";" + ((EngineAPI.getActiveMap()==null)?"None":EngineAPI.getActiveMap().getName()));
    }


}
