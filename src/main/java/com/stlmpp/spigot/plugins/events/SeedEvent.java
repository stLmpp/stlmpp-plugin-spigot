package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Config;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SeedEvent implements Listener {

  public SeedEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  private final StlmppPlugin plugin;
  private final int maxRecursionLevel = 50;
  private final BlockFace[] blockFaces = {
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST,
    BlockFace.NORTH_EAST,
    BlockFace.NORTH_WEST,
    BlockFace.SOUTH_EAST,
    BlockFace.SOUTH_WEST,
  };

  private List<Block> getRelatives(Block block) {
    return Arrays.stream(this.blockFaces).map(block::getRelative).toList();
  }

  // TODO create class to handle the recursive function, se we can use properties to control the recursion level

  private void getBlocks(
    Set<Block> blocks,
    Block currentBlock,
    Set<Block> visited,
    int maxBlocks,
    int currentRecursionLevel
  ) {
    var relatives = this.getRelatives(currentBlock);
    var hasAnyRelativeFarmland = relatives.stream().anyMatch(block -> block.getType() == Material.FARMLAND);
    if (!hasAnyRelativeFarmland) {
      visited.addAll(relatives);
      return;
    }
    if (blocks.size() >= maxBlocks) {
      return;
    }
    for (Block block : relatives) {
      if (visited.contains(block)) {
        continue;
      }
      if (block.getType() == Material.FARMLAND) {
        blocks.add(block);
      }
      visited.add(block);
      this.getBlocks(blocks, block, visited, maxBlocks, currentRecursionLevel + 1);
    }
  }

  @EventHandler
  public void onBlockClick(PlayerInteractEvent event) {
    var player = event.getPlayer();
    var block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    Bukkit.getConsoleSender().sendMessage("Material " + block.getType().name());
    if (event.getItem() != null) {
      Bukkit.getConsoleSender().sendMessage("InHand " + event.getItem().getType().name());
    }
    var blockType = block.getType();
    var playerInventory = player.getInventory();
    if (
      !player.isSneaking() ||
      !event.hasItem() ||
      blockType != Material.FARMLAND ||
      !playerInventory.contains(Material.WHEAT_SEEDS)
    ) {
      return;
    }
    var items = new ArrayList<ItemStack>();
    for (ItemStack itemStack : playerInventory) {
      if (itemStack != null && itemStack.getType() == Material.WHEAT_SEEDS) {
        items.add(itemStack);
      }
    }
    if (items.size() == 0) {
      return;
    }
    var itemsAmount = items.stream().reduce(0, (acc, item) -> acc + item.getAmount(), Integer::sum);
    var maxBlocks = Math.max(this.plugin.config.getInt(Config.seedMaxBlocks), itemsAmount);
    var blocks = new HashSet<Block>();
    this.getBlocks(blocks, block, new HashSet<Block>(), maxBlocks, 0);
    var world = player.getWorld();
    blocks.forEach(
      _block -> {
        Bukkit.getConsoleSender().sendMessage("X=" + _block.getX() + ", Y=" + _block.getY() + ", Z=" + _block.getZ());
        var blockAbove = world.getBlockAt(_block.getX(), _block.getY() + 3, _block.getZ());
        blockAbove.setType(Material.DIAMOND_BLOCK);
      }
    );
  }
}
