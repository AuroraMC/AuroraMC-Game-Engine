/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.permissions.Permission;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.ServerCommand;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.GameSession;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;

public class CommandGive extends ServerCommand {


    public CommandGive() {
        super("give", Collections.emptyList(), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCServerPlayer player, String aliasUsed, List<String> args) {
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
                    player.sendMessage(TextFormatter.pluginMessage("Give", "No matches were found for material **" + item + "**."));
                    continue;
                }

                if (matches.size() > 1) {
                    player.sendMessage(TextFormatter.pluginMessage("Give", "Multiple possible matches found for material **" + item + "**. Please be more specific. Matches: [**" + String.join("**, **", matches) + "**]"));
                    continue;
                }

                materials.add(Material.getMaterial(matches.get(0)));
            }

            if (materials.size() == 0) {
                player.sendMessage(TextFormatter.pluginMessage("Give", "No items provided were valid."));
                return;
            }

            int amount = 1;
            String name = null;
            Map<Enchantment, Integer> enchantments = new HashMap<>();

            if (args.size() >= 1) {
                try {
                    amount = Integer.parseInt(args.remove(0));
                    if (amount < 1) {
                        player.sendMessage(TextFormatter.pluginMessage("Give", "Invalid syntax. Correct syntax: **/give <player|all> <item> [amount] [name] [enchantments...]**"));
                        return;
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(TextFormatter.pluginMessage("Give", "Invalid syntax. Correct syntax: **/give <player|all> <item> [amount] [name] [enchantments...]**"));
                    return;
                }
            }

            if (args.size() >= 1) {
                name = TextFormatter.convert(TextFormatter.highlightRaw(args.remove(0).replace("_"," ")));
            }

            if (args.size() >= 1) {
                for (String arg : args) {
                    String[] ench = arg.split(":");
                    if (ench.length == 2) {
                        Enchantment enchantment = Enchantment.getByName(ench[0].toUpperCase());
                        int level;
                        if (enchantment == null) {
                            player.sendMessage(TextFormatter.pluginMessage("Give", "No matches for enchantment **" + ench[0] + "**."));
                            return;
                        }
                        try {
                            level = Integer.parseInt(ench[1]);
                            if (level < 1) {
                                player.sendMessage(TextFormatter.pluginMessage("Give", "An enchantment was not in a valid format. Please format enchantments as: **enchantment:level**"));
                                return;
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(TextFormatter.pluginMessage("Give", "An enchantment was not in a valid format. Please format enchantments as: **enchantment:level**"));
                            return;
                        }
                        enchantments.put(enchantment, level);
                    } else {
                        player.sendMessage(TextFormatter.pluginMessage("Give", "An enchantment was not in a valid format. Please format enchantments as: **enchantment:level**"));
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


            if (target.equalsIgnoreCase("all")) {
                int playersInGame = 0;
                List<String> item = new ArrayList<>();
                for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
                    if (!player1.isVanished() && !((AuroraMCGamePlayer)player1).isSpectator()) {
                        playersInGame++;
                    }
                    for (ItemStack is : itemsToGive) {
                        player1.getInventory().addItem(is.clone());
                    }
                    for (Material material : materials) {
                        player1.sendMessage(TextFormatter.pluginMessage("Give", "You were given **" + amount + " " + WordUtils.capitalizeFully(material.name().replace("_", " ")) + ((amount > 1)?"s":"") + "** by **" + player.getName() + "**."));
                        item.add(WordUtils.capitalizeFully(material.name().replace("_", " ")));
                    }
                }
                for (Material material : materials) {
                    player.sendMessage(TextFormatter.pluginMessage("Give", "You gave **" + amount + " " + WordUtils.capitalizeFully(material.name().replace("_", " ")) + ((amount > 1)?"s":"") + "** to **" + ServerAPI.getPlayers().size() + "** players."));
                }
                if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                    if (playersInGame > 0) {
                        EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                    }
                    EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", amount + " " + String.join(", ", item) + " given to all players.").put("player", player.getName())));
                }
            } else {
                String[] targets = target.split(",");
                int players = 0;
                int playersInGame = 0;
                List<String> item = new ArrayList<>();
                for (String target1 : targets) {
                    AuroraMCServerPlayer player1 = ServerAPI.getPlayer(target1);
                    if (player1 != null) {
                        if (!player1.isVanished() && !((AuroraMCGamePlayer)player1).isSpectator()) {
                            playersInGame++;
                        }
                        for (ItemStack is : itemsToGive) {
                            player1.getInventory().addItem(is.clone());
                        }
                        players++;
                        for (Material material : materials) {
                            player1.sendMessage(TextFormatter.pluginMessage("Give", "You were given **" + amount + " " + WordUtils.capitalizeFully(material.name().replace("_", " ")) + ((amount > 1)?"s":"") + "** by **" + player.getName() + "**."));
                            item.add(WordUtils.capitalizeFully(material.name().replace("_", " ")));
                        }
                    } else {
                        player.sendMessage(TextFormatter.pluginMessage("Give", "Target **" + target1 + "** not found."));
                    }
                }
                if (players == 0) {
                    player.sendMessage(TextFormatter.pluginMessage("Give", "No targets were found, so nothing was given out."));
                } else {
                    for (Material material : materials) {
                        player.sendMessage(TextFormatter.pluginMessage("Give", "You gave **" + amount + " " + WordUtils.capitalizeFully(material.name().replace("_", " ")) + ((amount > 1)?"s":"") + "** to **" + players + "** players."));
                    }
                    if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                        if (playersInGame > 0) {
                            EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                        }
                        EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", amount + " " + String.join(", ", item) + " given to " + players + " players.").put("player", player.getName())));
                    }
                }
            }
        } else {
            player.sendMessage(TextFormatter.pluginMessage("Give", "Invalid syntax. Correct syntax: **/give <player|all> <item> [amount] [name] [enchantments...]**"));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCServerPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
