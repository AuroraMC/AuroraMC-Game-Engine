/*
 * Copyright (c) 2023-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class CommandLoadEvent extends ServerCommand {


    public CommandLoadEvent() {
        super("loadevent", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String s, List<String> list) {
        if (!ServerAPI.isEventMode()) {
            try {
                ServerAPI.loadEvent();
            } catch (Exception e) {
                AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
                player.sendMessage(TextFormatter.pluginMessage("Events", "An exception occurred when attempting to load the event plugin. Please try again."));
            }
        } else {
            player.sendMessage(TextFormatter.pluginMessage("Events", "This server is already in event mode."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer player, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
