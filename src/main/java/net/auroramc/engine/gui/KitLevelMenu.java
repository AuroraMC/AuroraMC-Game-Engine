/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.gui;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.utils.LevelUtils;
import net.auroramc.core.api.utils.gui.GUI;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.players.PlayerKitLevel;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

public class KitLevelMenu extends GUI {

    private final AuroraMCGamePlayer player;
    private final PlayerKitLevel kitLevel;
    private final Kit kit;

    public KitLevelMenu(AuroraMCGamePlayer player, PlayerKitLevel level, Kit kit) {
        super("&3&l" + kit.getName() + " Kit Levels", 2, true);
        border("&3&l" + kit.getName() + " Kit Levels", "");

        this.kit = kit;
        this.kitLevel = level;
        this.player = player;

        this.setItem(0, 0, new GUIItem(Material.ARROW, "&3&lBack"));
        this.setItem(0, 4, new GUIItem(kit.getMaterial(), "&3&l" + kit.getName(), 1, ";&7" + WordUtils.wrap(kit.getDescription(), 40, ";&7", false) + ""));

        this.setItem(1, 4, new GUIItem(Material.STAINED_GLASS_PANE, "&b&lLevel " + level.getLevel(), 1, ";&rRewards:;**" + ((EngineAPI.getKitLevelRewards().containsKey(level.getLevel()))?EngineAPI.getKitLevelRewards().get(level.getLevel()).getRewardString():"None") + "**;;&aThis is your current level.", (short)5));

        if (level.getLatestUpgrade() < level.getLevel() / 20) {
            int cost;
            switch (level.getLevel()) {
                case 0: {
                    cost = 25000;
                    break;
                }
                case 1: {
                    cost = 75000;
                    break;
                }
                case 2: {
                    cost = 125000;
                    break;
                }
                case 3: {
                    cost = 250000;
                    break;
                }
                case 4: {
                    cost = 750000;
                    break;
                }
                default: {
                    cost = -1;
                    break;
                }
            }
            this.setItem(2, 4, new GUIItem(Material.DIAMOND, "&a&lClick to upgrade your kit!", 1, ";&rCurrent Upgrade: **Level " + level.getLatestUpgrade() + "**;&rUpgrade to: **" + level.getLatestUpgrade() + "**;;&rCost: &6" + cost + " Crowns"));
        }

        int col = 3;
        int lvl = level.getLevel() - 1;

        while (lvl >= 0 && col >= 1) {
            this.setItem(1, col, new GUIItem(Material.STAINED_GLASS_PANE, "&b&lLevel " + lvl, 1, ";&rRewards:;**" + ((EngineAPI.getKitLevelRewards().containsKey(lvl))?EngineAPI.getKitLevelRewards().get(lvl).getRewardString():"None") + "**;;&aYou have already received this reward."));
            col--;
            lvl--;
        }

        col = 5;
        lvl = level.getLevel() + 1;

        String levelHover = null;
        if (level.getLevel() != 100) {
            String progress = "||||||||||||||||||||||||||||||";
            double percentage = (((double) level.getXpIntoLevel() / LevelUtils.xpForLevel(level.getLevel() + 1))*100);
            if (level.getLevel() != 100) {
                int amountToColour = (int) Math.floor(((percentage) / 100)*30);
                progress = ((progress.substring(0, amountToColour) + "&r&l" + progress.substring(amountToColour + 1)));
            } else {
                percentage = 100.0;
            }
            levelHover = AuroraMCAPI.getFormatter().convert(AuroraMCAPI.getFormatter().highlight(String.format("&r &3&l«%s» &r&b&l%s&r &3&l«%s»;&rProgress to Next Level: **%s%%**", level.getLevel() - ((level.getLevel() == 100)?1:0), progress, level.getLevel() + ((level.getLevel() != 100)?1:0), new DecimalFormat("##.#").format(percentage))));
        }

        while (lvl <= 100 && col <= 7) {
            this.setItem(1, col, new GUIItem(Material.STAINED_GLASS_PANE, "&b&lLevel " + lvl, 1, ";&rRewards:;**&kReward**" + ((col == 5 && levelHover != null)?";;" + levelHover:""), (short)14));
            col++;
            lvl++;
        }
    }

    @Override
    public void onClick(int row, int column, ItemStack itemClicked, ClickType clickType) {
        if (itemClicked.getType() == Material.DIAMOND) {
            int cost;
            switch (kitLevel.getLevel()) {
                case 0: {
                    cost = 25000;
                    break;
                }
                case 1: {
                    cost = 75000;
                    break;
                }
                case 2: {
                    cost = 125000;
                    break;
                }
                case 3: {
                    cost = 250000;
                    break;
                }
                case 4: {
                    cost = 750000;
                    break;
                }
                default: {
                    cost = -1;
                    break;
                }
            }
            if (player.getBank().getCrowns() >= cost) {
                player.getBank().withdrawCrowns(cost, false, true);
                kitLevel.upgrade();
                player.getPlayer().closeInventory();
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You purchased **" + kit.getName() + " Kit Level " + kitLevel.getLevel() + "**!"));
            } else {
                player.getPlayer().closeInventory();
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Game Manager", "You have insufficient funds to buy this kit upgrade!"));
            }
        } else if (itemClicked.getType() == Material.ARROW) {
            Kits kits = new Kits(player);
            kits.open(player);
            AuroraMCAPI.openGUI(player, kits);
        }
    }
}
