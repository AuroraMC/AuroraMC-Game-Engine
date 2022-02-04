/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.players;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.util.SpectatorUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public class AuroraMCGamePlayer extends AuroraMCPlayer {

    private boolean spectator;
    private Kit kit;
    private Map<Integer, List<Integer>> unlockedKits;

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
            getPlayer().spigot().setCollidesWithEntities(false);
            if (isDead) {
                SpectatorUtil.giveItems(this);
            }
        } else {
            getPlayer().spigot().setCollidesWithEntities(true);
        }
        this.dead = isDead;

    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public long getJoinTimestamp() {
        return joinTimestamp;
    }

    public Map<Integer, List<Integer>> getUnlockedKits() {
        return unlockedKits;
    }
}
