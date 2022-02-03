/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandEffect extends Command {

    public CommandEffect() {
        super("effect", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (args.size() >= 2) {
            String target = args.remove(0);
            String effect = args.remove(0);

            List<String> matches = new ArrayList<>();
            for (PotionEffectType type : PotionEffectType.values()) {
                if (type == null || type.getName() == null) {
                    continue;
                }
                if (type.getName().startsWith(effect.toUpperCase())) {
                    matches.add(type.getName());
                }
            }

            if (matches.size() == 0) {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "No matches were found for potion effect **" + effect + "**."));
                return;
            }

            if (matches.size() > 1) {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "Multiple possible matches found for potion effect **" + effect + "**. Please be more specific. Matches: [**" + String.join("**, **", matches) + "**]"));
                return;
            }

            PotionEffectType type = PotionEffectType.getByName(matches.get(0));
            int multiplier = 1;
            int duration = 10;

            if (args.size() >= 1) {
                try {
                    multiplier = Integer.parseInt(args.remove(0));
                    if (multiplier < 0 || multiplier > 255) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
                    return;
                }
            }
            if (args.size() >= 1) {
                try {
                    duration = Integer.parseInt(args.remove(0));
                    if (duration < 1) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
                    return;
                }
            }

            if (target.equalsIgnoreCase("all")) {
                for (AuroraMCPlayer player1 : AuroraMCAPI.getPlayers()) {
                    player1.getPlayer().addPotionEffect(new PotionEffect(type, duration*20, multiplier));
                    player1.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "You were given effect **" + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + "** for **" + duration + "** seconds by **" + player.getPlayer().getName() + "**."));
                }
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "You gave effect **" + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + "** for **" + duration + " seconds** to **" + AuroraMCAPI.getPlayers().size() + "** players."));
            } else {
                String[] targets = target.split(",");
                int players = 0;
                for (String target1 : targets) {
                    AuroraMCPlayer player1 = AuroraMCAPI.getPlayer(target1);
                    if (player1 != null) {
                        player1.getPlayer().addPotionEffect(new PotionEffect(type, duration*20, multiplier));
                        players++;
                        player1.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "You were given effect **" + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + "** for **" + duration + "** seconds by **" + player.getPlayer().getName() + "**."));
                    } else {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "Target **" + target1 + "** not found."));
                    }
                }
                if (players == 0) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "No targets were found, so nothing was given out."));
                } else {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "You gave effect **" + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + "** for **" + duration + " seconds** to **" + players + "** players."));
                }
            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }

}
