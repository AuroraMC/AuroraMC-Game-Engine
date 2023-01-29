/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.commands.admin;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.command.Command;
import net.auroramc.core.api.permissions.Permission;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.games.GameSession;
import net.auroramc.engine.api.server.ServerState;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;

public class CommandMob extends Command {


    public CommandMob() {
        super("mob", Arrays.asList("mobs", "summon", "spawn"), Collections.singletonList(Permission.ADMIN), false, null);
    }

    @Override
    public void execute(AuroraMCPlayer player, String aliasUsed, List<String> args) {
        if (args.size() >= 1) {
            if (args.get(0).equalsIgnoreCase("kill")) {
                if (args.size() == 2) {
                    if (args.get(1).equalsIgnoreCase("all")) {
                        for (Entity entity : player.getPlayer().getLocation().getWorld().getEntities()) {
                            if (entity instanceof Player) {
                                continue;
                            }
                            entity.remove();
                        }
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "You killed all entities."));
                        return;
                    }
                    List<String> matches = new ArrayList<>();
                    String mobString = args.remove(1);
                    for (EntityType type : EntityType.values()) {
                        if (!type.isAlive()) {
                            continue;
                        }
                        if (type.name().startsWith(mobString.toUpperCase())) {
                            matches.add(type.name());
                        }
                    }

                    if (matches.size() == 0) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "No matches were found for mob **" + mobString + "**."));
                        return;
                    }

                    if (matches.size() > 1) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "Multiple possible matches found for mob **" + mobString + "**. Please be more specific. Matches: [**" + String.join("**, **", matches) + "**]"));
                        return;
                    }

                    EntityType type = EntityType.valueOf(matches.get(0));
                    if (type == EntityType.PLAYER) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "You cannot kill players with /mob."));
                        return;
                    }
                    Collection<Entity> entities = player.getPlayer().getWorld().getEntitiesByClasses(type.getEntityClass());
                    for (Entity entity : entities) {
                        if (entity.getPassenger() != null) {
                            if (entity.getPassenger().getPassenger() != null) {
                                entity.getPassenger().getPassenger().remove();
                            }
                            entity.getPassenger().remove();
                        }
                        entity.remove();
                    }
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "You have killed **" + entities.size() + " " + WordUtils.capitalizeFully(type.name().replace("_", " ")) + "s**."));
                } else {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "Invalid syntax. Correct syntax: **/mob kill [mob]**"));
                }
            } else if (args.get(0).equalsIgnoreCase("list")) {
                Map<String, Integer> mobs = new HashMap<>();
                for (Entity entity : player.getPlayer().getLocation().getWorld().getEntities()) {
                    String name = entity.getClass().getSimpleName().replace("Craft", "");
                    if (mobs.containsKey(name)) {
                        mobs.put(name, mobs.get(name) + 1);
                    } else {
                        mobs.put(name, 1);
                    }
                }
                StringBuilder build = new StringBuilder();
                for (Map.Entry<String, Integer> entry : mobs.entrySet()) {
                    build.append("\n");
                    build.append(entry.getKey());
                    build.append(": **");
                    build.append(entry.getValue());
                    build.append("**");
                }
                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "All mobs in world **" + player.getPlayer().getLocation().getWorld().getName() + "**:" +
                        build));
            } else {
                List<String> matches = new ArrayList<>();
                String mobString = args.remove(0);
                for (EntityType type : EntityType.values()) {
                    if (!type.isAlive()) {
                        continue;
                    }
                    if (type.name().startsWith(mobString.toUpperCase())) {
                        matches.add(type.name());
                    }
                }

                if (matches.size() == 0) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "No matches were found for mob **" + mobString + "**."));
                    return;
                }

                if (matches.size() > 1) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "Multiple possible matches found for mob **" + mobString + "**. Please be more specific. Matches: [**" + String.join("**, **", matches) + "**]"));
                    return;
                }

                EntityType type = EntityType.valueOf(matches.get(0));
                Block block = player.getPlayer().getTargetBlock((Set<Material>) null, 50);
                if (block == null) {
                    player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "You are not looking in a valid location. Please try again."));
                    return;
                }
                Location location = block.getLocation();
                location.setY(location.getY() + 1);

                byte amount = 1;

                if (args.size() >= 1) {
                    try {
                        amount = Byte.parseByte(args.remove(0));
                    } catch (NumberFormatException e) {
                        player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "You specified an invalid amount. Please try again."));
                        return;
                    }
                }
                String name = null;
                Material item = null;
                String armorType = null;
                int totalHealth = -1;
                boolean angry = false;
                boolean baby = false;
                boolean noAI = false;

                for (String arg : args) {
                    if (arg.equalsIgnoreCase("baby")) {
                        baby = true;
                    } else if (arg.equalsIgnoreCase("angry")) {
                        angry = true;
                    } else if (arg.equalsIgnoreCase("noai")) {
                        noAI = true;
                    } else if (arg.matches("^h[0-9]+$")) {
                        totalHealth = Integer.parseInt(arg.substring(1));
                    } else if (arg.matches("^n[0-9a-zA-Z_&]+$")) {
                        name = arg.substring(1);
                    } else if (arg.toLowerCase().matches("^(airon|adiamond|achainmail|aleather)$")) {
                        armorType = arg.substring(1).toLowerCase();
                    } else if (arg.matches("^i[0-9a-zA-Z_&!]+$")) {
                        String itemString = arg.substring(1);
                        List<String> itemMatches = new ArrayList<>();
                        for (Material material : Material.values()) {
                            if (material.name().startsWith(itemString.toUpperCase())) {
                                itemMatches.add(material.name());
                            }
                        }
                        if (itemMatches.size() > 0) {
                            item = Material.valueOf(itemMatches.get(0));
                        }
                    }
                }

                player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "You have spawned **" + amount + " " + WordUtils.capitalizeFully(type.name().replace("_", " ")) + "s**. Options:\n" +
                        "Baby: **" + baby + "**\n" +
                        "Angry: **" + angry + "**\n" +
                        "NoAI: **" + noAI + "**\n" +
                        "Armor: **" + ((armorType != null)?armorType:"None") + "**\n" +
                        "Held Item: **" + ((item != null)?item.name():"None") + "**\n" +
                        "Custom Name: **" + ((name != null)?name:"None") + "**\n" +
                        "Total Health: **" + ((totalHealth > -1)?totalHealth:"Default") + "hp**"));
                if (EngineAPI.getServerState() == ServerState.IN_GAME) {
                    EngineAPI.getActiveGame().voidGame("an admin used a command that effects gameplay");
                    EngineAPI.getActiveGame().getGameSession().log(new GameSession.GameLogEntry(GameSession.GameEvent.GAME_EVENT, new JSONObject().put("description", amount + " " + WordUtils.capitalizeFully(type.name().replace("_", " ")) + " spawned in world.").put("player", player.getPlayer().getName())));
                }

                while (amount > 0) {
                    LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);

                    entity.setCanPickupItems(false);
                    entity.setCustomNameVisible(true);

                    if (name != null) {
                        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
                        stand.setVisible(false);
                        stand.setCustomName(AuroraMCAPI.getFormatter().convert(AuroraMCAPI.getFormatter().highlight(name.replace("_", " "))));
                        stand.setCustomNameVisible(true);
                        stand.setSmall(true);
                        stand.setMarker(true);
                        Rabbit rabbit = location.getWorld().spawn(location, Rabbit.class);
                        rabbit.setPassenger(stand);
                        rabbit.setBaby();
                        CraftEntity craftEntity = ((CraftEntity)rabbit);
                        NBTTagCompound tag = craftEntity.getHandle().getNBTTag();
                        if (tag == null) {
                            tag = new NBTTagCompound();
                        }
                        craftEntity.getHandle().c(tag);
                        tag.setInt("NoAI", 1);
                        craftEntity.getHandle().f(tag);
                        rabbit.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 1, true, false));
                        entity.setPassenger(rabbit);
                        //entity.setCustomName(AuroraMCAPI.getFormatter().convert(AuroraMCAPI.getFormatter().highlight(name.replace("_", " "))));
                    }
                    if (entity instanceof EntityInsentient) {
                        if (item != null) {
                            entity.getEquipment().setItemInHandDropChance(0);
                            entity.getEquipment().setItemInHand(new ItemStack(item, 1));
                        }
                        if (armorType != null) {
                            switch (armorType) {
                                case "iron": {
                                    entity.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS, 1));
                                    entity.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE, 1));
                                    entity.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS, 1));
                                    entity.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET, 1));
                                    break;
                                }
                                case "diamond": {
                                    entity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
                                    entity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
                                    entity.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
                                    entity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1));
                                    break;
                                }
                                case "chainmail": {
                                    entity.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS, 1));
                                    entity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1));
                                    entity.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1));
                                    entity.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET, 1));
                                    break;
                                }
                                case "leather": {
                                    entity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS, 1));
                                    entity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE, 1));
                                    entity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS, 1));
                                    entity.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET, 1));
                                    break;
                                }
                            }
                            entity.getEquipment().setBootsDropChance(0);
                            entity.getEquipment().setHelmetDropChance(0);
                            entity.getEquipment().setChestplateDropChance(0);
                            entity.getEquipment().setLeggingsDropChance(0);
                        }
                    }
                    if (totalHealth != -1) {
                        entity.setMaxHealth(totalHealth);
                        entity.setHealth(totalHealth);
                    }
                    if (angry && entity instanceof Wolf) {
                        ((Wolf)entity).setAngry(true);
                    }
                    if (baby && entity instanceof Ageable) {
                        ((Ageable)entity).setBaby();
                        ((Ageable)entity).setAgeLock(true);
                    } else if (!baby && entity instanceof Ageable) {
                        ((Ageable)entity).setAdult();
                        ((Ageable)entity).setAgeLock(true);
                    }

                    if (noAI) {
                        assert entity instanceof CraftEntity;
                        CraftEntity craftEntity = ((CraftEntity)entity);
                        NBTTagCompound tag = craftEntity.getHandle().getNBTTag();
                        if (tag == null) {
                            tag = new NBTTagCompound();
                        }
                        craftEntity.getHandle().c(tag);
                        tag.setInt("NoAI", 1);
                        craftEntity.getHandle().f(tag);
                    }
                    amount--;
                }

            }
        } else {
            player.getPlayer().sendMessage(AuroraMCAPI.getFormatter().pluginMessage("Mob", "Invalid syntax. Correct syntax: **/mob [mob] [amount] n[name] i[held item] a[armor type] h[total health] [baby] [angry] [noai]** / **/mob kill [mob]** / **/mob list**"));
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(AuroraMCPlayer auroraMCPlayer, String s, List<String> list, String s1, int i) {
        return new ArrayList<>();
    }
}
