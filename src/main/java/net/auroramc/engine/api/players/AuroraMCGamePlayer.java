/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.players;

import net.auroramc.api.cosmetics.Gadget;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.GameRewards;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.util.SpectatorUtil;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuroraMCGamePlayer extends AuroraMCServerPlayer {

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
    private final Map<Gadget, Long> lastUsed;

    public AuroraMCGamePlayer(AuroraMCServerPlayer oldPlayer) {
        super(oldPlayer);
        spectator = isVanished();
        kit = null;
        optedSpec = false;
        joinTimestamp = System.currentTimeMillis();
        lastHitAt = -1;
        lastHitBy = null;
        latestHits = new HashMap<>();
        gameData = new HashMap<>();
        lastUsed = new HashMap<>();
        new BukkitRunnable(){
            @Override
            public void run() {
                unlockedKits = EngineDatabaseManager.getUnlockedKits(oldPlayer.getId());
            }
        }.runTaskAsynchronously(ServerAPI.getCore());
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator, boolean isDead) {
        this.spectator = spectator;
        if (spectator) {
            this.hidden = true;
            setCollidesWithEntities(false);
            setAllowFlight(true);
            setFlying(true);
            setGameMode(GameMode.SURVIVAL);
            setHealth(20);
            setFoodLevel(30);
            for (PotionEffect effect : getActivePotionEffects()) {
                removePotionEffect(effect.getType());
            }
            if (isDead) {
                getInventory().clear();
                getInventory().setArmorContents(new ItemStack[4]);
                SpectatorUtil.giveItems(this);
                setExp(0);
                setLevel(0);
                getEnderChest().clear();
            }
        } else {
            this.hidden = false;
            setCollidesWithEntities(true);
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

    public Map<Gadget, Long> getLastUsed() {
        return lastUsed;
    }
}
