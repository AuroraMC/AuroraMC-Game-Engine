/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.players;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.utils.LevelUtils;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.Kit;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerKitLevel {

    private final AuroraMCGamePlayer player;
    private final int gameId;
    private final int kitId;
    private int level;
    private long xpIntoLevel;
    private long totalXpEarned;
    private short latestUpgrade;

    public PlayerKitLevel(AuroraMCGamePlayer player, int gameId, int kitId, int level, long xpIntoLevel, long totalXpEarned, short latestUpgrade) {
        this.player = player;
        this.gameId = gameId;
        this.kitId = kitId;
        this.level = level;
        this.xpIntoLevel = xpIntoLevel;
        this.totalXpEarned = totalXpEarned;
        this.latestUpgrade = latestUpgrade;
    }

    public int getGameId() {
        return gameId;
    }

    public int getKitId() {
        return kitId;
    }

    public int getLevel() {
        return level;
    }

    public long getTotalXpEarned() {
        return totalXpEarned;
    }

    public long getXpIntoLevel() {
        return xpIntoLevel;
    }

    public void addXp(Kit kit, long amount) {
        this.xpIntoLevel += amount;
        this.totalXpEarned += amount;
        boolean levelUp = false;
        if (this.level < 100 && this.xpIntoLevel >= LevelUtils.xpForLevel((this.level + 1))) {
            do {
                ++this.level;
                this.xpIntoLevel -= LevelUtils.xpForLevel((long)this.level);
                if (EngineAPI.getKitLevelRewards().containsKey(this.level)) {
                    EngineAPI.getKitLevelRewards().get(this.level).apply(player);
                }
            } while(this.xpIntoLevel >= LevelUtils.xpForLevel((long)(this.level + 1)) && this.level < 100);

            levelUp = true;
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                EngineDatabaseManager.setKitLevel(player.getId(), gameId, kitId, level, xpIntoLevel, totalXpEarned, latestUpgrade);
            }
        }.runTaskAsynchronously(AuroraMCAPI.getCore());
        if (levelUp) {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You just levelled up kit **" + kit.getName() + "** to level **" + level + "**!"));
        }
    }

    public void upgrade() {
        latestUpgrade++;
        new BukkitRunnable(){
            @Override
            public void run() {
                EngineDatabaseManager.setKitLevel(player.getId(), gameId, kitId, level, xpIntoLevel, totalXpEarned, latestUpgrade);
            }
        }.runTaskAsynchronously(AuroraMCAPI.getCore());
    }

    public short getLatestUpgrade() {
        return latestUpgrade;
    }
}
