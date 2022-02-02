/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.util;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;

public class TitleBarRunnable extends BukkitRunnable {

    @Override
    public void run() {
        if (EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) {
            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                player.sendHotBar("§7" + EngineAPI.getActiveGame().getGameSession().getUuid().toString() + " - " + AuroraMCAPI.getServerInfo().getName() + " - " + new Date(), ChatColor.GRAY, false);
            }
        } else {
            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                player.sendHotBar("Players: §b" + AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished()).count() + "§r/§b" + AuroraMCAPI.getServerInfo().getServerType().getInt("max_players") + " - Lobby: §b" + EngineAPI.getWaitingLobbyMap().getName() + " §rby §b" + EngineAPI.getWaitingLobbyMap().getAuthor(), ChatColor.WHITE, false);
            }
        }
    }
}
