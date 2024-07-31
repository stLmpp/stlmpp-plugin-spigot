package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class SMMBlockBreaker {

  public SMMBlockBreaker() {
    this.map = new HashMap<>();
    this.map.put(Material.CHEST, this::onChest);
    this.map.put(Material.SPAWNER, this::onSpawner);
    for (Material material : Util.oreList) {
      this.map.put(material, this::onOre);
    }
  }

  private final HashMap<Material, Function<Block, Collection<ItemStack>>> map;

  public Function<Block, Collection<ItemStack>> get(Material material) {
    return Optional.ofNullable(map.get(material)).orElse(this::onDefault);
  }

  private Collection<ItemStack> onOre(Block block) {
    final var pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
    pickaxe.addEnchantment(Enchantment.FORTUNE, 3);
    final var allOres = Util.getBlocksAround(block);
    final var items = new ArrayList<ItemStack>();
    for (Block oreBlock : allOres) {
      items.addAll(oreBlock.getDrops(pickaxe));
      oreBlock.setType(Material.AIR);
    }
    return items;
  }

  private Collection<ItemStack> onDefault(Block block) {
    final var tool = Util.findBestTool(block);
    var drops = block.getDrops(tool);
    if (drops.isEmpty()) {
      tool.removeEnchantments();
      tool.addEnchantment(Enchantment.SILK_TOUCH, 1);
      drops = block.getDrops(tool);
    }
    block.setType(Material.AIR);
    return drops;
  }

  private Collection<ItemStack> onChest(Block block) {
    final var chest = (org.bukkit.block.Chest) block.getState();
    final var isDoubleChest = chest.getInventory().getHolder() instanceof DoubleChest;
    final var inventory =
        isDoubleChest ? chest.getInventory().getHolder().getInventory() : chest.getInventory();
    final var chestItems = Stream.of(inventory.getContents()).filter(Objects::nonNull).toList();
    final List<ItemStack> items = new ArrayList<>(chestItems);
    final var chestStack = new ItemStack(Material.CHEST);
    if (isDoubleChest) {
      chestStack.add();
      final var left = ((DoubleChest) chest.getInventory().getHolder()).getLeftSide();
      final var right = ((DoubleChest) chest.getInventory().getHolder()).getRightSide();
      if (left != null && left.getInventory().getLocation() != null) {
        left.getInventory().getLocation().getBlock().setType(Material.AIR);
      }
      if (right != null && right.getInventory().getLocation() != null) {
        right.getInventory().getLocation().getBlock().setType(Material.AIR);
      }
    } else {
      chest.setType(Material.AIR);
    }
    items.add(chestStack);
    return items;
  }

  private Collection<ItemStack> onSpawner(Block block) {
    // TODO
    return new ArrayList<>();
  }
}
