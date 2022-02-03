/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin.game;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGame extends Command {


    public CommandGame() {
        super("game", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
        this.registerSubcommand("next", Collections.emptyList(), new CommandGameNext());
        this.registerSubcommand("set", Collections.emptyList(), new CommandGameSet());
        this.registerSubcommand("stop", Collections.emptyList(), new CommandGameStop());
        this.registerSubcommand("start", Collections.emptyList(), new CommandGameStart());
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (args.size() > 0) {
            switch (args.get(0).toLowerCase()) {
                case "set":
                case "start":
                case "next":
                case "stop":
                    aliasUsed = args.remove(0).toLowerCase();
                    subcommands.get(aliasUsed).execute(player, aliasUsed, args);
                    break;
                default:
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game", "Available subcommands are:\n" +
                            "**/game set [game] v[variation] m[map]** - Set the current game, or the next game if a game is in progress.\n" +
                            "**/game next  [game] v[variation] m[map]** - Set the next game.\n" +
                            "**/game stop** - Stop the current game.\n" +
                            "**/game start [seconds]** - Start the currently loaded game with the specified starting timer."));
                    break;
            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game", "Available subcommands are:\n" +
                    "**/game set [game] v[variation] m[map]** - Set the current game, or the next game if a game is in progress.\n" +
                    "**/game next  [game] v[variation] m[map]** - Set the next game.\n" +
                    "**/game stop** - Stop the current game.\n" +
                    "**/game start [seconds]** - Start the currently loaded game with the specified starting timer."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer player, String aliasUsed, List<String> args, String lastToken, int numberArguments) {
        ArrayList<String> completions = new ArrayList<>();
        if (numberArguments == 1) {
            for (String s : subcommands.keySet()){
                if (s.startsWith(lastToken)) {
                    completions.add(s);
                }
            }
        }
        return completions;
    }
}
