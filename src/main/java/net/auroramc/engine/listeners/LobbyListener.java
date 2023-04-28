/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.cosmetics.Cosmetic;
import net.auroramc.api.cosmetics.Gadget;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.events.block.BlockBreakEvent;
import net.auroramc.core.api.events.block.BlockPlaceEvent;
import net.auroramc.core.api.events.cosmetics.CosmeticEnableEvent;
import net.auroramc.core.api.events.cosmetics.CosmeticSwitchEvent;
import net.auroramc.core.api.events.entity.FoodLevelChangeEvent;
import net.auroramc.core.api.events.entity.PlayerDamageEvent;
import net.auroramc.core.api.events.inventory.InventoryClickEvent;
import net.auroramc.core.api.events.inventory.InventoryOpenEvent;
import net.auroramc.core.api.events.player.*;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
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
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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
                if (!e.getPlayer().getStats().getAchievementsGained().containsKey(AuroraMCAPI.getAchievement(5))) {
                    e.getPlayer().getStats().achievementGained(AuroraMCAPI.getAchievement(5), 1, true);
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
    public void onDamage(PlayerDamageEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getCause() == PlayerDamageEvent.DamageCause.VOID && EngineAPI.getServerState() != ServerState.ENDING) {
                JSONArray spawnLocations = EngineAPI.getWaitingLobbyMap().getMapData().getJSONObject("spawn").getJSONArray("PLAYERS");
                int x, y, z;
                x = spawnLocations.getJSONObject(0).getInt("x");
                y = spawnLocations.getJSONObject(0).getInt("y");
                z = spawnLocations.getJSONObject(0).getInt("z");
                float yaw = spawnLocations.getJSONObject(0).getFloat("yaw");
                e.getPlayer().teleport(new Location(Bukkit.getWorld("world"), x, y, z, yaw, 0));
                e.getPlayer().setFallDistance(0);
                e.getPlayer().setVelocity(new Vector());
            } else if (e.getCause() == PlayerDamageEvent.DamageCause.FIRE || e.getCause() == PlayerDamageEvent.DamageCause.FIRE_TICK || e.getCause() == PlayerDamageEvent.DamageCause.LAVA) {
                e.getPlayer().setFireTicks(0);
            }
            e.setCancelled(true);

        }


    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME) {
            if (e.getEntity() instanceof ArmorStand || e.getEntity() instanceof Painting || e.getEntity() instanceof ItemFrame) {
                e.setCancelled(true);
            }
        }

        if (e.getEntity() instanceof Rabbit && !((Rabbit) e.getEntity()).isAdult()) {
            if (e.getEntity().isInsideVehicle()) {
                if (e.getEntity().getVehicle() instanceof Damageable) {
                    e.setCancelled(true);
                    Damageable damageable = (Damageable) e.getEntity().getVehicle();
                    if (e instanceof EntityDamageByEntityEvent) {
                        damageable.damage(e.getDamage(), ((EntityDamageByEntityEvent) e).getDamager());
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
            if (e.getEntity().getPassenger() instanceof Rabbit && !((Rabbit) e.getEntity().getPassenger()).isAdult()) {
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
            if (e.getLevel() < 25) {
                e.setCancelled(true);
                e.setLevel(30);
            }
        }
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME || e.getWorld().getName().equals("world")) {
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
        for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
            updateHeaderFooter(player);
        }
        if (e.getState() != ServerState.ENDING && e.getState() != ServerState.IN_GAME) {
            for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                player.getScoreboard().setTitle("&3-= &b&l" + e.getState().getName().toUpperCase() + "&r &3=-");
                player.getScoreboard().setLine(12, ((EngineAPI.getActiveGameInfo() != null) ? EngineAPI.getActiveGameInfo().getName() : "None   "));
                player.getScoreboard().setLine(9, ((EngineAPI.getActiveMap() != null) ? EngineAPI.getActiveMap().getName() : "None  "));
                if (player instanceof AuroraMCGamePlayer) {
                    AuroraMCGamePlayer pl = (AuroraMCGamePlayer) player;
                    player.getScoreboard().setLine(6, ((pl.getKit() != null) ? pl.getKit().getName() : "None "));
                }

            }
        }
    }

    @EventHandler
    public void onPaintingBreak(HangingBreakEvent e) {
        if (EngineAPI.getServerState() != ServerState.ENDING && EngineAPI.getServerState() != ServerState.IN_GAME) {
            e.setCancelled(true);
        }
    }

    public static void updateHeaderFooter(AuroraMCServerPlayer player) {
        try {
            CraftPlayer player2 = player.getCraft();
            IChatBaseComponent header = IChatBaseComponent.ChatSerializer.a("{\"text\": \"§3§lAURORAMC NETWORK         §b§lAURORAMC.NET\",\"color\":\"dark_aqua\",\"bold\":\"false\"}");
            IChatBaseComponent footer = IChatBaseComponent.ChatSerializer.a("{\"text\": \"\n§fYou are currently connected to §b" + ((player.isDisguised() && player.getPreferences().isHideDisguiseNameEnabled())?"§oHidden":AuroraMCAPI.getInfo().getName()) + "\n\n" +
                    "§rStatus §3§l» §b" + EngineAPI.getServerState().getName() + "\n" +
                    "§rGame §3§l» §b" + ((EngineAPI.getActiveGameInfo() != null) ? EngineAPI.getActiveGameInfo().getName() : "None") + "\n" +
                    "§rMap §3§l» §b" + ((EngineAPI.getActiveMap() != null) ? EngineAPI.getActiveMap().getName() : "None") + "\n" +
                    "\",\"color\":\"aqua\",\"bold\":\"false\"}");

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
    public void onGadget(PlayerUseCosmeticEvent e) {
        if (EngineAPI.getServerState() == ServerState.IN_GAME || EngineAPI.getServerState() == ServerState.ENDING) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        if (EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
            if (e.getItem() != null && e.getItem().getType() != Material.AIR) {
                if (e.getPlayer().getInventory().getHeldItemSlot() == 3) {
                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) e.getPlayer();
                    if (player.getActiveCosmetics().containsKey(Cosmetic.CosmeticType.GADGET) && EngineAPI.getServerState() != ServerState.IN_GAME && EngineAPI.getServerState() != ServerState.ENDING) {
                        Gadget gadget = (Gadget) player.getActiveCosmetics().get(Cosmetic.CosmeticType.GADGET);
                        if (e.getItem().getType() == Material.FISHING_ROD && e.getClickedBlock() != null) {
                            return;
                        }
                        if (System.currentTimeMillis() - player.getLastUsed().getOrDefault(gadget, 0L) < gadget.getCooldown() * 1000L) {
                            double amount = ((player.getLastUsed().getOrDefault(gadget, 0L) + (gadget.getCooldown() * 1000L)) - System.currentTimeMillis()) / 100d;
                            long amount1 = Math.round(amount);
                            if (amount1 < 0) {
                                amount1 = 0;
                            }
                            player.sendMessage(TextFormatter.pluginMessage("Gadgets", "You cannot use this gadget for **" + (amount1 / 10f) + " seconds**."));
                            e.setUseItemInHand(Event.Result.DENY);
                            e.setUseInteractedBlock(Event.Result.DENY);
                            return;
                        }
                        if (gadget.getId() == 801) {
                            e.setCancelled(false);
                        }
                        if (e.getClickedBlock() != null) {
                            gadget.onUse(player, e.getClickedBlock().getLocation().getX(), e.getClickedBlock().getY(), e.getClickedBlock().getZ());
                        } else {
                            gadget.onUse(player, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                        }
                        player.getLastUsed().put(gadget, System.currentTimeMillis());
                    }
                    return;
                }
                switch (e.getItem().getType()) {
                    case EMERALD: {
                        e.setCancelled(true);
                        Cosmetics cosmetics = new Cosmetics(e.getPlayer());
                        cosmetics.open(e.getPlayer());
                        break;
                    }
                    case WOOD_DOOR: {
                        e.setCancelled(true);
                        ByteArrayDataOutput out = ByteStreams.newDataOutput();
                        out.writeUTF("Lobby");
                        out.writeUTF(e.getPlayer().getUniqueId().toString());
                        e.getPlayer().sendPluginMessage(out.toByteArray());
                        break;
                    }
                    case REDSTONE_COMPARATOR: {
                        e.setCancelled(true);
                        Preferences prefs = new Preferences(e.getPlayer());
                        prefs.open(e.getPlayer());
                        break;
                    }
                    case CHEST: {
                        e.setCancelled(true);
                        if (EngineAPI.getServerState() != ServerState.WAITING_FOR_PLAYERS && EngineAPI.getServerState() != ServerState.STARTING) {
                            return;
                        }
                        Kits kits = new Kits((AuroraMCGamePlayer) e.getPlayer());
                        kits.open(e.getPlayer());
                        break;
                    }
                    case LEATHER_CHESTPLATE: {
                        e.setCancelled(true);
                        if (EngineAPI.getServerState() != ServerState.WAITING_FOR_PLAYERS && EngineAPI.getServerState() != ServerState.STARTING) {
                            return;
                        }
                        Teams teams = new Teams((AuroraMCGamePlayer) e.getPlayer());
                        teams.open(e.getPlayer());
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (EngineAPI.getServerState() == ServerState.STARTING && EngineAPI.getGameStartingRunnable() != null && EngineAPI.getGameStartingRunnable().getStartTime() <= 5) {
            e.setCancelled(true);
            ServerAPI.closeGUI(e.getPlayer());
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
            if (e.getClickedInventory() instanceof PlayerInventory && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVanish(PlayerVanishEvent e) {
        if (EngineAPI.getServerState() == ServerState.ENDING || EngineAPI.getServerState() == ServerState.IN_GAME) {
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