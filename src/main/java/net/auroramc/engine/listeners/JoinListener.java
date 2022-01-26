/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.events.player.PlayerObjectCreationEvent;
import net.auroramc.core.api.players.PlayerScoreboard;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.json.JSONArray;

import java.lang.reflect.Field;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        try {
            IChatBaseComponent header = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + ((EngineAPI.getActiveGameInfo() != null)?EngineAPI.getActiveGameInfo().getName().toUpperCase():EngineAPI.getServerState().getName().toUpperCase()) + "\",\"color\":\"dark_aqua\",\"bold\":\"true\"}");
            IChatBaseComponent footer = IChatBaseComponent.ChatSerializer.a("{\"text\": \"You are connected to server: " + AuroraMCAPI.getServerInfo().getName() + "\",\"color\":\"aqua\",\"bold\":\"true\"}");

            PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
            Field ff = packet.getClass().getDeclaredField("a");
            ff.setAccessible(true);
            ff.set(packet, header);

            ff = packet.getClass().getDeclaredField("b");
            ff.setAccessible(true);
            ff.set(packet, footer);

            ((CraftPlayer)e.getPlayer()).getHandle().playerConnection.sendPacket(packet);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }

        if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
            JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("players");
            if (spawnLocations == null || spawnLocations.length() > 0) {
                EngineAPI.getGameEngine().getLogger().info("An invalid waiting lobby was supplied, assuming 0, 64, 0 spawn position.");
                e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), 0, 64, 0));
            } else {
                int x, y, z;
                x = spawnLocations.getJSONObject(0).getInt("x");
                y = spawnLocations.getJSONObject(0).getInt("y");
                z = spawnLocations.getJSONObject(0).getInt("z");
                float yaw = spawnLocations.getJSONObject(0).getFloat("yaw");
                e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), x, y, z, yaw, 0));
            }
        } else if (EngineAPI.getActiveGame() != null) {
            EngineAPI.getActiveGame().onPlayerJoin(e.getPlayer());
        }
    }

    @EventHandler
    public void onObjectCreate(PlayerObjectCreationEvent e) {
        AuroraMCGamePlayer player = new AuroraMCGamePlayer(e.getPlayer());
        e.setPlayer(player);
        if ((EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) && EngineAPI.getActiveGame() != null) {
            EngineAPI.getActiveGame().onPlayerJoin(player);
        } else {
            PlayerScoreboard scoreboard = player.getScoreboard();
            scoreboard.setTitle("&3&l-= &b&l" + EngineAPI.getServerState().getName().toUpperCase() + "&r &3&l=-");
            scoreboard.setLine(11, "&b&l«GAME»");
            scoreboard.setLine(10, ((EngineAPI.getActiveGameInfo() != null)?EngineAPI.getActiveGameInfo().getName():"None   "));
            scoreboard.setLine(9, " ");
            scoreboard.setLine(8, "&b&l«MAP»");
            scoreboard.setLine(7, ((EngineAPI.getActiveMap() != null)?EngineAPI.getActiveMap().getName():"None  "));
            scoreboard.setLine(6, "  ");
            scoreboard.setLine(5, "&b&l«KIT»");
            scoreboard.setLine(4, ((player.getKit() != null)?player.getKit().getName():"None "));
            scoreboard.setLine(3, "   ");
            scoreboard.setLine(2, "&b&l«SERVER»");
            scoreboard.setLine(1, AuroraMCAPI.getServerInfo().getName());

            if (!player.isVanished() && EngineAPI.getServerState() != ServerState.STARTING && EngineAPI.getActiveGame() != null) {
                if (AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished()).count() >= AuroraMCAPI.getServerInfo().getServerType().getInt("min_players")) {
                    EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30));
                    EngineAPI.getGameStartingRunnable().runTaskTimer(AuroraMCAPI.getCore(), 0, 20);
                }
            }

            player.getPlayer().getInventory().setItem(8, EngineAPI.getLobbyItem().getItem());
            player.getPlayer().getInventory().setItem(7, EngineAPI.getPrefsItem().getItem());
            player.getPlayer().getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItem());

            if (EngineAPI.getActiveGame() != null) {
                player.getPlayer().getInventory().setItem(0, EngineAPI.getKitItem().getItem());
                if (EngineAPI.getActiveGame().getTeams().size() > 1) {
                    player.getPlayer().getInventory().setItem(0, EngineAPI.getTeamItem().getItem());
                }
            }
        }
    }

}
