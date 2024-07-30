package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SuperMiningMachineDestroyEvent implements Listener {

  private final StlmppPlugin plugin;

  public static SuperMiningMachineDestroyEvent register(StlmppPlugin plugin) {
    return new SuperMiningMachineDestroyEvent(plugin);
  }

  private SuperMiningMachineDestroyEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {

    assert this.plugin.superMiningMachineManager != null;
    if (!this.plugin.superMiningMachineManager.isBlockTypeValid(event.getBlock().getType())
        || !this.plugin.superMiningMachineManager.isWorldValid(event.getBlock().getWorld())) {
      return;
    }
    final var machine = this.plugin.superMiningMachineManager.getMachineByBlock(event.getBlock());
    if (machine == null) {
      return;
    }
    if (machine.getIsRunning()) {
      this.plugin.log(
          String.format(
              "%s will explode with power of %s", machine.getId(), machine.getExplosionPower()));
      // TODO move explosion logic to machine class
      final var blocksToExplode = machine.getCorners().stream().toList();
      var blockNumber = 0;
      for (Block block : machine.getCorners()) {
        blockNumber++;
        final var explosionLocation = block.getLocation().subtract(0, 1, 0);
        final var isFinalBlock = blockNumber == blocksToExplode.size();
        Util.runLater(
            Tick.fromSeconds(blockNumber),
            () -> {
              explosionLocation
                  .getBlock()
                  .getWorld()
                  .createExplosion(explosionLocation, machine.getExplosionPower(), true, true);
              if (Chance.of(50)) {
                block.setType(Material.LAVA);
              } else {
                block.breakNaturally();
              }
              if (!isFinalBlock) {
                return;
              }
              var obsidianNumber = 0;
              for (Block obsidianBlock : machine.getBlocks()) {
                if (obsidianBlock.getType().equals(Material.NETHERITE_BLOCK)) {
                  continue;
                }
                Util.runLater(
                    Tick.fromSeconds(++obsidianNumber / 10), obsidianBlock::breakNaturally);
              }
            });
      }
      machine.stop();
    }
    // TODO send message saying that the machine is broken
    // TODO what will happen when someone breaks the machine? Maybe break some blocks of it

    this.plugin.superMiningMachineManager.removeMachine(machine);
  }
}
