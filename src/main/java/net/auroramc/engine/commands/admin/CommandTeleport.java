/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTeleport extends Command {

    public CommandTeleport() {
        super("Teleport", Arrays.asList("tp", "teleportplayer"), Arrays.asList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String s, List<String> args) {
        if (args.size() > 0) {
            if (args.get(0).equalsIgnoreCase("here")) {
                if (args.size() == 2) {
                    if (args.get(1).equalsIgnoreCase("all")) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", "You have teleported everyone to yourself."));
                        for (Player from : Bukkit.getOnlinePlayers()) {
                            if (from.equals(player.getPlayer())) {
                                continue;
                            }
                            from.sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Map Manager", String.format("You have been teleported to player **%s**.", player.getPlayer().getName())));
                            from.teleport(player.getPlayer().getLocation());
                        }
                    } else {
                        Player target = Bukkit.getPlayer(args.get(1));
                        AuroraMCPlayer aTarget = AuroraMCAPI.getPlayer(target);
                        if (target != null) {
                            aTarget.getPlayer().teleport(player.getPlayer().getLocation());
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Map Manager", String.format("You have teleported **%s** to your location.", target.getName())));
                            target.sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", String.format("**%s** has teleported you to their location.", player.getPlayer().getName())));
                        } else {
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", String.format("No matches found for user [**%s**]", args.get(0))));
                        }
                    }
                } else {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", "Invalid syntax. Correct syntax: **/tp here <user | all**"));
                }
            } else {
                if (args.size() == 1) {
                    if (args.get(0).equalsIgnoreCase(player.getPlayer().getName())) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", "You cannot teleport to yourself."));
                        return;
                    }
                    Player target = Bukkit.getPlayer(args.get(0));
                    AuroraMCPlayer aTarget = AuroraMCAPI.getPlayer(target);
                    if (target != null) {
                        player.getPlayer().teleport(aTarget.getPlayer().getLocation());
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", String.format("You have been teleported to player **%s**.", target.getName())));
                    } else {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", String.format("No matches found for user [**%s**]", args.get(0))));
                    }
                } else if (args.size() == 2) {
                    if (args.get(0).equalsIgnoreCase(args.get(1))) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", "You cannot teleport a player to themselves."));
                        return;
                    }
                    Player from = Bukkit.getPlayer(args.get(0));
                    AuroraMCPlayer afrom = AuroraMCAPI.getPlayer(from);

                    if (from != null) {

                        Player to = Bukkit.getPlayer(args.get(1));
                        AuroraMCPlayer ato = AuroraMCAPI.getPlayer(to);

                        if (to != null) {
                            from.sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Map Manager", String.format("You have been teleported to player **%s**.", to.getName())));
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", String.format("You have teleported **%s** to **%s**.", args.get(0), args.get(1))));
                            afrom.getPlayer().teleport(ato.getPlayer().getLocation());
                        } else {
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", String.format("No matches found for user [**%s**]", args.get(1))));
                        }
                    } else {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", String.format("No matches found for user [**%s**]", args.get(0))));
                    }
                } else {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", "Invalid syntax. Correct syntax: **/tp <user> <user>**"));
                }
            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Teleport", "Invalid syntax. Correct syntax: **/tp <user> <user>**"));
        }
    }


    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String lastToken, int numberArguments) {
        ArrayList<String> completions = new ArrayList<>();
        if (numberArguments == 1) {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                if (player1.getName().toLowerCase().startsWith(lastToken.toLowerCase())) {
                    completions.add(player1.getName());
                }
            }
        }
        return completions;
    }

}
