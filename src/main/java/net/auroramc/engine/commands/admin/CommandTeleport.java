/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTeleport extends ServerCommand {

    public CommandTeleport() {
        super("Teleport", Arrays.asList("tp", "teleportplayer"), Arrays.asList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String s, List<String> args) {
        if (args.size() > 0) {
            if (args.get(0).equalsIgnoreCase("here")) {
                if (args.size() == 2) {
                    if (args.get(1).equalsIgnoreCase("all")) {
                        player.sendMessage(TextFormatter.pluginMessage("Teleport", "You have teleported everyone to yourself."));
                        for (Player from : Bukkit.getOnlinePlayers()) {
                            if (from.equals(player)) {
                                continue;
                            }
                            from.spigot().sendMessage(TextFormatter.pluginMessage("Map Manager", String.format("You have been teleported to player **%s**.", player.getName())));
                            from.teleport(player.getLocation());
                        }
                    } else {
                        Player target = Bukkit.getPlayer(args.get(1));
                        AuroraMCServerPlayer aTarget = ServerAPI.getPlayer(target);
                        if (target != null) {
                            aTarget.teleport(player.getLocation());
                            player.sendMessage(TextFormatter.pluginMessage("Map Manager", String.format("You have teleported **%s** to your location.", target.getName())));
                            aTarget.sendMessage(TextFormatter.pluginMessage("Teleport", String.format("**%s** has teleported you to their location.", player.getName())));
                        } else {
                            player.sendMessage(TextFormatter.pluginMessage("Teleport", String.format("No matches found for user [**%s**]", args.get(0))));
                        }
                    }
                } else {
                    player.sendMessage(TextFormatter.pluginMessage("Teleport", "Invalid syntax. Correct syntax: **/tp here <user | all**"));
                }
            } else {
                if (args.size() == 1) {
                    if (args.get(0).equalsIgnoreCase(player.getName())) {
                        player.sendMessage(TextFormatter.pluginMessage("Teleport", "You cannot teleport to yourself."));
                        return;
                    }
                    Player target = Bukkit.getPlayer(args.get(0));
                    AuroraMCServerPlayer aTarget = ServerAPI.getPlayer(target);
                    if (target != null) {
                        player.teleport(aTarget.getLocation());
                        player.sendMessage(TextFormatter.pluginMessage("Teleport", String.format("You have been teleported to player **%s**.", target.getName())));
                    } else {
                        player.sendMessage(TextFormatter.pluginMessage("Teleport", String.format("No matches found for user [**%s**]", args.get(0))));
                    }
                } else if (args.size() == 2) {
                    if (args.get(0).equalsIgnoreCase(args.get(1))) {
                        player.sendMessage(TextFormatter.pluginMessage("Teleport", "You cannot teleport a player to themselves."));
                        return;
                    }
                    Player from = Bukkit.getPlayer(args.get(0));
                    AuroraMCServerPlayer afrom = ServerAPI.getPlayer(from);

                    if (from != null) {

                        Player to = Bukkit.getPlayer(args.get(1));
                        AuroraMCServerPlayer ato = ServerAPI.getPlayer(to);

                        if (to != null) {
                            afrom.sendMessage(TextFormatter.pluginMessage("Map Manager", String.format("You have been teleported to player **%s**.", to.getName())));
                            player.sendMessage(TextFormatter.pluginMessage("Teleport", String.format("You have teleported **%s** to **%s**.", args.get(0), args.get(1))));
                            afrom.teleport(ato.getLocation());
                        } else {
                            player.sendMessage(TextFormatter.pluginMessage("Teleport", String.format("No matches found for user [**%s**]", args.get(1))));
                        }
                    } else {
                        player.sendMessage(TextFormatter.pluginMessage("Teleport", String.format("No matches found for user [**%s**]", args.get(0))));
                    }
                } else {
                    player.sendMessage(TextFormatter.pluginMessage("Teleport", "Invalid syntax. Correct syntax: **/tp <user> <user>**"));
                }
            }
        } else {
            player.sendMessage(TextFormatter.pluginMessage("Teleport", "Invalid syntax. Correct syntax: **/tp <user> <user>**"));
        }
    }


    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer auroraMCPlayer, String s, List<String> list, String lastToken, int numberArguments) {
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
