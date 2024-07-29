package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SuperMiningMachineStartEvent implements Listener {

  private final StlmppPlugin plugin;

  public static SuperMiningMachineStartEvent register(StlmppPlugin plugin) {
    return new SuperMiningMachineStartEvent(plugin);
  }

  private SuperMiningMachineStartEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onBlockClick(PlayerInteractEvent event) {
    final var block = event.getClickedBlock();
    final var player = event.getPlayer();
    final var world = player.getWorld();
    final var hand = event.getHand();
    if (hand == null
        || !event.getHand().equals(EquipmentSlot.HAND)
        || block == null
        || !block.getType().equals(Material.NETHERITE_BLOCK)
        || (!world.getName().equals(this.plugin.getWorldName())
            && !world.getName().equals(this.plugin.getWorldNetherName()))
        || !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
        || !player.getInventory().getItemInMainHand().getType().equals(Material.NETHER_STAR)) {
      return;
    }
    assert this.plugin.superMiningMachineManager != null;
    final var machine = this.plugin.superMiningMachineManager.getMachineByNetheriteBlock(block);
    if (machine == null) {
      this.plugin.log(String.format("machine not found with block %s", block), true);
      return;
    }
    // TODO check if player has enough experience and consume it
    player.getInventory().getItemInMainHand().subtract(1);
    machine.start();
  }
}
