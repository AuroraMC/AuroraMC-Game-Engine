/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.gui;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.utils.gui.GUI;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.players.PlayerKitLevel;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Kits extends GUI {

    private final AuroraMCGamePlayer player;

    public Kits(AuroraMCGamePlayer player) {
        super("&3&lSelect a kit!", (EngineAPI.getActiveGame().getKits().size() / 7) + 2, true);
        border("&3&lSelect a kit!","");

        this.player = player;

        int column = 1;
        int row = 1;
        for (Kit kit : EngineAPI.getActiveGame().getKits()) {
            if (kit.getCost() == -1 || (player.getUnlockedKits().containsKey(kit.getGameId()) && player.getUnlockedKits().get(kit.getGameId()).contains(kit.getId()))) {
                this.setItem(row, column, new GUIItem(kit.getMaterial(), "&3&l" + kit.getName(), 1, ";&7" + WordUtils.wrap(kit.getDescription(), 40, ";&7", false) + ";;&aLeft Click to equip the " + kit.getName() + "&a kit.;&aRight click to view kit levels.", (short)0, player.getKit().equals(kit)));
                column++;
                if (column == 8) {
                    row++;
                    column = 1;
                    if (row == 5) {
                        return;
                    }
                }
                continue;
            }
            this.setItem(row, column, new GUIItem(Material.BARRIER, "&c&l" + kit.getName(), 1, ";&7" + WordUtils.wrap(kit.getDescription(), 40, ";&7", false) + ";;&r&fCost: &6" + kit.getCost() + " Crowns&r&f;&r&f&aShift-Left-Click to purchase the " + kit.getName() + "&a kit.", (short)0, player.getKit().equals(kit)));
            column++;
            if (column == 8) {
                row++;
                column = 1;
                if (row == 5) {
                    return;
                }
            }
        }
    }

    @Override
    public void onClick(int row, int column, ItemStack item, ClickType clickType) {
        if (item.getType() == Material.STAINED_GLASS_PANE) {
            player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ITEM_BREAK, 100, 0);
            return;
        }
        Kit kit = EngineAPI.getActiveGame().getKits().get(((row - 1) * 7) + (column - 1));
        if (kit.equals(player.getKit())) {
            if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        PlayerKitLevel level = EngineDatabaseManager.getKitLevel(player, kit.getGameId(), kit.getId());
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                KitLevelMenu menu = new KitLevelMenu(player, level, kit);
                                menu.open(player);
                                AuroraMCAPI.openGUI(player, menu);
                            }
                        }.runTask(AuroraMCAPI.getCore());
                    }
                }.runTaskAsynchronously(AuroraMCAPI.getCore());
            } else {
                player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ITEM_BREAK, 100, 0);
            }
        } else {
            if (kit.getCost() == -1 || (player.getUnlockedKits().containsKey(kit.getGameId()) && player.getUnlockedKits().get(kit.getGameId()).contains(kit.getId()))) {
                if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.LEFT) {
                    player.setKit(kit);
                    player.getPlayer().closeInventory();
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You set your kit to **" + kit.getName() + "**."));
                    player.getScoreboard().setLine(6, ChatColor.stripColor(player.getKit().getName()) + " ");
                    if (!AuroraMCAPI.isTestServer()) {
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                EngineDatabaseManager.setDefaultKit(player.getId(), kit.getGameId(), kit.getId());
                            }
                        }.runTaskAsynchronously(AuroraMCAPI.getCore());
                    }
                } else if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            PlayerKitLevel level = EngineDatabaseManager.getKitLevel(player, kit.getGameId(), kit.getId());
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    KitLevelMenu menu = new KitLevelMenu(player, level, kit);
                                    menu.open(player);
                                    AuroraMCAPI.openGUI(player, menu);
                                }
                            }.runTask(AuroraMCAPI.getCore());
                        }
                    }.runTaskAsynchronously(AuroraMCAPI.getCore());
                }
            } else {
                if (clickType == ClickType.SHIFT_LEFT) {
                    if (player.getBank().getCrowns() >= kit.getCost() ) {
                        player.getBank().withdrawCrowns(kit.getCost(), false, true);
                        if (!player.getUnlockedKits().containsKey(kit.getGameId())) {
                            player.getUnlockedKits().put(kit.getGameId(), new ArrayList<>());
                        }
                        player.getUnlockedKits().get(kit.getGameId()).add(kit.getId());
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You unlocked and set your kit to **" + kit.getName() + "**."));
                        player.getPlayer().closeInventory();
                        player.setKit(kit);
                        player.getScoreboard().setLine(6, ChatColor.stripColor(player.getKit().getName()) + " ");
                        if (!AuroraMCAPI.isTestServer()) {
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    EngineDatabaseManager.setUnlockedKits(player.getId(), kit.getGameId(), player.getUnlockedKits().get(kit.getGameId()));
                                    EngineDatabaseManager.setDefaultKit(player.getId(), kit.getGameId(), kit.getId());
                                }
                            }.runTaskAsynchronously(AuroraMCAPI.getCore());
                        }
                    } else {
                        player.getPlayer().closeInventory();
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You have insufficient funds to buy kit **" + kit.getName() + "**."));
                    }
                } else {
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ITEM_BREAK, 100, 0);
                }
            }
        }
    }
}
