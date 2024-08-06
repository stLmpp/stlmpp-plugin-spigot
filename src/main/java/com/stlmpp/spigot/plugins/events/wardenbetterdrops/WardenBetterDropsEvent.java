package com.stlmpp.spigot.plugins.events.wardenbetterdrops;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.WeightedRandomCollection;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Nullable;

public class WardenBetterDropsEvent implements Listener {

  @Nullable
  public static WardenBetterDropsEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.wardenBetterDropsEnabled)) {
      return null;
    }
    return new WardenBetterDropsEvent(plugin);
  }

  private WardenBetterDropsEvent(StlmppPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    plugin.log("Warden Better Drops Event Activated");
  }

  private final WeightedRandomCollection<Drop> possibleItems =
      new WeightedRandomCollection<Drop>()
          .add(1000, new Drop(Material.DIAMOND, 12))
          .add(1000, new Drop(Material.GOLD_INGOT, 16))
          .add(100, new Drop(Material.ANCIENT_DEBRIS, 4))
          .add(100, new Drop(Material.SNIFFER_EGG, 1))
          .add(100, new Drop(Material.DIAMOND_BLOCK, 2))
          .add(1000, new Drop(Material.GOLD_BLOCK, 3))
          .add(75, new Drop(Material.NETHERITE_INGOT, 9))
          .add(50, new Drop(Material.NETHERITE_BLOCK, 1))
          .add(750, new Drop(Material.DIAMOND_SWORD, 1))
          .add(750, new Drop(Material.DIAMOND_SHOVEL, 1))
          .add(750, new Drop(Material.DIAMOND_PICKAXE, 1))
          .add(750, new Drop(Material.DIAMOND_AXE, 1))
          .add(750, new Drop(Material.DIAMOND_HOE, 1))
          .add(40, new Drop(Material.NETHERITE_SWORD, 1))
          .add(40, new Drop(Material.NETHERITE_SHOVEL, 1))
          .add(40, new Drop(Material.NETHERITE_PICKAXE, 1))
          .add(40, new Drop(Material.NETHERITE_AXE, 1))
          .add(40, new Drop(Material.NETHERITE_HOE, 1))
          .add(500, new Drop(Material.NETHER_STAR, 1))
          .add(800, new Drop(Material.DIAMOND_HELMET, 1))
          .add(800, new Drop(Material.DIAMOND_CHESTPLATE, 1))
          .add(800, new Drop(Material.DIAMOND_LEGGINGS, 1))
          .add(800, new Drop(Material.DIAMOND_BOOTS, 1))
          .add(100, new Drop(Material.GOLDEN_HELMET, 1))
          .add(100, new Drop(Material.GOLDEN_CHESTPLATE, 1))
          .add(100, new Drop(Material.GOLDEN_LEGGINGS, 1))
          .add(100, new Drop(Material.GOLDEN_BOOTS, 1))
          .add(35, new Drop(Material.NETHERITE_HELMET, 1))
          .add(35, new Drop(Material.NETHERITE_CHESTPLATE, 1))
          .add(35, new Drop(Material.NETHERITE_LEGGINGS, 1))
          .add(35, new Drop(Material.NETHERITE_BOOTS, 1))
          .add(400, new Drop(Material.GOLDEN_APPLE, 32))
          .add(350, new Drop(Material.ENCHANTED_GOLDEN_APPLE, 32))
          .add(200, new Drop(Material.HEAVY_CORE, 2))
          .add(1, new Drop(Material.WITHER_ROSE, 1))
          .add(1, new Drop(Material.TORCHFLOWER, 1))
          .add(1, new Drop(Material.ENDER_CHEST, 1))
          .add(1, new Drop(Material.EMERALD_BLOCK, 1))
          .add(1, new Drop(Material.EMERALD, 9))
          .add(1, new Drop(Material.SHULKER_BOX, 1))
          .add(1, new Drop(Material.SKELETON_SKULL, 1))
          .add(1, new Drop(Material.WITHER_SKELETON_SKULL, 1))
          .add(1, new Drop(Material.PLAYER_HEAD, 1))
          .add(1, new Drop(Material.ZOMBIE_HEAD, 1))
          .add(1, new Drop(Material.CREEPER_HEAD, 1))
          .add(1, new Drop(Material.PIGLIN_HEAD, 1))
          .add(100, new Drop(Material.DIAMOND_HORSE_ARMOR, 1))
          .add(20, new Drop(Material.ELYTRA, 1));

  private final WeightedRandomCollection<Integer> quantity =
      new WeightedRandomCollection<Integer>()
          .add(100, 4)
          .add(90, 5)
          .add(75, 6)
          .add(55, 7)
          .add(30, 8)
          .add(10, 9)
          .add(1, 10)
          .add(1, 11)
          .add(1, 12);

  @EventHandler
  public void onEntityDeathEvent(EntityDeathEvent event) {
    if (!event.getEntity().getType().equals(EntityType.WARDEN)) {
      return;
    }

    final var location = event.getEntity().getLocation();
    final var itemQuantity = quantity.next();

    for (var i = 0; i < itemQuantity; i++) {
      final var item = possibleItems.next().getStack();
      location.getWorld().dropItemNaturally(location, item);
    }
  }
}
