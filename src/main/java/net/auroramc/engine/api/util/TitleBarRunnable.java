/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.util;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.backend.info.ServerInfo;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;

public class TitleBarRunnable extends BukkitRunnable {

    @Override
    public void run() {
        if (EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) {
            for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                player.sendHotBar(new TextComponent("§7" + EngineAPI.getActiveGame().getGameSession().getUuid().toString() + " - " + AuroraMCAPI.getInfo().getName() + " - " + new Date()));
            }
        } else {
            for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                player.sendHotBar(new TextComponent("Players: §b" + ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && (player1 instanceof AuroraMCGamePlayer && !((AuroraMCGamePlayer) player1).isOptedSpec())).count() + "§r/§b" + ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("max_players") + "§r - Lobby: §b" + EngineAPI.getWaitingLobbyMap().getName() + " §rby §b" + EngineAPI.getWaitingLobbyMap().getAuthor()));
            }
        }

        EngineDatabaseManager.updateServerData();
    }
}
