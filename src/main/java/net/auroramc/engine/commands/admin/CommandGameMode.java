/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
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
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGameMode extends ServerCommand {

    public CommandGameMode() {
        super("gamemode", Collections.singletonList("gm"), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String aliasUsed, List<String> args) {
        if (args.size() == 0) {
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;

            if (pl.getGameMode().equals(GameMode.CREATIVE)) {
                pl.setGameMode(GameMode.SURVIVAL);
                pl.sendMessage(TextFormatter.pluginMessage("GameMode", "Creative Mode: §cDisabled"));
                if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                    if (!player.isVanished() && !((AuroraMCGamePlayer) player).isSpectator()) {
                        EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                    }
                    EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", "Set own GameMode to Survival.").put("player", player.getName())));
                }
            } else {
                pl.setGameMode(GameMode.CREATIVE);
                pl.sendMessage(TextFormatter.pluginMessage("GameMode", "Creative Mode: §aEnabled"));
                if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                    if (!player.isVanished() && !((AuroraMCGamePlayer) player).isSpectator()) {
                        EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                    }
                    EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", "Set own GameMode to Creative.").put("player", player.getName())));
                }
            }
        } else {
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) ServerAPI.getPlayer(args.get(0));
            if (pl != null) {
                if (pl.getGameMode().equals(GameMode.CREATIVE)) {
                    pl.setGameMode(GameMode.SURVIVAL);
                    pl.sendMessage(TextFormatter.pluginMessage("GameMode", "Creative Mode: §cDisabled"));
                    player.sendMessage(TextFormatter.pluginMessage("GameMode", "Creative mode for player **" + pl.getName() + "**: §cDisabled"));
                    if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                        if (!pl.isVanished() && !pl.isSpectator()) {
                            EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                        }
                        EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", "Set GameMode to Survival.").put("player", player.getName()).put("to", pl.getName())));
                    }
                } else {
                    pl.setGameMode(GameMode.CREATIVE);
                    pl.sendMessage(TextFormatter.pluginMessage("GameMode", "Creative Mode: §aEnabled"));
                    player.sendMessage(TextFormatter.pluginMessage("GameMode", "Creative mode for player **" + pl.getName() + "**: §aEnabled"));
                    if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                        if (!pl.isVanished() && !pl.isSpectator()) {
                            EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                        }
                        EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", "Set GameMode to Creative.").put("player", player.getName()).put("to", pl.getName())));
                    }
                }
            } else {
                player.sendMessage(TextFormatter.pluginMessage("GameMode", "Player **" + args.get(0) + "** was not found."));
            }
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }

}
