/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.players;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.cosmetics.Cosmetic;
import net.auroramc.api.stats.PlayerStatistics;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Reward {

    private final String rewardString;
    private final int xp;
    private final long tickets;
    private final long crowns;
    private final Map<Integer, Integer> achievements;
    private final List<Integer> cosmetics;


    public Reward(String rewardString, int xp, long tickets, long crowns, Map<Integer, Integer> achievements, List<Integer> cosmetics) {
        this.rewardString = rewardString;
        this.xp = xp;
        this.tickets = tickets;
        this.crowns = crowns;
        this.achievements = achievements;
        this.cosmetics = cosmetics;
    }

    public int getXp() {
        return xp;
    }

    public Map<Integer, Integer> getAchievements() {
        return achievements;
    }

    public List<Integer> getCosmetics() {
        return cosmetics;
    }

    public long getCrowns() {
        return crowns;
    }

    public long getTickets() {
        return tickets;
    }

    public String getRewardString() {
        return rewardString;
    }

    public void apply(AuroraMCServerPlayer player) {
        player.getStats().addXp(this.xp, true);
        player.getBank().addCrowns(this.crowns, true, true);
        player.getBank().addTickets(this.tickets, true, true);
        for (Map.Entry<Integer, Integer> entry : achievements.entrySet()) {
            player.getStats().achievementGained(AuroraMCAPI.getAchievement(entry.getKey()), entry.getValue(), true);
        }
        for (int cosmeticId : cosmetics) {
            Cosmetic cosmetic = AuroraMCAPI.getCosmetics().get(cosmeticId);
            if (!player.getUnlockedCosmetics().contains(cosmetic)) {
                player.getUnlockedCosmetics().add(cosmetic);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        AuroraMCAPI.getDbManager().addCosmetic(player.getUniqueId(), cosmetic);
                    }
                }.runTaskAsynchronously(ServerAPI.getCore());

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("CosmeticAdd");
                out.writeUTF(player.getName());
                out.writeInt(cosmeticId);
                player.sendPluginMessage(out.toByteArray());
            }
        }
    }

    public void apply(int id, PlayerStatistics statistics) {
        statistics.addXp(this.xp, false);
        statistics.addTicketsEarned(this.tickets, false);
        statistics.addCrownsEarned(this.crowns, false);
        for (Map.Entry<Integer, Integer> entry : achievements.entrySet()) {
            statistics.achievementGained(AuroraMCAPI.getAchievement(entry.getKey()), entry.getValue(), false);
        }
        UUID uuid = AuroraMCAPI.getDbManager().getUUIDFromID(id);
        AuroraMCAPI.getDbManager().crownsAdded(uuid, this.crowns);
        AuroraMCAPI.getDbManager().ticketsAdded(uuid, this.tickets);
        List<Cosmetic> cosmetics = AuroraMCAPI.getDbManager().getUnlockedCosmetics(uuid);
        for (int cosmeticId : this.cosmetics) {
            Cosmetic cosmetic = AuroraMCAPI.getCosmetics().get(cosmeticId);
            if (!cosmetics.contains(cosmetic)) {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        AuroraMCAPI.getDbManager().addCosmetic(uuid, cosmetic);
                    }
                }.runTaskAsynchronously(ServerAPI.getCore());
            }
        }
    }

    @Override
    public String toString() {
        return rewardString;
    }
}
