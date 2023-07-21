/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandXPBoost extends ServerCommand {


    public CommandXPBoost() {
        super("xpboost", Collections.singletonList("boost"), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String aliasUsed, List<String> args) {
       if (args.size() >= 3) {
           int duration;
           float multiplier;
           try {
               duration = Integer.parseInt(args.remove(0));
               multiplier = Float.parseFloat(args.remove(0));
           } catch (NumberFormatException e) {
               player.sendMessage(TextFormatter.pluginMessage("XP Boost", "Invalid syntax. Correct syntax: **/xpboost [duration in days] [multiplier] [message...]**"));
               return;
           }
           if (duration < 1 || multiplier < 1) {
               player.sendMessage(TextFormatter.pluginMessage("XP Boost", "Invalid syntax. Correct syntax: **/xpboost [duration in days] [multiplier] [message...]**"));
               return;
           }
           String message = String.join(" ", args);
           if (!AuroraMCAPI.isTestServer()) {
               EngineDatabaseManager.activateXpMultiplier(duration, multiplier, message);
           }
           player.sendMessage(TextFormatter.pluginMessage("XP Boost", "XP Boost activated! Please give up to a minute for the change to be reflected in-game!"));
       } else {
           player.sendMessage(TextFormatter.pluginMessage("XP Boost", "Invalid syntax. Correct syntax: **/xpboost [duration in days] [multiplier] [message...]**"));
       }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer player, String aliasUsed, List<String> args, String lastToken, int noOfTokens) {
        return new ArrayList<>();
    }
}
