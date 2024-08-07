package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SMMStartEvent implements Listener {

  private final StlmppPlugin plugin;

  public static SMMStartEvent register(StlmppPlugin plugin) {
    return new SMMStartEvent(plugin);
  }

  private SMMStartEvent(StlmppPlugin plugin) {
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
    assert this.plugin.smmManager != null;
    final var machine = this.plugin.smmManager.getMachineByNetheriteBlock(block);
    if (machine == null) {
      this.plugin.log(String.format("machine not found with block %s", block), true);
      return;
    }
    final var isCreative = player.getGameMode().equals(GameMode.CREATIVE);
    if (machine.getHasFinished()) {
      this.plugin.sendMessage(
          String.format("%s, essa maquina ja terminou a escavacao!", player.getName()));
      return;
    }
    if (!machine.getIsRunning()) {
      this.startMachine(player, machine, isCreative);
      return;
    }
    if (machine.hasMaxBoost()) {
      this.plugin.sendMessage(
          String.format("%s, essa maquina ja esta com o boost maximo!", player.getName()));
      return;
    }
    machine.addBoost();
    if (!isCreative) {
      player.getInventory().getItemInMainHand().subtract(1);
    }
    this.plugin.sendMessage(
        String.format(
            "%s adicionou um boost na escavadeira! Boost total de %s%%",
            player.getName(), (int) Math.floor(machine.getBoostFactor() * 100)));
  }

  private void startMachine(Player player, SuperMiningMachine machine, boolean isCreative) {
    if (player.getLevel() < machine.getExpLevelRequired() && !isCreative) {
      this.plugin.sendMessage(
          String.format(
              "%s, essa escavadeira custa %s levels, voce tem %s levels",
              player.getName(), machine.getExpLevelRequired(), player.getLevel()));
      return;
    }
    if (!isCreative) {
      player.setLevel(player.getLevel() - machine.getExpLevelRequired());
      player.getInventory().getItemInMainHand().subtract(1);
    }
    machine.start();
    this.plugin.sendMessage(
        String.format(
            "%s iniciou a escavadeira sacrificando %s levels de xp",
            player.getName(), machine.getExpLevelRequired()));
  }
}
