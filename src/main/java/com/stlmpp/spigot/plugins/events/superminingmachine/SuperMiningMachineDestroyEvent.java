package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
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
      this.plugin.sendMessage(
          String.format(
              "%s destruiu uma escavadeira que estava funcionando... Ela vai explodir em 5 segundos",
              event.getPlayer().getName()));
      machine.explode(4);
      machine.stop();
    }
    this.plugin.superMiningMachineManager.removeMachine(machine);
  }
}
