/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.players;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameRewards;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.util.SpectatorUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public class AuroraMCGamePlayer extends AuroraMCPlayer {

    private boolean spectator;
    private Kit kit;
    private PlayerKitLevel kitLevel;
    private Map<Integer, List<Integer>> unlockedKits;
    private GameRewards rewards;

    private final long joinTimestamp;

    public AuroraMCGamePlayer(AuroraMCPlayer oldPlayer) {
        super(oldPlayer);
        spectator = isVanished();
        kit = null;
        joinTimestamp = System.currentTimeMillis();
        new BukkitRunnable(){
            @Override
            public void run() {
                unlockedKits = EngineDatabaseManager.getUnlockedKits(oldPlayer.getId());
            }
        }.runTaskAsynchronously(AuroraMCAPI.getCore());
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator, boolean isDead) {
        this.spectator = spectator;
        if (spectator) {
            this.hidden = true;
            getPlayer().spigot().setCollidesWithEntities(false);
            getPlayer().setAllowFlight(true);
            getPlayer().setFlying(true);
            getPlayer().setGameMode(GameMode.SURVIVAL);
            getPlayer().setHealth(20);
            getPlayer().setFoodLevel(30);
            if (isDead) {
                getPlayer().getInventory().clear();
                SpectatorUtil.giveItems(this);
                getPlayer().setExp(0);
                getPlayer().setLevel(0);
                getPlayer().getEnderChest().clear();
            }
        } else {
            this.hidden = false;
            getPlayer().spigot().setCollidesWithEntities(true);
        }
        this.dead = isDead;

    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
        if (kit == null) {
            Bukkit.broadcastMessage("test " + getPlayer().getName());
            kitLevel = null;
        } else {
            Bukkit.broadcastMessage("test2 " + getPlayer().getName());
            kitLevel = EngineDatabaseManager.getKitLevel(this, kit.getGameId(), kit.getId());
        }
    }

    public PlayerKitLevel getKitLevel() {
        return kitLevel;
    }

    public long getJoinTimestamp() {
        return joinTimestamp;
    }

    public Map<Integer, List<Integer>> getUnlockedKits() {
        return unlockedKits;
    }

    public void gameStarted() {
        rewards = new GameRewards(this);
    }

    public GameRewards getRewards() {
        return rewards;
    }

    public void gameOver() {
        rewards = null;
    }
}
