/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.Map;

public class GameRewards {

    private final AuroraMCGamePlayer player;
    private final long startTimestamp;
    private long stopTimestamp;
    private final Map<String, Integer> xp;
    private int totalXP;
    private int tickets;
    private int crowns;

    public GameRewards(AuroraMCGamePlayer player) {
        this.player = player;
        xp = new HashMap<>();
        tickets = 0;
        crowns = 0;
        totalXP = 0;
        startTimestamp = System.currentTimeMillis();
        stopTimestamp = -1;
    }

    public void addXp(String reason, int amount) {
        xp.put(reason, xp.getOrDefault(reason, 0) + amount);
        totalXP += amount;
    }

    public void addTickets(int amount) {
        tickets += amount;
    }

    public void addCrowns(int amount) {
        crowns += amount;
    }

    public void stop() {
        if (stopTimestamp == -1) {
            stopTimestamp = System.currentTimeMillis();
        }
    }

    public void apply(boolean message) {
        if (stopTimestamp < 0) {
            stopTimestamp = System.currentTimeMillis();
        }
        long totalMs = stopTimestamp - startTimestamp;
        long totalS = totalMs / 1000;
        int timeXp = 0;

        if (totalS > 60) {
            timeXp += 180;
            totalS -= 60;
            if (totalS > 120) {
                timeXp += 240;
                totalS -= 120;
                if (totalS > 600) {
                    timeXp += 420;
                } else {
                    timeXp += totalS;
                }
            } else {
                timeXp += totalS * 2;
            }
        } else {
            timeXp += totalS * 3;
        }

        double multiplier = 1;
        if (player.hasPermission("master")) {
            multiplier = 1.5;
        } else if (player.hasPermission("elite")) {
            multiplier = 1.25;
        }
        if (player.hasPermission("plus")) {
            multiplier += 1;
        }

        long totalXp = Math.round((this.totalXP + timeXp) * EngineAPI.getXpBoostMultiplier());

        long totalTickets = Math.round((tickets + timeXp)*multiplier);

        player.getStats().addXp(totalXp, true);
        player.getBank().addCrowns(crowns + timeXp, true, true);
        player.getBank().addTickets(totalTickets, true, true);


        if (message) {
            StringBuilder xpBreakdown = new StringBuilder();
            xpBreakdown.append("&r+");
            xpBreakdown.append(timeXp);
            xpBreakdown.append(" XP **Time Bonus**");
            for (Map.Entry<String, Integer> entry : xp.entrySet()) {
                xpBreakdown.append("\n&r+");
                xpBreakdown.append(entry.getValue());
                xpBreakdown.append(" XP **");
                xpBreakdown.append(entry.getKey());
                xpBreakdown.append("**");
            }

            TextComponent textComponent = new TextComponent("");

            TextComponent lines = new TextComponent("▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆▆");
            lines.setBold(true);
            lines.setColor(ChatColor.DARK_AQUA);
            textComponent.addExtra(lines);

            TextComponent rewardsBreakdown = new TextComponent("\nGame Rewards");
            rewardsBreakdown.setBold(true);
            rewardsBreakdown.setColor(ChatColor.AQUA);
            textComponent.addExtra(rewardsBreakdown);

            textComponent.addExtra("\n \n");

            TextComponent xp = new TextComponent("+" + totalXp + " XP");
            xp.setColor(ChatColor.GREEN);
            xp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(AuroraMCAPI.getFormatter().convert(AuroraMCAPI.getFormatter().highlight(xpBreakdown.toString()))).create()));

            if (EngineAPI.getXpBoostMessage() != null) {
                xp.addExtra(" " + EngineAPI.getXpBoostMessage() + "\n");
            } else {
                xp.addExtra("\n");
            }

            textComponent.addExtra(xp);

            xp = new TextComponent("+" + ((totalXP + timeXp)*4) + " Kit XP\n");
            xp.setColor(ChatColor.DARK_GREEN);
            textComponent.addExtra(xp);

            TextComponent crowns = new TextComponent("+" + (this.crowns + timeXp) + " Crowns\n");
            crowns.setColor(ChatColor.GOLD);
            textComponent.addExtra(crowns);

            TextComponent tickets = new TextComponent("+" + totalTickets + " Tickets");
            if (player.hasPermission("master")) {
                tickets.setColor(ChatColor.LIGHT_PURPLE);
                tickets.addExtra(" (" + multiplier + "x Multiplier)");
            } else if (player.hasPermission("elite")) {
                tickets.setColor(ChatColor.AQUA);
                tickets.addExtra(" (" + multiplier + "x Multiplier)");
            } else {
                if (multiplier > 1) {
                    tickets.addExtra(" (" + multiplier + "x Multiplier)");
                }
                tickets.setColor(ChatColor.BLUE);
            }
            textComponent.addExtra(tickets);

            textComponent.addExtra("\n \n");
            textComponent.addExtra(lines);
            player.getPlayer().spigot().sendMessage(textComponent);
            player.getKitLevel().addXp(player.getKit(), (totalXP + timeXp) * 4L);
        }
    }

}
