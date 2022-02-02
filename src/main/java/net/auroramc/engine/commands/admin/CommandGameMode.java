/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandGameMode extends Command {

    public CommandGameMode() {
        super("gamemode", Collections.singletonList("gm"), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (args.size() == 0) {
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;

            if (pl.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                pl.getPlayer().setGameMode(GameMode.SURVIVAL);
                pl.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("GameMode", "Creative Mode: &cDisabled"));
            } else {
                pl.getPlayer().setGameMode(GameMode.CREATIVE);
                pl.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("GameMode", "Creative Mode: &aEnabled"));
            }
        } else {
            AuroraMCGamePlayer pl = (AuroraMCGamePlayer) AuroraMCAPI.getPlayer(args.get(0));
            if (pl != null) {
                if (pl.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                    pl.getPlayer().setGameMode(GameMode.SURVIVAL);
                    pl.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("GameMode", "Creative Mode: &cDisabled"));
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("GameMode", "Creative mode for player **" + pl.getName() + "**: &cDisabled"));
                } else {
                    pl.getPlayer().setGameMode(GameMode.CREATIVE);
                    pl.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("GameMode", "Creative Mode: &aEnabled"));
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("GameMode", "Creative mode for player **" + pl.getName() + "**: &aEnabled"));
                }
            } else {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("GameMode", "Player **" + args.get(0) + "** was not found."));
            }
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }

}
