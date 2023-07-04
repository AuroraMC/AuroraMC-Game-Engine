/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.gui;

import net.auroramc.api.utils.LevelUtils;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.utils.gui.GUI;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.Kit;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.players.PlayerKitLevel;
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

        this.setItem(1, 4, new GUIItem(Material.STAINED_GLASS_PANE, "&b&lLevel " + level.getLevel(), 1, ";&r&fRewards:;**" + ((EngineAPI.getKitLevelRewards().containsKey(level.getLevel()))?EngineAPI.getKitLevelRewards().get(level.getLevel()).getRewardString():"None") + "**;;&aThis is your current level.", (short)5));

        if (level.getLatestUpgrade() < level.getLevel() / 20) {
            int cost;
            switch (level.getLatestUpgrade()) {
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
            this.setItem(2, 4, new GUIItem(Material.DIAMOND, "&a&lClick to upgrade your kit!", 1, ";&r&fCurrent Upgrade: **Level " + level.getLatestUpgrade() + "**;&r&fUpgrade to: **" + (level.getLatestUpgrade() + 1) + "**;;&r&fUnlocks:;&r&f - **" + kit.getUpgradeReward(level.getLatestUpgrade() + 1) + "**;&r&fCost: &6" + cost + " Crowns"));
        }

        int col = 3;
        int lvl = level.getLevel() - 1;

        while (lvl >= 0 && col >= 1) {
            this.setItem(1, col, new GUIItem(Material.STAINED_GLASS_PANE, "&b&lLevel " + lvl, 1, ";&r&fRewards:;**" + ((EngineAPI.getKitLevelRewards().containsKey(lvl))?EngineAPI.getKitLevelRewards().get(lvl).getRewardString():"None") + "**;;&aYou have already received this reward."));
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
                progress = ((progress.substring(0, amountToColour) + "&r&f&l" + progress.substring(amountToColour + 1)));
            } else {
                percentage = 100.0;
            }
            levelHover = TextFormatter.convert(TextFormatter.highlightRaw(String.format("&r&f &3&l«%s» &r&f&b&l%s&r&f &3&l«%s»;&r&fProgress to Next Level: **%s%%**", level.getLevel() - ((level.getLevel() == 100)?1:0), progress, level.getLevel() + ((level.getLevel() != 100)?1:0), new DecimalFormat("##.#").format(percentage))));
        }

        while (lvl <= 100 && col <= 7) {
            this.setItem(1, col, new GUIItem(Material.STAINED_GLASS_PANE, "&b&lLevel " + lvl, 1, ";&r&fRewards:;**&kReward**" + ((col == 5 && levelHover != null)?";;" + levelHover:""), (short)14));
            col++;
            lvl++;
        }
    }

    @Override
    public void onClick(int row, int column, ItemStack itemClicked, ClickType clickType) {
        if (itemClicked.getType() == Material.DIAMOND) {
            int cost;
            switch (kitLevel.getLatestUpgrade()) {
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
                player.closeInventory();
                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You purchased **" + kit.getName() + " Kit Level " + kitLevel.getLatestUpgrade() + "**!"));
            } else {
                player.closeInventory();
                player.sendMessage(TextFormatter.pluginMessage("Game Manager", "You have insufficient funds to buy this kit upgrade!"));
            }
        } else if (itemClicked.getType() == Material.ARROW) {
            Kits kits = new Kits(player);
            kits.open(player);
        }
    }
}
