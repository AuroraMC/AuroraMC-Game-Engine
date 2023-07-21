/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.GameSession;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.apache.commons.lang.WordUtils;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandEffect extends ServerCommand {

    public CommandEffect() {
        super("effect", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String aliasUsed, List<String> args) {
        if (args.size() >= 2) {
            String target = args.remove(0);
            String effect = args.remove(0);

            PotionEffectType type = null;

            List<String> matches = new ArrayList<>();
            for (PotionEffectType type1 : PotionEffectType.values()) {
                if (type1 == null || type1.getName() == null) {
                    continue;
                }
                if (type1.getName().equalsIgnoreCase(effect)) {
                    type = type1;
                    break;
                }
                if (type1.getName().startsWith(effect.toUpperCase())) {
                    matches.add(type1.getName());
                }
            }

            if (matches.size() == 0 && type == null) {
                player.sendMessage(TextFormatter.pluginMessage("Effect", "No matches were found for potion effect **" + effect + "**."));
                return;
            }

            if (matches.size() > 1 && type == null) {
                player.sendMessage(TextFormatter.pluginMessage("Effect", "Multiple possible matches found for potion effect **" + effect + "**. Please be more specific. Matches: [**" + String.join("**, **", matches) + "**]"));
                return;
            }

            if (type == null) {
                type = PotionEffectType.getByName(matches.get(0));
            }
            int multiplier = 1;
            int duration = 10;

            if (args.size() >= 1) {
                try {
                    multiplier = Integer.parseInt(args.remove(0));
                    if (multiplier < 0 || multiplier > 255) {
                        player.sendMessage(TextFormatter.pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(TextFormatter.pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
                    return;
                }
            }
            if (args.size() >= 1) {
                try {
                    duration = Integer.parseInt(args.remove(0));
                    if (duration < 1) {
                        player.sendMessage(TextFormatter.pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(TextFormatter.pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
                    return;
                }
            }

            if (target.equalsIgnoreCase("all")) {
                if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                    EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                    EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", "Effect " + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + " given to all players for " + duration + " seconds.").put("player", player.getName())));
                }
                for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
                    player1.addPotionEffect(new PotionEffect(type, duration*20, multiplier));
                    player1.sendMessage(TextFormatter.pluginMessage("Effect", "You were given effect **" + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + "** for **" + duration + "** seconds by **" + player.getName() + "**."));
                }
                player.sendMessage(TextFormatter.pluginMessage("Effect", "You gave effect **" + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + "** for **" + duration + " seconds** to **" + ServerAPI.getPlayers().size() + "** players."));
            } else {
                String[] targets = target.split(",");
                int players = 0;
                int playersInGame = 0;
                for (String target1 : targets) {
                    AuroraMCServerPlayer player1 = ServerAPI.getPlayer(target1);
                    if (player1 != null) {
                        player1.addPotionEffect(new PotionEffect(type, duration*20, multiplier));
                        players++;
                        player1.sendMessage(TextFormatter.pluginMessage("Effect", "You were given effect **" + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + "** for **" + duration + "** seconds by **" + player.getName() + "**."));
                        if (!player1.isVanished() && !((AuroraMCGamePlayer)player).isSpectator()) {
                            playersInGame++;
                        }
                    } else {
                        player.sendMessage(TextFormatter.pluginMessage("Give", "Target **" + target1 + "** not found."));
                    }
                }
                if (players == 0) {
                    player.sendMessage(TextFormatter.pluginMessage("Give", "No targets were found, so nothing was given out."));
                } else {
                    if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                        if (playersInGame > 0) {
                            EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                        }
                        EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", "Effect " + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + " given to " + players + " players for " + duration + " seconds.").put("player", player.getName())));
                    }
                    player.sendMessage(TextFormatter.pluginMessage("Effect", "You gave effect **" + WordUtils.capitalizeFully(type.getName().replace("_", " ")) + " " + multiplier + "** for **" + duration + " seconds** to **" + players + "** players."));
                }
            }
        } else {
            player.sendMessage(TextFormatter.pluginMessage("Effect", "Invalid syntax. Correct syntax: **/effect <player|all> <effect> [amplifier] [duration]**"));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }

}
