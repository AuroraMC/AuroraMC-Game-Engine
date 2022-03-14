/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.Game;
import net.auroramc.engine.api.games.GameInfo;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.players.PlayerKitLevel;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandKitXP extends Command {


    public CommandKitXP() {
        super("kitxp", Collections.singletonList("kxp"), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUSed, List<String> args) {
        if (args.size() == 5) {
            if (args.get(0).equalsIgnoreCase("add")) {
                String target = args.get(1);
                String gameName = args.get(2);
                GameInfo game;
                int kid;
                long amount;

                try {
                    kid = Integer.parseInt(args.get(3));
                    amount = Long.parseLong(args.get(4));
                } catch (NumberFormatException ignored) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/kitxp add [player] [game] [kit id] [amount]**"));
                    return;
                }

                game = EngineAPI.getGames().get(gameName);
                if (gameName == null) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "That is not a valid game!"));
                    return;
                }

                AuroraMCPlayer player1 = AuroraMCAPI.getDisguisedPlayer(target);
                if (player1 == null) {
                    player1 = AuroraMCAPI.getPlayer(target);
                    if (player1 != null) {
                        AuroraMCGamePlayer gp = (AuroraMCGamePlayer) player1;
                        if (gp.getKitLevel() != null && gp.getKitLevel().getGameId() == game.getId() && gp.getKitLevel().getKitId() == kid) {
                            gp.getKitLevel().addXp(gp.getKit(), amount);
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "**" + amount +"** XP added to kit **" + gp.getKit().getName() + "** in game **" + game.getName() + "**."));
                            return;
                        }
                        return;
                    }
                }
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        int id = AuroraMCAPI.getDbManager().getAuroraMCID(target);
                        if (id == -1) {
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "That player could not be found."));
                            return;
                        }

                        PlayerKitLevel kitLevel = EngineDatabaseManager.getKitLevel(id, game.getId(), kid);
                        kitLevel.addXp(null, amount);
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "**" + amount + "** XP added to kit with ID **" + kid + "** in game **" + game.getName() + "**."));
                    }
                }.runTaskAsynchronously(AuroraMCAPI.getCore());

            } else if (args.get(0).equalsIgnoreCase("remove")) {
                String target = args.get(1);
                String gameName = args.get(2);
                GameInfo game;
                int kid;
                int amount;

                try {
                    kid = Integer.parseInt(args.get(3));
                    amount = Integer.parseInt(args.get(4));
                } catch (NumberFormatException ignored) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/kitxp add [player] [game] [kit id] [amount]**"));
                    return;
                }

                game = EngineAPI.getGames().get(gameName);
                if (gameName == null) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "That is not a valid game!"));
                    return;
                }

                AuroraMCPlayer player1 = AuroraMCAPI.getDisguisedPlayer(target);
                if (player1 == null) {
                    player1 = AuroraMCAPI.getPlayer(target);
                    if (player1 != null) {
                        AuroraMCGamePlayer gp = (AuroraMCGamePlayer) player1;
                        if (gp.getKitLevel() != null && gp.getKitLevel().getGameId() == game.getId() && gp.getKitLevel().getKitId() == kid) {
                            gp.getKitLevel().removeXP(amount);
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "**" + amount + "** XP removed from kit **" + gp.getKit().getName() + "** in game **" + game.getName() + "**."));
                            return;
                        }
                        return;
                    }
                }
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        int id = AuroraMCAPI.getDbManager().getAuroraMCID(target);
                        if (id == -1) {
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "That player could not be found."));
                            return;
                        }

                        PlayerKitLevel kitLevel = EngineDatabaseManager.getKitLevel(id, game.getId(), kid);
                        kitLevel.removeXP(amount);
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "**" + amount + "** XP removed from kit with ID **" + kid + "** in game **" + game.getName() + "**."));
                    }
                }.runTaskAsynchronously(AuroraMCAPI.getCore());
            } else {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Available subcommands:\n" +
                        "**/kitxp add [player] [game] [kit id] [amount]** - Add XP to the specified kit in the specified game to the specified player.\n" +
                        "**/kitxp remove [player] [game] [kit id] [amount]** - Remove XP from the specified kit in the specified game to the specified player."));
            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Available subcommands:\n" +
                    "**/kitxp add [player] [game] [kit id] [amount]** - Add XP to the specified kit in the specified game to the specified player.\n" +
                    "**/kitxp remove [player] [game] [kit id] [amount]** - Remove XP from the specified kit in the specified game to the specified player."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
