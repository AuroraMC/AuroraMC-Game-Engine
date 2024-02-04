/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.backend.communication.CommunicationUtils;
import net.auroramc.core.api.backend.communication.Protocol;
import net.auroramc.core.api.backend.communication.ProtocolMessage;
import net.auroramc.core.api.events.server.ProtocolMessageEvent;
import net.auroramc.core.api.events.server.ServerCloseRequestEvent;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
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
            if (Bukkit.getOnlinePlayers().size() == 0) {
                //No-one is online, kill the server now.
                AuroraMCAPI.setShuttingDown(true);
                CommunicationUtils.sendMessage(new ProtocolMessage(Protocol.CONFIRM_SHUTDOWN, "Mission Control", e.getType(), AuroraMCAPI.getInfo().getName(), AuroraMCAPI.getInfo().getNetwork().name()));
            } else {
                for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                    player.sendMessage(TextFormatter.pluginMessage("Server Manager", "This server is restarting" + ((!e.isEmergency())?" for an update":"") + ". You are being sent to a lobby."));
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Lobby");
                    out.writeUTF(player.getUniqueId().toString());
                    player.sendPluginMessage(out.toByteArray());
                }
                //Wait 10 seconds, then close the server
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.kickPlayer(TextFormatter.pluginMessageRaw("Server Manager", "This server is restarting.\n\nYou can reconnect to the network to continue playing!"));
                        }
                        AuroraMCAPI.setShuttingDown(true);
                        CommunicationUtils.sendMessage(new ProtocolMessage(Protocol.CONFIRM_SHUTDOWN, "Mission Control", e.getType(), AuroraMCAPI.getInfo().getName(), AuroraMCAPI.getInfo().getNetwork().name()));
                    }
                }.runTaskLater(ServerAPI.getCore(), 200);
            }
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
        } else if (e.getMessage().getProtocol() == Protocol.UPDATE_MAPS) {
            if (EngineAPI.getServerState() == ServerState.IDLE || EngineAPI.getServerState() == ServerState.WAITING_FOR_PLAYERS) {
                EngineAPI.setActiveGameInfo(null);
                EngineAPI.setActiveGame(null);
                EngineAPI.setActiveMap(null);
                EngineAPI.setServerState(ServerState.IDLE);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        EngineAPI.reloadMaps();
                    }
                }.runTaskAsynchronously(ServerAPI.getCore());
            } else {
                EngineAPI.setAwaitingMapReload(true);
            }
        }
    }

}
