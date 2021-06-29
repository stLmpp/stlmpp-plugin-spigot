package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Config;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AutoSeedEvent implements Listener {

  public AutoSeedEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  private final StlmppPlugin plugin;
  private final BlockFace[] blockFaces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

  private List<Block> getRelatives(Block block) {
    return Arrays.stream(this.blockFaces).map(block::getRelative).toList();
  }

  private Set<Block> getBlocks(Block startBlock, int maxBlocks) {
    int currentIteration = 0;
    var blocks = new HashSet<Block>();
    var visited = new HashSet<Block>();
    blocks.add(startBlock);
    var queue = new LinkedList<>(this.getRelatives(startBlock));
    while (!queue.isEmpty() && blocks.size() < maxBlocks) {
      currentIteration++;
      if (currentIteration > 1000) {
        if (this.plugin.isDevMode) {
          StlmppPlugin.sendConsoleMessage("Auto seed tried more than 1000 iterations!");
        }
        break;
      }
      var block = queue.remove();
      if (!visited.contains(block)) {
        visited.add(block);
        if (block.getType() == Material.FARMLAND) {
          blocks.add(block);
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
    var first = items.get(0);
    first.setAmount(first.getAmount() - 1);
    if (first.getAmount() <= 0) {
      items.remove(0);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onBlockClick(PlayerInteractEvent event) {
    var block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    if (event.getBlockFace() != BlockFace.UP) {
      return;
    }
    var blockType = block.getType();
    if (blockType != Material.FARMLAND) {
      return;
    }
    var player = event.getPlayer();
    if (!player.isSneaking() || !event.hasItem()) {
      return;
    }
    var playerInventory = player.getInventory();
    if (!playerInventory.contains(Material.WHEAT_SEEDS)) {
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
    var maxBlocks = Math.min(this.plugin.config.getInt(Config.autoSeedMaxBlocks), itemsAmount);
    var blocks = this.getBlocks(block, maxBlocks);
    for (Block _block : blocks) {
      this.removeOneFromStackList(items);
      _block.getRelative(BlockFace.UP).setType(Material.WHEAT);
    }
  }
}
