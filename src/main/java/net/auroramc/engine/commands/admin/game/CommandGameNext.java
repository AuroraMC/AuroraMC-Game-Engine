/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.commands.admin.game;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.GameInfo;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.GameVariation;
import net.auroramc.engine.api.games.GameVariationInfo;
import net.auroramc.engine.api.server.ServerState;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGameNext extends ServerCommand {


    public CommandGameNext() {
        super("next", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String aliasUsed, List<String> args) {
        if (args.size() >= 1) {
            if (EngineAPI.getServerState() != ServerState.STARTING_UP && EngineAPI.getServerState() != ServerState.RELOADING_MAPS) {
                String gameString = args.remove(0);
                GameInfo info = EngineAPI.getGames().get(gameString.toUpperCase());
                if (info == null) {
                    List<GameInfo> infos = new ArrayList<>();
                    for (GameInfo gameInfo : EngineAPI.getGames().values()) {
                        if (gameInfo.getRegistryKey().toUpperCase().contains(gameString.toUpperCase())) {
                            infos.add(gameInfo);
                        }
                    }
                    if (infos.size() > 0) {
                        if (infos.size() > 1) {
                            StringBuilder builder = new StringBuilder();
                            for (GameInfo info1 : infos) {
                                builder.append("\n - **").append(info1.getRegistryKey().toUpperCase()).append("**");
                            }
                            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Multiple matches for game: **" + gameString + "**. Possible games:" + builder));
                            return;
                        } else {
                            info = infos.get(0);
                        }
                    } else {
                        player.sendMessage(TextFormatter.pluginMessage("Game Manager", "No results found for game: **" + gameString + "**"));
                        return;
                    }
                }
                GameVariationInfo gameVariation = null;
                GameMap map = null;
                if (args.size() >= 1) {
                    String arg = args.remove(0);
                    if (arg.startsWith("v")) {
                        arg = arg.substring(1);
                        gameVariation = info.getVariations().get(arg);
                        if (gameVariation == null) {
                            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "No results found for variation: **" + arg + "**"));
                            return;
                        }
                    } else if (arg.startsWith("m")) {
                        arg = arg.substring(1);
                        if (arg.contains(":")) {
                            String[] args2 = arg.split(":");
                            if (args2.length != 2) {
                                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Invalid syntax. When specifying maps from other games, please use format: **GAME:MAP**"));
                                return;
                            }
                            if (!EngineAPI.getMaps().containsKey(args2[0])) {
                                if (args2.length != 2) {
                                    player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Game Key **" + args2[0] + "** does not exist. Are you sure its correct?"));
                                    return;
                                }
                            }
                            map = EngineAPI.getMaps().get(args2[0]).getMap(args2[1]);
                        } else {
                            map = EngineAPI.getMaps().get(info.getRegistryKey()).getMap(arg);
                        }
                        if (map == null) {
                            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "No results found for map: **" + arg + "**"));
                            return;
                        }
                    } else {
                        player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/game set [game] v[variation] m[map]**"));
                    }
                    if (args.size() == 1) {
                        arg = args.remove(0);
                        if (arg.startsWith("v") && gameVariation == null) {
                            arg = arg.substring(1);
                            gameVariation = info.getVariations().get(arg);
                            if (gameVariation == null) {
                                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "No results found for variation: **" + arg + "**"));
                                return;
                            }
                        } else if (arg.startsWith("m") && map == null) {
                            arg = arg.substring(1);
                            if (arg.contains(":")) {
                                String[] args2 = arg.split(":");
                                if (args2.length != 2) {
                                    player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Invalid syntax. When specifying maps from other games, please use format: **GAME:MAP**"));
                                    return;
                                }
                                map = EngineAPI.getMaps().get(args2[0]).getMap(args2[1]);
                            } else {
                                map = EngineAPI.getMaps().get(info.getRegistryKey()).getMap(arg);
                            }
                            if (map == null) {
                                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "No results found for map: **" + arg + "**"));
                                return;
                            }
                        } else {
                            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/game set [game] v[variation] m[map]**"));
                        }
                    }
                }
                EngineAPI.setNextMap(map);
                EngineAPI.setNextGame(info);
                EngineAPI.setNextVariation(gameVariation);
                BaseComponent message;
                if (map != null) {
                    if (gameVariation != null) {
                        message = TextFormatter.pluginMessage("Game Manager", String.format("The next game has been set to **%s %s** with map **%s**.", gameVariation.getName(), info.getName(), map.getName()));
                    } else {
                        message = TextFormatter.pluginMessage("Game Manager", String.format("The next game has been set to **%s** with map **%s**.", info.getName(), map.getName()));
                    }
                } else {
                    if (gameVariation != null) {
                        message = TextFormatter.pluginMessage("Game Manager", String.format("The next game has been set to **%s %s**.", gameVariation.getName(), info.getName()));
                    } else {
                        message = TextFormatter.pluginMessage("Game Manager", String.format("The next game has been set to **%s**.", info.getName()));
                    }
                }
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    player1.spigot().sendMessage(message);
                }
            } else {
                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You cannot set the next game at this time."));
            }
        } else {
            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/game next [game] v[variation] m[map]**"));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
