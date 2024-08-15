package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.RandomList;
import com.stlmpp.spigot.plugins.utils.Util;
import com.stlmpp.spigot.plugins.utils.WeightedRandomCollection;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class DeathEvent implements Listener {

  public static @Nullable DeathEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.deathEventEnabled)) {
      return null;
    }
    return new DeathEvent(plugin);
  }

  private DeathEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    plugin.log("Death Event activated");
  }

  private final StlmppPlugin plugin;
  private final RandomList<String> messages =
      new RandomList<>(
          List.of(
              "%s morreu e seus itens foram duplicados para a coordenada %s %s %s ... espero que um dia voce consiga recuperar",
              "%s esta morto... Seus itens estao na coordenada %s %s %s",
              "%s anota essas coordenadas: %s %s %s, seus itens estao la",
              "%s nao se preocupe, seu itens foram armazenados e estao na coordenada %s %s %s",
              "%s soh morre ein, vai pegar seus itens na coordenada %s %s %s"));
  private final WeightedRandomCollection<Material> floorTypes =
      new WeightedRandomCollection<Material>()
          .add(1000, Material.COBBLESTONE)
          .add(750, Material.STONE)
          .add(500, Material.IRON_BLOCK)
          .add(250, Material.COPPER_BLOCK)
          .add(100, Material.COAL_BLOCK)
          .add(50, Material.QUARTZ_BLOCK)
          .add(25, Material.QUARTZ_BLOCK)
          .add(15, Material.REDSTONE_BLOCK)
          .add(10, Material.EMERALD_BLOCK)
          .add(5, Material.DIAMOND_BLOCK)
          .add(2.5, Material.HONEY_BLOCK)
          .add(1.5, Material.SLIME_BLOCK)
          .add(1.0, Material.STRIPPED_BAMBOO_BLOCK)
          .add(0.75, Material.ANCIENT_DEBRIS)
          .add(0.5, Material.NETHERITE_BLOCK);

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeathEvent(PlayerDeathEvent event) {
    if (event.getDrops().isEmpty()) {
      return;
    }

    final var overworld = this.plugin.getWorld();

    if (overworld == null) {
      return;
    }

    var location = event.getPlayer().getLocation();

    if (!location.getWorld().getName().equals(plugin.getWorldName())) {
      location = location.clone();
      location.multiply(8);
      location.setY(62);
      location.setWorld(overworld);
    }

    location =
        Util.getRandomLocationAroundLocation(
            location, new BoundingBox(-2048, -60, -2048, 2048, 320, 2048));

    plugin.log(String.format("Location = %s", location), true);

    final var chest1 = location.getBlock();
    final var chest2 = location.clone().add(1, 0, 0).getBlock();
    chest1.setType(Material.CHEST);
    chest2.setType(Material.CHEST);

    final var floorBlock = floorTypes.next();

    if (!chest1.getRelative(BlockFace.DOWN).isSolid()) {
      chest1.getRelative(BlockFace.DOWN).setType(floorBlock);
    }

    if (!chest2.getRelative(BlockFace.DOWN).isSolid()) {
      chest2.getRelative(BlockFace.DOWN).setType(floorBlock);
    }

    final var chest1Data = chest1.getBlockData();
    final var chest2Data = chest2.getBlockData();

    if (chest1Data instanceof Chest c1d) {
      c1d.setType(Chest.Type.LEFT);
    }

    if (chest2Data instanceof Chest c2d) {
      c2d.setType(Chest.Type.RIGHT);
    }

    chest1.setBlockData(chest1Data);
    chest2.setBlockData(chest2Data);

    final var state = chest1.getState();

    plugin.log(String.format("is chest %s", state instanceof org.bukkit.block.Chest), true);

    if (state instanceof org.bukkit.block.Chest c1
        && c1.getInventory().getHolder() instanceof DoubleChest doubleChest) {
      for (ItemStack item : event.getDrops()) {
        if (item == null) {
          continue;
        }
        plugin.log(String.format("item = %s", item), true);
        doubleChest.getInventory().addItem(item);
      }
    }

    plugin
        .getServer()
        .broadcast(
            Component.text(
                String.format(
                    messages.next(),
                    event.getPlayer().getName(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ())));
  }
}
