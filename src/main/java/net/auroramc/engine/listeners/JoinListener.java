/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.cosmetics.Cosmetic;
import net.auroramc.core.api.cosmetics.ServerMessage;
import net.auroramc.core.api.events.player.PlayerObjectCreationEvent;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.players.scoreboard.PlayerScoreboard;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.JSONArray;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        LobbyListener.updateHeaderFooter((CraftPlayer) e.getPlayer());
        e.getPlayer().setFlying(false);
        e.getPlayer().setAllowFlight(false);
        e.getPlayer().setGameMode(GameMode.SURVIVAL);
        e.getPlayer().setHealth(20);
        e.getPlayer().setFoodLevel(30);
        e.getPlayer().getInventory().clear();
        e.getPlayer().getInventory().setArmorContents(new ItemStack[4]);
        e.getPlayer().setExp(0);
        e.getPlayer().setLevel(0);
        e.getPlayer().getEnderChest().clear();
        for (PotionEffect pe : e.getPlayer().getActivePotionEffects()) {
            e.getPlayer().removePotionEffect(pe.getType());
        }
        if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
            JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("PLAYERS");
            if (spawnLocations == null || spawnLocations.length() == 0) {
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
            if (EngineAPI.getWaitingLobbyMap().getMapData().getInt("time") > 12000) {
                e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 0, true, false), false);
            }

        } else if (EngineAPI.getActiveGame() != null) {
            //Hide spectators from the user joining.
            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                if (player instanceof AuroraMCGamePlayer) {
                    if (((AuroraMCGamePlayer)player).isSpectator() && !e.getPlayer().equals(player.getPlayer())) {
                        e.getPlayer().hidePlayer(player.getPlayer());
                    }
                }
            }
            if (EngineAPI.getActiveMap().getMapData().has("time")) {
                if (EngineAPI.getActiveMap().getMapData().getInt("time") > 12000) {
                    e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 1000000, 0, true, false), false);
                }
            }
            EngineAPI.getActiveGame().onPlayerJoin(e.getPlayer());
        }
    }

    @EventHandler
    public void onObjectCreate(PlayerObjectCreationEvent e) {
        AuroraMCGamePlayer player = new AuroraMCGamePlayer(e.getPlayer());
        e.setPlayer(player);
        if (!player.isVanished()) {
            String message;
            if (player.getActiveCosmetics().containsKey(Cosmetic.CosmeticType.SERVER_MESSAGE)) {
                message = ((ServerMessage)player.getActiveCosmetics().get(Cosmetic.CosmeticType.SERVER_MESSAGE)).onJoin(player);
            } else {
                message = ((ServerMessage)AuroraMCAPI.getCosmetics().get(400)).onJoin(player);
            }
            for (AuroraMCPlayer player1 : AuroraMCAPI.getPlayers()) {
                player1.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Join", message));
            }
        }
        if ((EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) && EngineAPI.getActiveGame() != null) {
            EngineAPI.getActiveGame().onPlayerJoin(player);
        } else {
            PlayerScoreboard scoreboard = player.getScoreboard();
            scoreboard.setTitle("&3-= &b&l" + EngineAPI.getServerState().getName().toUpperCase() + "&r &3=-");
            scoreboard.setLine(13, "&b&l«GAME»");
            scoreboard.setLine(12, ((EngineAPI.getActiveGameInfo() != null)?EngineAPI.getActiveGameInfo().getName():"None   "));
            scoreboard.setLine(11, " ");
            scoreboard.setLine(10, "&b&l«MAP»");
            scoreboard.setLine(9, ((EngineAPI.getActiveMap() != null)?EngineAPI.getActiveMap().getName():"None  "));
            scoreboard.setLine(8, "  ");
            scoreboard.setLine(7, "&b&l«KIT»");
            scoreboard.setLine(6, ((player.getKit() != null)? ChatColor.stripColor(player.getKit().getName()) :"None "));
            scoreboard.setLine(5, "   ");
            scoreboard.setLine(4, "&b&l«SERVER»");
            if (player.getPreferences().isHideDisguiseNameEnabled() && player.isDisguised()) {
                scoreboard.setLine(3, "&oHidden");
            } else {
                scoreboard.setLine(3, AuroraMCAPI.getServerInfo().getName());
            }
            scoreboard.setLine(2, "    ");
            scoreboard.setLine(1, "&7auroramc.net");

            if (!player.isVanished() && EngineAPI.getServerState() == ServerState.WAITING_FOR_PLAYERS && EngineAPI.getActiveGame() != null) {
                if (AuroraMCAPI.getPlayers().stream().filter(player1 -> !player1.isVanished()).count() >= AuroraMCAPI.getServerInfo().getServerType().getInt("min_players")) {
                    EngineAPI.setServerState(ServerState.STARTING);
                    EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30));
                    EngineAPI.getGameStartingRunnable().runTaskTimer(AuroraMCAPI.getCore(), 0, 20);
                    int kitId = EngineDatabaseManager.getDefaultKit(player.getId(), EngineAPI.getActiveGameInfo().getId());
                    for (Kit kit : EngineAPI.getActiveGame().getKits()) {
                        if (kitId == kit.getId()) {
                            player.setKit(kit);
                            break;
                        }
                    }
                    if (player.getKit() == null) {
                        player.setKit(EngineAPI.getActiveGame().getKits().get(0));
                    }
                }
            }

            if (EngineAPI.getActiveGame() != null) {
                if (!player.isSpectator() && !player.isVanished() && player.getKit() == null) {
                    int kitId = EngineDatabaseManager.getDefaultKit(player.getId(), EngineAPI.getActiveGameInfo().getId());
                    for (Kit kit : EngineAPI.getActiveGame().getKits()) {
                        if (kitId == kit.getId()) {
                            player.setKit(kit);
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "Your kit was set to **" + kit.getName() + "**."));
                            scoreboard.setLine(6, kit.getName());
                            break;
                        }
                    }
                    if (player.getKit() == null) {
                        player.setKit(EngineAPI.getActiveGame().getKits().get(0));
                        scoreboard.setLine(6, EngineAPI.getActiveGame().getKits().get(0).getName());
                    }
                }
            }

            player.getPlayer().getInventory().setItem(8, EngineAPI.getLobbyItem().getItem());
            player.getPlayer().getInventory().setItem(7, EngineAPI.getPrefsItem().getItem());
            player.getPlayer().getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItem());

            if (EngineAPI.getActiveGame() != null) {
                player.getPlayer().getInventory().setItem(0, EngineAPI.getKitItem().getItem());
                if (EngineAPI.getActiveGame().getTeams().size() > 1 && !EngineAPI.getActiveGameInfo().hasTeamCommand() && !EngineAPI.isTeamBalancingEnabled()) {
                    player.getPlayer().getInventory().setItem(1, EngineAPI.getTeamItem().getItem());
                }
            }
        }
    }

}
