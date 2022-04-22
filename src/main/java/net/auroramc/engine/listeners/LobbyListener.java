/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.events.VanishEvent;
import net.auroramc.core.api.events.cosmetics.CosmeticEnableEvent;
import net.auroramc.core.api.events.cosmetics.CosmeticSwitchEvent;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.gui.cosmetics.Cosmetics;
import net.auroramc.core.gui.preferences.Preferences;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.events.ServerStateChangeEvent;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.gui.Kits;
import net.auroramc.engine.gui.Teams;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.json.JSONArray;

import java.lang.reflect.Field;

/**
 * All of these listeners will take over the second the game ends or when the server is in the lobby.
 */
public class LobbyListener implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                if (!AuroraMCAPI.getPlayer(e.getPlayer()).getStats().getAchievementsGained().containsKey(AuroraMCAPI.getAchievement(5))) {
                    AuroraMCAPI.getPlayer(e.getPlayer()).getStats().achievementGained(AuroraMCAPI.getAchievement(5), 1, true);
                }
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getEntity() instanceof Player) {
                if (e.getCause() == EntityDamageEvent.DamageCause.VOID && EngineAPI.getServerState() != ServerState.ENDING) {
                    JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("PLAYERS");
                    int x, y, z;
                    x = spawnLocations.getJSONObject(0).getInt("x");
                    y = spawnLocations.getJSONObject(0).getInt("y");
                    z = spawnLocations.getJSONObject(0).getInt("z");
                    float yaw = spawnLocations.getJSONObject(0).getFloat("yaw");
                    e.getEntity().teleport(new Location(Bukkit.getWorld("world"), x, y, z, yaw, 0));
                    e.getEntity().setFallDistance(0);
                    e.getEntity().setVelocity(new Vector());
                } else if (e.getCause() == EntityDamageEvent.DamageCause.FIRE || e.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                    e.getEntity().setFireTicks(0);
                }
                e.setCancelled(true);

            } else if (e.getEntity() instanceof ArmorStand || e.getEntity() instanceof Painting || e.getEntity() instanceof ItemFrame) {
                e.setCancelled(true);
            }
        }

        if (e.getEntity() instanceof Rabbit && !((Rabbit)e.getEntity()).isAdult()) {
            if (e.getEntity().isInsideVehicle()) {
                if (e.getEntity().getVehicle() instanceof Damageable) {
                    e.setCancelled(true);
                    Damageable damageable = (Damageable) e.getEntity().getVehicle();
                    if (e instanceof EntityDamageByEntityEvent) {
                        damageable.damage(e.getDamage(), ((EntityDamageByEntityEvent)e).getDamager());
                    } else {
                        damageable.damage(e.getDamage());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (e.getEntity().getPassenger() != null) {
            if (e.getEntity().getPassenger() instanceof Rabbit && !((Rabbit)e.getEntity().getPassenger()).isAdult()) {
                if (e.getEntity().getPassenger().getPassenger() != null) {
                    e.getEntity().getPassenger().getPassenger().remove();
                }
                e.getEntity().getPassenger().remove();
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getEntity() instanceof Player && e.getFoodLevel() < 25) {
                e.setCancelled(true);
                e.setFoodLevel(30);
            }
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.toWeatherState()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onStateChange(ServerStateChangeEvent e) {
        if (e.getState() != ServerState.ENDING && e.getState() != ServerState.IN_GAME) {
            for (AuroraMCPlayer player : AuroraMCAPI.getPlayers()) {
                player.getScoreboard().setTitle("&3-= &b&l" + e.getState().getName().toUpperCase() + "&r &3=-");
                player.getScoreboard().setLine(12, ((EngineAPI.getActiveGameInfo() != null)?EngineAPI.getActiveGameInfo().getName():"None   "));
                player.getScoreboard().setLine(9, ((EngineAPI.getActiveMap() != null)?EngineAPI.getActiveMap().getName():"None  "));
                if (player instanceof AuroraMCGamePlayer) {
                    AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;
                    player.getScoreboard().setLine(6, ((pl.getKit() != null)?pl.getKit().getName():"None "));
                }
                updateHeaderFooter((CraftPlayer) player.getPlayer());
            }
        }
    }

    @EventHandler
    public void onPaintingBreak(HangingBreakEvent e) {
        if (EngineAPI.getServerState() != ServerState.ENDING && EngineAPI.getServerState() != ServerState.IN_GAME) {
            e.setCancelled(true);
        }
    }

    public static void updateHeaderFooter(CraftPlayer player2) {
        try {
            IChatBaseComponent header = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + ((EngineAPI.getActiveGameInfo() != null)?EngineAPI.getActiveGameInfo().getName().toUpperCase():EngineAPI.getServerState().getName().toUpperCase()) + "\",\"color\":\"dark_aqua\",\"bold\":\"true\"}");
            IChatBaseComponent footer = IChatBaseComponent.ChatSerializer.a("{\"text\": \"Purchase ranks, cosmetics and more at store.auroramc.net!\",\"color\":\"aqua\",\"bold\":\"false\"}");

            PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
            Field ff = packet.getClass().getDeclaredField("a");
            ff.setAccessible(true);
            ff.set(packet, header);

            ff = packet.getClass().getDeclaredField("b");
            ff.setAccessible(true);
            ff.set(packet, footer);

            player2.getHandle().playerConnection.sendPacket(packet);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
            if (e.getItem() != null && e.getItem().getType() != Material.AIR) {
                switch (e.getItem().getType()) {
                    case EMERALD: {
                        e.setCancelled(true);
                        AuroraMCPlayer player = AuroraMCAPI.getPlayer(e.getPlayer());
                        Cosmetics cosmetics = new Cosmetics(player);
                        cosmetics.open(player);
                        AuroraMCAPI.openGUI(player, cosmetics);
                        break;
                    }
                    case WOOD_DOOR: {
                        e.setCancelled(true);
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Lobby");
                        out.writeUTF(e.getPlayer().getUniqueId().toString());
                        e.getPlayer().sendPluginMessage(AuroraMCAPI.getCore(), "BungeeCord", out.toByteArray());
                        break;
                    }
                    case REDSTONE_COMPARATOR: {
                        e.setCancelled(true);
                        AuroraMCPlayer player = AuroraMCAPI.getPlayer(e.getPlayer());
                        Preferences prefs = new Preferences(player);
                        prefs.open(player);
                        AuroraMCAPI.openGUI(player, prefs);
                        break;
                    }
                    case CHEST: {
                        e.setCancelled(true);
                        AuroraMCPlayer player = AuroraMCAPI.getPlayer(e.getPlayer());
                        Kits kits = new Kits((AuroraMCGamePlayer) player);
                        kits.open(player);
                        AuroraMCAPI.openGUI(player, kits);
                        break;
                    }
                    case LEATHER_CHESTPLATE: {
                        e.setCancelled(true);
                        AuroraMCPlayer player = AuroraMCAPI.getPlayer(e.getPlayer());
                        Teams teams = new Teams((AuroraMCGamePlayer) player);
                        teams.open(player);
                        AuroraMCAPI.openGUI(player, teams);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (EngineAPI.getServerState() != ServerState.ENDING && EngineAPI.getServerState() != ServerState.IN_GAME && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInvMove(InventoryClickEvent e) {
        if (EngineAPI.getServerState() != ServerState.ENDING && EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getClickedInventory() instanceof PlayerInventory && e.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVanish(VanishEvent e) {
        if (e.isVanish() && (EngineAPI.getServerState() == ServerState.ENDING || EngineAPI.getServerState() == ServerState.IN_GAME)) {
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onCosmeticEnable(CosmeticEnableEvent e) {
        if (EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onCosmeticSwitch(CosmeticSwitchEvent e) {
        if (EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) {
            e.setCancelled(true);
        }
    }

}
