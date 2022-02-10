/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.backend.communication.CommunicationUtils;
import net.auroramc.core.api.backend.communication.Protocol;
import net.auroramc.core.api.backend.communication.ProtocolMessage;
import net.auroramc.core.api.events.ProtocolMessageEvent;
import net.auroramc.core.api.events.ServerCloseRequestEvent;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerCloseRequestListener implements Listener {

    @EventHandler
    public void onServerCloseRequest(ServerCloseRequestEvent e) {
        if (e.isEmergency() || (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Server Manager", "This server is restarting" + ((!e.isEmergency())?" for an update":"") + ". You are being sent to a lobby."));
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Lobby");
                out.writeUTF(player.getUniqueId().toString());
                player.sendPluginMessage(AuroraMCAPI.getCore(), "BungeeCord", out.toByteArray());
            }
            //Wait 10 seconds, then close the server
            new BukkitRunnable(){
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.kickPlayer(AuroraMCAPI.getFormatter().pluginMessage("Server Manager", "This server is restarting.\n\nYou can reconnect to the network to continue playing!"));
                    }
                    AuroraMCAPI.setShuttingDown(true);
                    CommunicationUtils.sendMessage(new ProtocolMessage(Protocol.CONFIRM_SHUTDOWN, "Mission Control", e.getType(), AuroraMCAPI.getServerInfo().getName(), AuroraMCAPI.getServerInfo().getNetwork().name()));
                }
            }.runTaskLater(AuroraMCAPI.getCore(), 200);
        } else {
            //Set that it is awaiting a restart, then restart when the game is over.
            EngineAPI.setAwaitingRestart(true);
            EngineAPI.setRestartType(e.getType());
        }
    }

    @EventHandler
    public void onProtocolMessage(ProtocolMessageEvent e) {
        if (e.getMessage().getProtocol() == Protocol.UPDATE_PLAYER_COUNT) {
            EngineAPI.setXpBoostMessage(EngineDatabaseManager.getXpMessage());
            EngineAPI.setXpBoostMultiplier(EngineDatabaseManager.getXpMultiplier());
        }
    }

}
