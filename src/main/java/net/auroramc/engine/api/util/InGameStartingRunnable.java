/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.util;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class InGameStartingRunnable extends BukkitRunnable {

    private int i;
    private final Game game;

    public InGameStartingRunnable(Game game) {
        i = 10;
        this.game = game;
    }

    @Override
    public void run() {
        if (i > 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game", String.format("The game is starting in **%s** second%s!", i, ((i > 1)?"s":""))));
                if (i < 6) {
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 2f-(1.5f*(i/5f)));
                }
            }
            i--;
        } else {
            game.inProgress();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game", "The game has begun!"));
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 100, 2);
                AuroraMCPlayer pl = AuroraMCAPI.getPlayer(player);
                if (pl instanceof AuroraMCGamePlayer && !((AuroraMCGamePlayer) pl).isSpectator() && !pl.isVanished()) {
                    ((AuroraMCGamePlayer) pl).gameStarted();
                }
            }
            this.cancel();
        }
    }
}
