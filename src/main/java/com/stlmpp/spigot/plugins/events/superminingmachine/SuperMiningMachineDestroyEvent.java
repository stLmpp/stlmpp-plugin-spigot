package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.Sound;
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
    if (!machine.getIsRunning()) {
      return;
    }
    // TODO figure out why the sound is not playing
    // TODO send message saying that the machine is broken
    event
        .getBlock()
        .getWorld()
        .playSound(event.getBlock().getLocation(), Sound.BLOCK_ANVIL_BREAK, 3.0f, 1f);
    machine.stop();
  }
}
