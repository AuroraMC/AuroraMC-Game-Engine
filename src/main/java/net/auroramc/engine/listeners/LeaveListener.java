/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.listeners;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.backend.info.ServerInfo;
import net.auroramc.api.cosmetics.Cosmetic;
import net.auroramc.api.cosmetics.ServerMessage;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.events.player.PlayerLeaveEvent;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LeaveListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerLeaveEvent e) {
        if (!e.getPlayer().isLoaded()) {
            return;
        }
        if (!e.getPlayer().isVanished()) {
            ServerMessage message = ((ServerMessage)e.getPlayer().getActiveCosmetics().getOrDefault(Cosmetic.CosmeticType.SERVER_MESSAGE, AuroraMCAPI.getCosmetics().get(400)));
            for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
                player1.sendMessage(TextFormatter.pluginMessage("Leave", TextFormatter.convert(message.onLeave(player1, e.getPlayer()))));
            }
        }
        if (EngineAPI.getServerState() == ServerState.STARTING || EngineAPI.getGameStartingRunnable() != null && !EngineAPI.getGameStartingRunnable().isForced()) {
            if (ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && !((AuroraMCGamePlayer)player1).isOptedSpec()).count() < ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("min_players")) {
                if (EngineAPI.getGameStartingRunnable() != null) {
                    EngineAPI.getGameStartingRunnable().cancel();
                    EngineAPI.setGameStartingRunnable(null);
                }
                EngineAPI.setServerState(ServerState.WAITING_FOR_PLAYERS);
                for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                    player.sendMessage(TextFormatter.pluginMessage("Server Manager", "A player has left the game so there are no longer enough players to start the game!"));
                }
            }
        } else if (EngineAPI.getServerState() == ServerState.IN_GAME) {
            AuroraMCGamePlayer player = ((AuroraMCGamePlayer)e.getPlayer());
            EngineAPI.getActiveGame().onPlayerLeave((AuroraMCGamePlayer) e.getPlayer());
            if (player.getJoinTimestamp() > EngineAPI.getActiveGame().getGameSession().getStartTimestamp()) {
                //The player joined after the game started, go from when they joined.
                player.getStats().addGameTime(System.currentTimeMillis() - player.getJoinTimestamp(), true);
            } else {
                player.getStats().addGameTime(System.currentTimeMillis() - EngineAPI.getActiveGame().getGameSession().getStartTimestamp(), true);
            }
            if (!player.isOptedSpec() && !player.isVanished()) {
                if (player.getRewards() != null) {
                    player.getRewards().stop();
                    player.getRewards().apply(false);
                    player.getStats().incrementStatistic(EngineAPI.getActiveGameInfo().getId(), "gamesPlayed", 1, true);
                }
            }
        }
    }

}
