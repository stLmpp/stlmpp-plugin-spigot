package com.stlmpp.spigot.plugins.events.superminingmachine;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;

public class SMMBlockDelay {

  private static final Map<Material, Double> map;

  static {
    map = new HashMap<>();

    // Ores
    map.put(Material.IRON_ORE, 4.0);
    map.put(Material.COAL_ORE, 8.0);
    map.put(Material.DIAMOND_ORE, 5.0);
    map.put(Material.COPPER_ORE, 9.0);
    map.put(Material.EMERALD_ORE, 7.0);
    map.put(Material.GOLD_ORE, 6.0);
    map.put(Material.LAPIS_ORE, 3.0);
    map.put(Material.REDSTONE_ORE, 2.0);
    map.put(Material.DEEPSLATE_IRON_ORE, 4.0 + 1.1);
    map.put(Material.DEEPSLATE_COAL_ORE, 8.0 + 1.5);
    map.put(Material.DEEPSLATE_DIAMOND_ORE, 5.0 + 2.0);
    map.put(Material.DEEPSLATE_COPPER_ORE, 9.0 + 3.75);
    map.put(Material.DEEPSLATE_EMERALD_ORE, 7.0 + 3.0);
    map.put(Material.DEEPSLATE_GOLD_ORE, 6.0 + 2.2);
    map.put(Material.DEEPSLATE_LAPIS_ORE, 3.0 + 2.5);
    map.put(Material.DEEPSLATE_REDSTONE_ORE, 2.0 + 2.2);
    map.put(Material.NETHER_GOLD_ORE, 3.2);
    map.put(Material.NETHER_QUARTZ_ORE, 2.2);
    map.put(Material.ANCIENT_DEBRIS, 20.0);

    // Blocks
    map.put(Material.DEEPSLATE, 1.1);
    map.put(Material.NETHERRACK, 0.65);
    map.put(Material.ANDESITE, 1.02);
    map.put(Material.DIRT, 0.95);
    map.put(Material.SAND, 0.95);
    map.put(Material.SANDSTONE, 1.05);
    map.put(Material.NETHERITE_BLOCK, 30.0);
    map.put(Material.OBSIDIAN, 5.0);
  }

  public static double get(Material material) {
    final var value = map.get(material);
    return value == null ? 1.0 : value;
  }
}
