package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class SuperMiningMachineManager {

  @Nullable
  public static SuperMiningMachineManager register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.superMiningMachineEnabled)) {
      return null;
    }
    return new SuperMiningMachineManager(plugin);
  }

  private SuperMiningMachineManager(StlmppPlugin plugin) {
    this.plugin = plugin;
    SuperMiningMachineCreationEvent.register(plugin);
    SuperMiningMachineStartEvent.register(plugin);
    SuperMiningMachineDestroyEvent.register(plugin);
  }

  private final StlmppPlugin plugin;

  private final HashMap<String, SuperMiningMachine> machines = new HashMap<>();

  public boolean isOverlappingAnotherMachine(BoundingBox boundingBox) {
    return machines.values().stream()
        .anyMatch(machine -> machine.boundingBox.overlaps(boundingBox));
  }

  @Nullable
  public SuperMiningMachine getMachineByNetheriteBlock(Block block) {
    return Util.findFirst(this.machines.values(), machine -> machine.hasNetheriteBlock(block));
  }

  @Nullable
  public SuperMiningMachine getMachineByBlock(Block block) {
    return Util.findFirst(this.machines.values(), machine -> machine.hasBlock(block));
  }

  public boolean isWorldValid(@NotNull World world) {
    return world.getName().equals(this.plugin.getWorldName())
        || world.getName().equals(this.plugin.getWorldNetherName());
  }

  public boolean isBlockTypeValid(@NotNull Material material) {
    return material.equals(Material.NETHERITE_BLOCK) || material.equals(Material.OBSIDIAN);
  }

  public void addMachine(SuperMiningMachine machine) {
    this.machines.put(machine.getId(), machine);
  }

  public void onEnable() {
    // TODO fetch machines and start them
  }

  public void onDisable() {
    for (SuperMiningMachine machine : machines.values()) {
      machine.stop();
    }
  }
}
