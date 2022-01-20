package net.auroramc.engine.commands.game;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.GameUtils;
import net.auroramc.engine.api.games.GameInfo;
import net.auroramc.engine.api.games.GameMap;
import net.auroramc.engine.api.games.GameVariation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGameSet extends Command {


    public CommandGameSet() {
        super("set", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (args.size() >= 1) {
            switch (EngineAPI.getServerState()) {
                case STARTING:
                case IDLE:
                case WAITING_FOR_PLAYERS: {
                    String gameString = args.remove(0);
                    GameInfo info = EngineAPI.getGames().get(gameString.toUpperCase());
                    if (info == null) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for game: **" + gameString + "**"));
                        return;
                    }
                    GameVariation gameVariation = null;
                    GameMap map = null;
                    if (args.size() >= 1) {
                        String arg = args.remove(0);
                        if (arg.startsWith("v")) {
                            arg = arg.substring(1);
                            gameVariation = info.getVariations().get(arg);
                            if (gameVariation == null) {
                                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for variation: **" + arg + "**"));
                                return;
                            }
                        } else if (arg.startsWith("m")) {
                            arg = arg.substring(1);
                            if (arg.contains(":")) {
                                String[] args2 = arg.split(":");
                                if (args2.length != 2) {
                                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. When specifying maps from other games, please use format: **GAME:MAP**"));
                                    return;
                                }
                                map = EngineAPI.getMaps().get(args2[0]).getMap(args2[1]);
                            } else {
                                map = EngineAPI.getMaps().get(info.getRegistryKey()).getMap(arg);
                            }
                            if (map == null) {
                                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for map: **" + arg + "**"));
                                return;
                            }
                        } else {
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/game set [game] v[variation] m[map]**"));
                        }
                        if (args.size() == 1) {
                            arg = args.remove(0);
                            if (arg.startsWith("v") && gameVariation == null) {
                                arg = arg.substring(1);
                                gameVariation = info.getVariations().get(arg);
                                if (gameVariation == null) {
                                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for variation: **" + arg + "**"));
                                    return;
                                }
                            } else if (arg.startsWith("m") && map == null) {
                                arg = arg.substring(1);
                                if (arg.contains(":")) {
                                    String[] args2 = arg.split(":");
                                    if (args2.length != 2) {
                                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. When specifying maps from other games, please use format: **GAME:MAP**"));
                                        return;
                                    }
                                    map = EngineAPI.getMaps().get(args2[0]).getMap(args2[1]);
                                } else {
                                    map = EngineAPI.getMaps().get(info.getRegistryKey()).getMap(arg);
                                }
                                if (map == null) {
                                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for map: **" + arg + "**"));
                                    return;
                                }
                            } else {
                                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/game set [game] v[variation] m[map]**"));
                            }
                        }
                    }
                    GameUtils.loadGame(info, map, gameVariation);
                    String message;
                    if (map != null) {
                        if (gameVariation != null) {
                            message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", String.format("The game has been set to **%s %s** with map **%s**.", gameVariation.getName(), info.getName(), map.getName()));
                        } else {
                            message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", String.format("The game has been set to **%s** with map **%s**.", info.getName(), map.getName()));
                        }
                    } else {
                        if (gameVariation != null) {
                            message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", String.format("The game has been set to **%s %s**.", gameVariation.getName(), info.getName()));
                        } else {
                            message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", String.format("The game has been set to **%s**.", info.getName()));
                        }
                    }
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        player1.sendMessage(message);
                    }
                    break;
                }
                case IN_GAME:
                case ENDING: {
                    String gameString = args.remove(0);
                    GameInfo info = EngineAPI.getGames().get(gameString.toUpperCase());
                    if (info == null) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for game: **" + gameString + "**"));
                        return;
                    }
                    GameVariation gameVariation = null;
                    GameMap map = null;
                    if (args.size() >= 1) {
                        String arg = args.remove(0);
                        if (arg.startsWith("v")) {
                            arg = arg.substring(1);
                            gameVariation = info.getVariations().get(arg);
                            if (gameVariation == null) {
                                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for variation: **" + arg + "**"));
                                return;
                            }
                        } else if (arg.startsWith("m")) {
                            arg = arg.substring(1);
                            if (arg.contains(":")) {
                                String[] args2 = arg.split(":");
                                if (args2.length != 2) {
                                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. When specifying maps from other games, please use format: **GAME:MAP**"));
                                    return;
                                }
                                map = EngineAPI.getMaps().get(args2[0]).getMap(args2[1]);
                            } else {
                                map = EngineAPI.getMaps().get(info.getRegistryKey()).getMap(arg);
                            }
                            if (map == null) {
                                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for map: **" + arg + "**"));
                                return;
                            }
                        } else {
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/game set [game] v[variation] m[map]**"));
                        }
                        if (args.size() == 1) {
                            arg = args.remove(0);
                            if (arg.startsWith("v") && gameVariation == null) {
                                arg = arg.substring(1);
                                gameVariation = info.getVariations().get(arg);
                                if (gameVariation == null) {
                                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for variation: **" + arg + "**"));
                                    return;
                                }
                            } else if (arg.startsWith("m") && map == null) {
                                arg = arg.substring(1);
                                if (arg.contains(":")) {
                                    String[] args2 = arg.split(":");
                                    if (args2.length != 2) {
                                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. When specifying maps from other games, please use format: **GAME:MAP**"));
                                        return;
                                    }
                                    map = EngineAPI.getMaps().get(args2[0]).getMap(args2[1]);
                                } else {
                                    map = EngineAPI.getMaps().get(info.getRegistryKey()).getMap(arg);
                                }
                                if (map == null) {
                                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "No results found for map: **" + arg + "**"));
                                    return;
                                }
                            } else {
                                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/game set [game] v[variation] m[map]**"));
                            }
                        }
                    }
                    EngineAPI.setNextMap(map);
                    EngineAPI.setNextGame(info);
                    EngineAPI.setNextVariation(gameVariation);
                    String message;
                    if (map != null) {
                        if (gameVariation != null) {
                            message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", String.format("The next game has been set to **%s %s** with map **%s**.", gameVariation.getName(), info.getName(), map.getName()));
                        } else {
                            message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", String.format("The next game has been set to **%s** with map **%s**.", info.getName(), map.getName()));
                        }
                    } else {
                        if (gameVariation != null) {
                            message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", String.format("The next game has been set to **%s %s**.", gameVariation.getName(), info.getName()));
                        } else {
                            message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", String.format("The next game has been set to **%s**.", info.getName()));
                        }
                    }
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        player1.sendMessage(message);
                    }
                    break;
                }
                default: {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You cannot set the game at this time."));
                }

            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Invalid syntax. Correct syntax: **/game set [game] v[variation] m[map]**"));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer player, String aliasUsed, List<String> args, String lastToken, int numberArguments) {
        return new ArrayList<>();
    }
}
