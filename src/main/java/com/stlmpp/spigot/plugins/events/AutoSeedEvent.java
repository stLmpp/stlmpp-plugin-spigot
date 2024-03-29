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

public class AutoSeedEvent implements Listener {

  public AutoSeedEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.fromSeed.put(Material.WHEAT_SEEDS, Material.WHEAT);
    this.fromSeed.put(Material.MELON_SEEDS, Material.MELON_STEM);
    this.fromSeed.put(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM);
    this.fromSeed.put(Material.BEETROOT_SEEDS, Material.BEETROOTS);
    this.fromSeed.put(Material.POTATO, Material.POTATOES);
    this.fromSeed.put(Material.CARROT, Material.CARROTS);
    final var allowedSeedNames = this.plugin.config.getList(StlmppPluginConfig.autoSeedAllowedSeedList);
    if (allowedSeedNames != null) {
      Util.setMaterialsFromNames(this.allowedSeeds, allowedSeedNames);
    }
  }

  private final StlmppPlugin plugin;
  private final BlockFace[] blockFaces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
  private final Set<Material> allowedSeeds = new HashSet<>();
  private final Map<Material, Material> fromSeed = new HashMap<>();

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
        this.plugin.sendConsoleMessage("Auto seed tried more than 1000 iterations!");
        break;
      }
      final var block = queue.remove();
      if (!visited.contains(block)) {
        visited.add(block);
        if (block.getType() == Material.FARMLAND) {
          if (block.getRelative(BlockFace.UP).getType().isAir()) {
            blocks.add(block);
          }
          queue.addAll(this.getRelatives(block));
        }
      }
    }
    return blocks;
  }

  private void removeOneFromStackList(List<ItemStack> items) {
    if (items.size() == 0) {
      return;
    }
    final var first = items.get(0);
    first.setAmount(first.getAmount() - 1);
    if (first.getAmount() <= 0) {
      items.remove(0);
    }
  }

  @EventHandler
  public void onBlockClick(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    final var itemInHand = event.getItem();
    if (itemInHand == null) {
      return;
    }
    final var itemInHandType = itemInHand.getType();
    if (!this.allowedSeeds.contains(itemInHandType)) {
      return;
    }
    final var plant = this.fromSeed.get(itemInHandType);
    if (plant == null) {
      return;
    }
    final var block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    if (event.getBlockFace() != BlockFace.UP) {
      return;
    }
    final var blockType = block.getType();
    if (blockType != Material.FARMLAND) {
      return;
    }
    final var player = event.getPlayer();
    if (!player.isSneaking()) {
      return;
    }
    final var playerInventory = player.getInventory();
    if (!playerInventory.contains(itemInHandType)) {
      return;
    }
    final var items = new ArrayList<ItemStack>();
    for (ItemStack itemStack : playerInventory) {
      if (itemStack != null && itemStack.getType() == itemInHandType) {
        items.add(itemStack);
      }
    }
    if (items.size() == 0) {
      return;
    }
    final var configAutoSeedMaxBlocks = this.plugin.config.getInt(StlmppPluginConfig.autoSeedMaxBlocks);
    final var isCreativeGameMode = player.getGameMode() == GameMode.CREATIVE;
    final var itemsAmount = isCreativeGameMode
      ? configAutoSeedMaxBlocks
      : items.stream().reduce(0, (acc, item) -> acc + item.getAmount(), Integer::sum);
    final var maxBlocks = Math.min(configAutoSeedMaxBlocks, itemsAmount);
    final var blocks = this.getBlocks(block, maxBlocks);
    for (Block _block : blocks) {
      if (!isCreativeGameMode) {
        this.removeOneFromStackList(items);
      }
      _block.getRelative(BlockFace.UP).setType(plant);
    }
  }
}
