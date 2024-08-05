package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SMMDestroyEvent implements Listener {

  private final StlmppPlugin plugin;

  public static SMMDestroyEvent register(StlmppPlugin plugin) {
    return new SMMDestroyEvent(plugin);
  }

  private SMMDestroyEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    assert this.plugin.smmManager != null;
    final var isValidBlock =
        this.plugin.smmManager.isBlockTypeValid(event.getBlock().getType())
            || event.getBlock().getType().equals(Material.CHEST);
    if (!isValidBlock || !this.plugin.smmManager.isWorldValid(event.getBlock().getWorld())) {
      return;
    }
    final var machine = this.plugin.smmManager.getMachineByBlockOrChest(event.getBlock());
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
    this.plugin.smmManager.removeMachine(machine);
  }
}
