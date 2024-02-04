/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.backend.info.ServerInfo;
import net.auroramc.api.cosmetics.Cosmetic;
import net.auroramc.api.cosmetics.ServerMessage;
import net.auroramc.api.permissions.Rank;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.events.player.PlayerObjectCreationEvent;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.core.api.player.scoreboard.PlayerScoreboard;
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
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;

import java.util.Map;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        Rank rank = AuroraMCAPI.getDbManager().getRank(e.getUniqueId());
        boolean isVanished = AuroraMCAPI.getDbManager().isVanished(e.getUniqueId());
        if (!(rank.hasPermission("moderation") && isVanished) && !rank.hasPermission("master")) {
            if (ServerAPI.getPlayers().stream().filter(player -> !player.isVanished()).count() >= ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("max_players") && ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().has("enforce_limit")) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, "This server is currently full. In order to bypass this, you need to purchase a rank!");
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
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
            for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                if (player instanceof AuroraMCGamePlayer) {
                    if (((AuroraMCGamePlayer)player).isSpectator() && !e.getPlayer().equals(player.getCraft())) {
                        e.getPlayer().hidePlayer(player.getCraft());
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
        LobbyListener.updateHeaderFooter(player);
        e.setPlayer(player);
        if (!player.isVanished()) {
            ServerMessage message = ((ServerMessage)player.getActiveCosmetics().getOrDefault(Cosmetic.CosmeticType.SERVER_MESSAGE, AuroraMCAPI.getCosmetics().get(400)));
            for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
                player1.sendMessage(TextFormatter.pluginMessage("Join", TextFormatter.convert(message.onJoin(player1, player))));
            }
        }
        if ((EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) && EngineAPI.getActiveGame() != null) {
            for (Map.Entry<Cosmetic.CosmeticType, Cosmetic> entry : player.getActiveCosmetics().entrySet()) {
                if (entry.getKey() == Cosmetic.CosmeticType.GADGET || entry.getKey() == Cosmetic.CosmeticType.BANNER || entry.getKey() == Cosmetic.CosmeticType.HAT  || entry.getKey() == Cosmetic.CosmeticType.PARTICLE) {
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            entry.getValue().onUnequip(player);
                        }
                    }.runTaskLater(ServerAPI.getCore(), 1);
                    player.sendMessage(TextFormatter.pluginMessage("Cosmetics", String.format("%s **%s** has been unequipped during the game.", entry.getKey().getName(), entry.getValue().getName())));
                }
            }
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
                scoreboard.setLine(3, AuroraMCAPI.getInfo().getName());
            }
            scoreboard.setLine(2, "    ");
            scoreboard.setLine(1, "&7auroramc.net");

            if (!player.isVanished() && EngineAPI.getServerState() == ServerState.WAITING_FOR_PLAYERS && EngineAPI.getActiveGame() != null && EngineAPI.getGameStartingRunnable() == null) {
                if (ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && ((AuroraMCGamePlayer)player1).isOptedSpec()).count() >= ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("min_players")) {
                    EngineAPI.setServerState(ServerState.STARTING);
                    EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30, false));
                    EngineAPI.getGameStartingRunnable().runTaskTimer(ServerAPI.getCore(), 0, 20);
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
                            player.sendMessage(TextFormatter.pluginMessage("Game Manager", "Your kit was set to **" + TextFormatter.convert(kit.getName()) + "**."));
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

            player.getInventory().setItem(8, EngineAPI.getLobbyItem().getItemStack());
            player.getInventory().setItem(7, EngineAPI.getPrefsItem().getItemStack());
            player.getInventory().setItem(4, EngineAPI.getCosmeticsItem().getItemStack());

            if (EngineAPI.getActiveGame() != null) {
                player.getInventory().setItem(0, EngineAPI.getKitItem().getItemStack());
                if (EngineAPI.getActiveGame().getTeams().size() > 1 && !EngineAPI.getActiveGameInfo().hasTeamCommand() && !EngineAPI.isTeamBalancingEnabled()) {
                    player.getInventory().setItem(1, EngineAPI.getTeamItem().getItemStack());
                }
            }
        }
    }

}
