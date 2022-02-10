/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.players;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.cosmetics.Cosmetic;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.stats.PlayerStatistics;
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

    public void apply(AuroraMCPlayer player) {
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
                        AuroraMCAPI.getDbManager().addCosmetic(player.getPlayer().getUniqueId(), cosmetic);
                    }
                }.runTaskAsynchronously(AuroraMCAPI.getCore());

                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("CosmeticAdd");
                out.writeUTF(player.getPlayer().getName());
                out.writeInt(cosmeticId);
                player.getPlayer().sendPluginMessage(AuroraMCAPI.getCore(),"auroramc:server", out.toByteArray());
            }
        }
    }

    public void apply(int id, PlayerStatistics statistics) {
        statistics.addXp(this.xp, true);
        statistics.addTicketsEarned(this.tickets, true);
        statistics.addCrownsEarned(this.crowns, true);
        for (Map.Entry<Integer, Integer> entry : achievements.entrySet()) {
            statistics.achievementGained(AuroraMCAPI.getAchievement(entry.getKey()), entry.getValue(), true);
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
                }.runTaskAsynchronously(AuroraMCAPI.getCore());
            }
        }
    }

    @Override
    public String toString() {
        return rewardString;
    }
}
