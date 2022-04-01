/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandHub extends Command {


    public CommandHub() {
        super("hub", Collections.singletonList("lobby"), Collections.singletonList(Permission.PLAYER), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (player.getStats().getAchievementsGained().containsKey(AuroraMCAPI.getAchievement(16))) {
            player.getStats().achievementGained(AuroraMCAPI.getAchievement(16), 1, true);
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Lobby");
        out.writeUTF(player.getPlayer().getUniqueId().toString());
        player.getPlayer().sendPluginMessage(AuroraMCAPI.getCore(), "BungeeCord", out.toByteArray());
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer player, String aliasUsed, List<String> args, String lastToken, int noOfTokens) {
        return new ArrayList<>();
    }
}
