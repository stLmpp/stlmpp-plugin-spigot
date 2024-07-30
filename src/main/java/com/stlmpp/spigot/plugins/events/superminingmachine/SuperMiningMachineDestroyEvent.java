package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.List;
import org.bukkit.Material;
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
      final var blocksToExplode =
          List.of(
              machine.bottomRightBlock,
              machine.topRightBlock,
              machine.topLeftBlock,
              machine.bottomLeftBlock);
      for (var index = 0; index < blocksToExplode.size(); index++) {
        final var block = blocksToExplode.get(index);
        final var explosionLocation = block.getLocation().subtract(0, 1, 0);
        final var isFinalBlock = (index + 1) == blocksToExplode.size();
        Util.runLater(
            Tick.fromSeconds(index + 0.2),
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
              if (isFinalBlock) {
                for (var indexObsidian = 0;
                    indexObsidian < machine.blockVectors.size();
                    indexObsidian++) {
                  final var obsidianBlock =
                      machine.blockVectors.stream()
                          .toList()
                          .get(indexObsidian)
                          .toLocation(event.getBlock().getWorld())
                          .getBlock();
                  if (obsidianBlock.getType().equals(Material.NETHERITE_BLOCK)) {
                    continue;
                  }
                  Util.runLater(
                      Tick.fromSeconds((indexObsidian + 1) / 10), obsidianBlock::breakNaturally);
                }
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
