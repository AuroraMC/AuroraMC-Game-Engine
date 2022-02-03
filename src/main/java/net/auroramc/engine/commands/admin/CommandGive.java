/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandGive extends Command {


    public CommandGive() {
        super("give", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (args.size() >= 2) {
            String target = args.remove(0);
            String[] items = args.remove(0).split(",");

            List<Material> materials = new ArrayList<>();

            outer:
            for (String item : items) {
                if (item.equals("")) {
                    continue;
                }
                List<String> matches = new ArrayList<>();
                for (Material material : Material.values()) {
                    if (material.name().equalsIgnoreCase(item)) {
                        materials.add(material);
                        continue outer;
                    }
                    if (material.name().startsWith(item.toUpperCase())) {
                        matches.add(material.name());
                    }
                }

                if (matches.size() == 0) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "No matches were found for material **" + item + "**."));
                    continue;
                }

                if (matches.size() > 1) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "Multiple possible matches found for material **" + item + "**. Please be more specific. Matches: [**" + String.join("**, **", matches) + "**]"));
                    continue;
                }

                materials.add(Material.getMaterial(matches.get(0)));
            }

            if (materials.size() == 0) {
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "No items provided were valid."));
                return;
            }

            int amount = 1;
            String name = null;
            Map<Enchantment, Integer> enchantments = new HashMap<>();

            if (args.size() >= 1) {
                try {
                    amount = Integer.parseInt(args.remove(0));
                    if (amount < 1) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "Invalid syntax. Correct syntax: **/give <player|all> <item> [amount] [name] [enchantments...]**"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "Invalid syntax. Correct syntax: **/give <player|all> <item> [amount] [name] [enchantments...]**"));
                    return;
                }
            }

            if (args.size() >= 1) {
                name = AuroraMCAPI.getFormatter().convert(AuroraMCAPI.getFormatter().highlight(args.remove(0).replace("_"," ")));
            }

            if (args.size() >= 1) {
                for (String arg : args) {
                    String[] ench = arg.split(":");
                    if (ench.length == 2) {
                        Enchantment enchantment = Enchantment.getByName(ench[0].toUpperCase());
                        int level;
                        if (enchantment == null) {
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "No matches for enchantment **" + ench[0] + "**."));
                            return;
                        }
                        try {
                            level = Integer.parseInt(ench[1]);
                            if (level < 1) {
                                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "An enchantment was not in a valid format. Please format enchantments as: **enchantment:level**"));
                                return;
                            }
                        } catch (NumberFormatException e) {
                            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "An enchantment was not in a valid format. Please format enchantments as: **enchantment:level**"));
                            return;
                        }
                        enchantments.put(enchantment, level);
                    } else {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "An enchantment was not in a valid format. Please format enchantments as: **enchantment:level**"));
                        return;
                    }
                }
            }

            List<ItemStack> itemsToGive = new ArrayList<>();

            for (Material material : materials) {
                int stacks = amount / material.getMaxStackSize();
                int singles = amount % material.getMaxStackSize();

                ItemStack itemStack = new ItemStack(material, material.getMaxStackSize());
                if (name != null) {
                    ItemMeta meta = itemStack.getItemMeta();
                    meta.setDisplayName(name);
                    itemStack.setItemMeta(meta);
                }
                itemStack.addUnsafeEnchantments(enchantments);
                while (stacks > 0) {
                    itemsToGive.add(itemStack);
                    stacks--;
                }
                if (singles > 0) {
                    ItemStack is = itemStack.clone();
                    is.setAmount(singles);
                    itemsToGive.add(is);
                }
            }

            ItemStack[] is = itemsToGive.toArray(new ItemStack[0]);


            if (target.equalsIgnoreCase("all")) {
                for (AuroraMCPlayer player1 : AuroraMCAPI.getPlayers()) {
                    player1.getPlayer().getInventory().addItem(is);
                    for (Material material : materials) {
                        player1.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "You were given **" + amount + " " + WordUtils.capitalizeFully(material.name().replace("_", " ")) + "s** by **" + player.getPlayer().getName() + "**."));
                    }
                }
                for (Material material : materials) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "You gave **" + amount + " " + WordUtils.capitalizeFully(material.name().replace("_", " ")) + "** to **" + AuroraMCAPI.getPlayers().size() + "** players."));
                }
            } else {
                String[] targets = target.split(",");
                int players = 0;
                for (String target1 : targets) {
                    AuroraMCPlayer player1 = AuroraMCAPI.getPlayer(target1);
                    if (player1 != null) {
                        player1.getPlayer().getInventory().addItem(is);
                        players++;
                        for (Material material : materials) {
                            player1.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "You were given **" + amount + " " + WordUtils.capitalizeFully(material.name().replace("_", " ")) + "s** by **" + player.getPlayer().getName() + "**."));
                        }
                    } else {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "Target **" + target1 + "** not found."));
                    }
                }
                if (players == 0) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "No targets were found, so nothing was given out."));
                } else {
                    for (Material material : materials) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "You gave **" + amount + " " + WordUtils.capitalizeFully(material.name().replace("_", " ")) + "** to **" + players + "** players."));
                    }
                }
            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Give", "Invalid syntax. Correct syntax: **/give <player|all> <item> [amount] [name] [enchantments...]**"));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
