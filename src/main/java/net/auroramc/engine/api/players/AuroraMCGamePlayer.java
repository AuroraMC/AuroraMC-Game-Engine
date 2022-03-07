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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuroraMCGamePlayer extends AuroraMCPlayer {

    private boolean spectator;
    private Kit kit;
    private PlayerKitLevel kitLevel;
    private Map<Integer, List<Integer>> unlockedKits;
    private GameRewards rewards;

    private final long joinTimestamp;
    private boolean optedSpec;

    private AuroraMCGamePlayer lastHitBy;
    private long lastHitAt;

    private final Map<AuroraMCGamePlayer, Long> latestHits;

    private final Map<String, Object> gameData;

    public AuroraMCGamePlayer(AuroraMCPlayer oldPlayer) {
        super(oldPlayer);
        spectator = isVanished();
        kit = null;
        optedSpec = false;
        joinTimestamp = System.currentTimeMillis();
        lastHitAt = -1;
        lastHitBy = null;
        latestHits = new HashMap<>();
        gameData = new HashMap<>();
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
                getPlayer().getInventory().setArmorContents(new ItemStack[4]);
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

    public boolean isOptedSpec() {
        return optedSpec;
    }

    public void setOptedSpec(boolean optedSpec) {
        this.optedSpec = optedSpec;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
        if (kit == null) {
            kitLevel = null;
        } else {
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

    public AuroraMCGamePlayer getLastHitBy() {
        return lastHitBy;
    }

    public long getLastHitAt() {
        return lastHitAt;
    }

    public void setLastHitAt(long lastHitAt) {
        this.lastHitAt = lastHitAt;
    }

    public void setLastHitBy(AuroraMCGamePlayer lastHitBy) {
        this.lastHitBy = lastHitBy;
    }

    public Map<AuroraMCGamePlayer, Long> getLatestHits() {
        return latestHits;
    }

    public Map<String, Object> getGameData() {
        return gameData;
    }
}
