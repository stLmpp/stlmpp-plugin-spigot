package com.stlmpp.spigot.plugins.events.wardenbetterdrops;

import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.RandomList;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public record Drop(Material material, int maxAmount) {

  private static final Map<Material, RandomList<Enchantment>> map = new HashMap<>();

  static {
    List.of(
            Material.DIAMOND_SWORD,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_AXE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_SWORD,
            Material.NETHERITE_SHOVEL,
            Material.NETHERITE_PICKAXE,
            Material.NETHERITE_AXE,
            Material.NETHERITE_HOE,
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.GOLDEN_HELMET,
            Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS,
            Material.GOLDEN_BOOTS,
            Material.NETHERITE_HELMET,
            Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS,
            Material.NETHERITE_BOOTS)
        .forEach(
            material -> {
              final var enchantments =
                  RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()
                      .filter(enchantment -> enchantment.canEnchantItem(ItemStack.of(material)));
              map.put(material, new RandomList<>(enchantments.toList()));
            });
  }

  public ItemStack getStack() {
    final var item = ItemStack.of(material, ThreadLocalRandom.current().nextInt(1, maxAmount + 1));
    if (!Chance.of(70)) {
      return item;
    }
    final var possibleEnchantments = map.get(material);
    if (possibleEnchantments == null) {
      return item;
    }
    final var numberOfEnchantments = ThreadLocalRandom.current().nextInt(1, 6);
    for (int i = 0; i < numberOfEnchantments; i++) {
      final var enchantment = possibleEnchantments.next();
      final var level = ThreadLocalRandom.current().nextInt(1, enchantment.getMaxLevel() + 1);
      item.addEnchantment(enchantment, level);
    }
    return item;
  }
}
