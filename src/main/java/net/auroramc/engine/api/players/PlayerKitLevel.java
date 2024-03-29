/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.players;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.utils.LevelUtils;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.Kit;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerKitLevel {

    private final AuroraMCGamePlayer player;
    private final int id;
    private final int gameId;
    private final int kitId;
    private int level;
    private long xpIntoLevel;
    private long totalXpEarned;
    private short latestUpgrade;

    public PlayerKitLevel(AuroraMCGamePlayer player, int gameId, int kitId, int level, long xpIntoLevel, long totalXpEarned, short latestUpgrade) {
        this.player = player;
        this.id = player.getId();
        this.gameId = gameId;
        this.kitId = kitId;
        this.level = level;
        this.xpIntoLevel = xpIntoLevel;
        this.totalXpEarned = totalXpEarned;
        this.latestUpgrade = latestUpgrade;
    }

    public PlayerKitLevel(int player, int gameId, int kitId, int level, long xpIntoLevel, long totalXpEarned, short latestUpgrade) {
        this.player = null;
        this.id = player;
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
                this.xpIntoLevel -= LevelUtils.xpForLevel(this.level);
                if (player != null) {
                    if (EngineAPI.getKitLevelRewards().containsKey(this.level)) {
                        EngineAPI.getKitLevelRewards().get(this.level).apply(player);
                    }
                } else {
                    if (EngineAPI.getKitLevelRewards().containsKey(this.level)) {
                        EngineAPI.getKitLevelRewards().get(this.level).apply(id, AuroraMCAPI.getDbManager().getStatistics(AuroraMCAPI.getDbManager().getUUIDFromID(id), id));
                    }
                }
            } while(this.xpIntoLevel >= LevelUtils.xpForLevel(this.level + 1) && this.level < 100);

            levelUp = true;
        }
        if (!AuroraMCAPI.isTestServer()) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    EngineDatabaseManager.setKitLevel(id, gameId, kitId, level, xpIntoLevel, totalXpEarned, latestUpgrade);
                }
            }.runTaskAsynchronously(ServerAPI.getCore());
        }

        if (levelUp && player != null) {
            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You just levelled up kit **" + kit.getName() + "** to level **" + level + "**!"));
        }
    }

    public void removeXP(long amount) {
        totalXpEarned -= amount;

        if (amount > xpIntoLevel) {
            do {
                level--;
                amount -= xpIntoLevel;
                xpIntoLevel = LevelUtils.xpForLevel(level + 1);
            } while (amount > xpIntoLevel && LevelUtils.xpForLevel(level) != -1);
        }
        xpIntoLevel -= amount;

        if (level / 20 < latestUpgrade) {
            latestUpgrade = (short)(level / 20);
        }

        if (!AuroraMCAPI.isTestServer()) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    EngineDatabaseManager.setKitLevel(id, gameId, kitId, level, xpIntoLevel, totalXpEarned, latestUpgrade);
                }
            }.runTaskAsynchronously(ServerAPI.getCore());
        }
    }

    public void upgrade() {
        latestUpgrade++;
        if (!AuroraMCAPI.isTestServer()) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    EngineDatabaseManager.setKitLevel(id, gameId, kitId, level, xpIntoLevel, totalXpEarned, latestUpgrade);
                }
            }.runTaskAsynchronously(ServerAPI.getCore());
        }
    }

    public short getLatestUpgrade() {
        return latestUpgrade;
    }
}
