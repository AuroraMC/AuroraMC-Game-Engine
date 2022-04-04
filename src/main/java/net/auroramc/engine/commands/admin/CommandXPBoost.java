/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandXPBoost extends Command {


    public CommandXPBoost() {
        super("xpboost", Collections.singletonList("boost"), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
       if (args.size() >= 3) {
           int duration;
           float multiplier;
           try {
               duration = Integer.parseInt(args.remove(0));
               multiplier = Float.parseFloat(args.remove(0));
           } catch (NumberFormatException e) {
               player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("XP Boost", "Invalid syntax. Correct syntax: **/xpboost [duration in days] [multiplier] [message...]**"));
               return;
           }
           if (duration < 1 || multiplier < 1) {
               player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("XP Boost", "Invalid syntax. Correct syntax: **/xpboost [duration in days] [multiplier] [message...]**"));
               return;
           }
           String message = String.join(" ", args);
           EngineDatabaseManager.activateXpMultiplier(duration, multiplier, message);
           player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("XP Boost", "XP Boost activated! Please give up to a minute for the change to be reflected in-game!"));
       } else {
           player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("XP Boost", "Invalid syntax. Correct syntax: **/xpboost [duration in days] [multiplier] [message...]**"));
       }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer player, String aliasUsed, List<String> args, String lastToken, int noOfTokens) {
        return new ArrayList<>();
    }
}