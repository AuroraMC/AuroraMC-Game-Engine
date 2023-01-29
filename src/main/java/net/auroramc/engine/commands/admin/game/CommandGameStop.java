/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin.game;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.GameUtils;
import net.auroramc.engine.api.games.GameSession;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandGameStop extends Command {


    public CommandGameStop() {
        super("stop", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.STARTING || EngineAPI.getServerState() == ServerState.WAITING_FOR_PLAYERS) {
            if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", "Game was forced to end.").put("player", player.getPlayer().getName())));
                EngineAPI.getActiveGame().voidGame("an admin stopped the game");
                EngineAPI.getActiveGame().end(null);
            } else {
                if (EngineAPI.getGameStartingRunnable() != null) {
                    EngineAPI.getGameStartingRunnable().cancel();
                    EngineAPI.setGameStartingRunnable(null);
                }
                for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                    AuroraMCGamePlayer gp = (AuroraMCGamePlayer) pl;
                    gp.setTeam(null);
                    gp.setKit(null);
                    for (AuroraMCPlayer pl2 : AuroraMCAPI.getPlayers()) {
                        pl2.updateNametag(gp);
                    }
                }
                if (EngineAPI.getNextGame() != null) {
                    if (EngineAPI.getNextMap() != null) {
                        GameUtils.loadGame(EngineAPI.getNextGame(), EngineAPI.getNextMap(), EngineAPI.getNextVariation());
                    } else {
                        GameUtils.loadGame(EngineAPI.getNextGame(), EngineAPI.getNextVariation());
                    }
                    EngineAPI.setNextVariation(null);
                    EngineAPI.setNextGame(null);
                    EngineAPI.setNextMap(null);
                } else if (EngineAPI.getGameRotation().size() > 0) {
                    GameUtils.loadNextGame();
                } else {
                    EngineAPI.setActiveGameInfo(null);
                    EngineAPI.setActiveGame(null);
                    EngineAPI.setActiveMap(null);
                    EngineAPI.setServerState(ServerState.IDLE);
                    for (AuroraMCPlayer pl : AuroraMCAPI.getPlayers()) {
                        pl.getPlayer().getInventory().setItem(0, new ItemStack(Material.AIR));
                    }

                }
            }
            String message = AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "The game has been stopped by an admin.");
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                player1.sendMessage(message);
            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You cannot stop the game at this time."));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
