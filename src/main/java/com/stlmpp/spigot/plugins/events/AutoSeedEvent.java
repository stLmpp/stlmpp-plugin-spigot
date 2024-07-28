package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Util;

import java.util.*;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AutoSeedEvent implements Listener {

  public static @Nullable AutoSeedEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.autoSeedEnabled)) {
      return null;
    }
    return new AutoSeedEvent(plugin);
  }

  private AutoSeedEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.configAutoSeedMaxBlocks = this.plugin.config.getInt(StlmppPluginConfig.autoSeedMaxBlocks);
    this.plugin.log(
        String.format("Auto seed activated with max of %s blocks!", this.configAutoSeedMaxBlocks));
    this.fromSeed.put(Material.WHEAT_SEEDS, Material.WHEAT);
    this.fromSeed.put(Material.MELON_SEEDS, Material.MELON_STEM);
    this.fromSeed.put(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM);
    this.fromSeed.put(Material.BEETROOT_SEEDS, Material.BEETROOTS);
    this.fromSeed.put(Material.POTATO, Material.POTATOES);
    this.fromSeed.put(Material.CARROT, Material.CARROTS);
    final var allowedSeedNames =
        this.plugin.config.getList(StlmppPluginConfig.autoSeedAllowedSeedList);
    if (allowedSeedNames != null) {
      Util.setMaterialsFromNames(this.allowedSeeds, allowedSeedNames);
    }
  }

  private final StlmppPlugin plugin;
  private final BlockFace[] blockFaces = {
    BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
  };
  private final Set<Material> allowedSeeds = new HashSet<>();
  private final Map<Material, Material> fromSeed = new HashMap<>();
  private final int configAutoSeedMaxBlocks;

  private List<Block> getRelatives(Block block) {
    return Arrays.stream(this.blockFaces).map(block::getRelative).toList();
  }

  private Set<Block> getBlocks(Block startBlock, int maxBlocks) {
    int currentIteration = 0;
    final var blocks = new HashSet<Block>();
    final var visited = new HashSet<Block>();
    blocks.add(startBlock);
    final var queue = new LinkedList<>(this.getRelatives(startBlock));
    while (!queue.isEmpty() && blocks.size() < maxBlocks) {
      currentIteration++;
      if (currentIteration > 1000) {
        this.plugin.log("Auto seed tried more than 1000 iterations!");
        break;
      }
      final var block = queue.remove();
      if (visited.contains(block)) {
        continue;
      }
      visited.add(block);
      if (block.getType() != Material.FARMLAND) {
        continue;
      }
      if (block.getRelative(BlockFace.UP).getType().isAir()) {
        blocks.add(block);
      }
      queue.addAll(this.getRelatives(block));
    }
    return blocks;
  }

  private void removeOneFromStackList(List<ItemStack> items) {
    if (items.isEmpty()) {
      return;
    }
    final var first = items.getFirst();
    first.setAmount(first.getAmount() - 1);
    if (first.getAmount() <= 0) {
      items.removeFirst();
    }
  }

  @EventHandler
  public void onBlockClick(PlayerInteractEvent event) {
    this.plugin.log(String.format("action = %s", event.getAction()), true);
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    final var itemInHand = event.getItem();
    this.plugin.log(String.format("itemInHand = %s", itemInHand), true);
    if (itemInHand == null) {
      return;
    }
    final var itemInHandType = itemInHand.getType();
    this.plugin.log(String.format("itemInHandType = %s", itemInHandType), true);
    if (!this.allowedSeeds.contains(itemInHandType)) {
      return;
    }
    final var plant = this.fromSeed.get(itemInHandType);
    this.plugin.log(String.format("plant = %s", plant), true);
    if (plant == null) {
      return;
    }
    final var block = event.getClickedBlock();
    this.plugin.log(String.format("block = %s", block), true);
    if (block == null) {
      return;
    }
    this.plugin.log(String.format("getBlockFace = %s", event.getBlockFace()), true);
    if (event.getBlockFace() != BlockFace.UP) {
      return;
    }
    final var blockType = block.getType();
    this.plugin.log(String.format("blockType = %s", blockType), true);
    if (blockType != Material.FARMLAND) {
      return;
    }
    final var player = event.getPlayer();
    this.plugin.log(String.format("player.isSneaking = %s", player.isSneaking()), true);
    if (!player.isSneaking()) {
      return;
    }
    final var playerInventory = player.getInventory();
    this.plugin.log(
        String.format(
            "playerInventory.contains(itemInHandType) = %s",
            playerInventory.contains(itemInHandType)),
        true);
    if (!playerInventory.contains(itemInHandType)) {
      return;
    }
    final var items = new ArrayList<ItemStack>();
    for (ItemStack itemStack : playerInventory) {
      if (itemStack != null && itemStack.getType().equals(itemInHandType)) {
        items.add(itemStack);
      }
    }
    this.plugin.log(String.format("items.isEmpty() = %s", items.isEmpty()), true);
    if (items.isEmpty()) {
      return;
    }
    final var isCreativeGameMode = player.getGameMode().equals(GameMode.CREATIVE);
    final var itemsAmount =
        isCreativeGameMode
            ? configAutoSeedMaxBlocks
            : items.stream().mapToInt(ItemStack::getAmount).sum();
    this.plugin.log(String.format("itemsAmount = %s", itemsAmount), true);
    final var maxBlocks = Math.min(configAutoSeedMaxBlocks, itemsAmount);
    final var blocks = this.getBlocks(block, maxBlocks);
    this.plugin.log(String.format("blocks.size() = %s", blocks.size()), true);
    for (Block _block : blocks) {
      if (!isCreativeGameMode) {
        this.removeOneFromStackList(items);
      }
      _block.getRelative(BlockFace.UP).setType(plant);
    }
  }
}
