/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.events.player.PlayerLeaveEvent;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LeaveListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerLeaveEvent e) {
        if (!e.getPlayer().isVanished()) {
            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Quit", String.format("**%s** has left the server.", e.getPlayer().getPlayer().getName())));
            }
        }
        if (EngineAPI.getServerState() == ServerState.STARTING || EngineAPI.getGameStartingRunnable() != null) {
            if (AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && !((AuroraMCGamePlayer)player1).isSpectator()).count() < AuroraMCAPI.getServerInfo().getServerType().getInt("min_players")) {
                EngineAPI.getGameStartingRunnable().cancel();
                EngineAPI.setGameStartingRunnable(null);
                for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Server Manager", "A player has left the game so there are no longer enough players to start the game!"));
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
        }
    }

}
