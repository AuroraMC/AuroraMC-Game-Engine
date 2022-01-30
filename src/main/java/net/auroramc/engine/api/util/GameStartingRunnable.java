/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.util;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class GameStartingRunnable extends BukkitRunnable {

    private int startTime;

    public GameStartingRunnable(int startTime) {
        this.startTime = startTime;
    }

    @Override
    public void run() {
        for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
            player.getScoreboard().setTitle("&3-= &b&l&nSTARTING IN " + startTime + "&r &3=-");
            switch(startTime) {
                case 5:
                case 3:
                case 2:
                case 1:
                    player.getPlayer().closeInventory();
                case 60:
                case 30:
                case 10:
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.NOTE_PLING, 100, 1);
            }
        }

        if (startTime == 0) {
            EngineAPI.getActiveGame().start();
            EngineAPI.setServerState(ServerState.IN_GAME);
            EngineAPI.setGameStartingRunnable(null);
            this.cancel();
            return;
        }
        startTime--;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }
}
