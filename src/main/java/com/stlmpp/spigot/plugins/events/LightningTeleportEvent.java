package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Util;
import com.stlmpp.spigot.plugins.utils.WeightedRandomCollection;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

public class LightningTeleportEvent implements Listener {

  public static @Nullable LightningTeleportEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.tpLightningEnabled)) {
      return null;
    }
    return new LightningTeleportEvent(plugin);
  }

  private final StlmppPlugin plugin;
  private final int radius;
  private final double explosionChance;
  private final WeightedRandomCollection<Material> materials = new WeightedRandomCollection<>();
  private final Set<Material> allowedMaterialsToReplace = new HashSet<>();
  private final Map<Material, ArrayList<Material>> specialReplacements = new HashMap<>();

  private LightningTeleportEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.radius = this.plugin.config.getInt(StlmppPluginConfig.tpLightningNetherBlocksRadius);
    this.explosionChance =
        this.plugin.config.getDouble(StlmppPluginConfig.tpLightningExplosionChance);
    this.plugin.log("Teleport lightning enabled");
    this.materials
        .add(1.0, Material.ANCIENT_DEBRIS)
        .add(200.0, Material.NETHERRACK)
        .add(10.0, Material.MAGMA_BLOCK)
        .add(2.0, Material.NETHER_GOLD_ORE)
        .add(5.0, Material.GILDED_BLACKSTONE);
    this.allowedMaterialsToReplace.addAll(
        List.of(
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.COBBLESTONE,
            Material.DEEPSLATE,
            Material.ANDESITE,
            Material.BLACKSTONE,
            Material.BRICK,
            Material.GRAVEL,
            Material.SAND,
            Material.SANDSTONE,
            Material.STONE,
            Material.SMOOTH_STONE,
            Material.EMERALD_BLOCK,
            Material.DIRT_PATH));
    this.specialReplacements.put(
        Material.SAND,
        new ArrayList<>(
            List.of(
                Material.GLASS,
                Material.WHITE_STAINED_GLASS,
                Material.ORANGE_STAINED_GLASS,
                Material.MAGENTA_STAINED_GLASS,
                Material.LIGHT_BLUE_STAINED_GLASS,
                Material.YELLOW_STAINED_GLASS,
                Material.LIME_STAINED_GLASS,
                Material.PINK_STAINED_GLASS,
                Material.GRAY_STAINED_GLASS,
                Material.LIGHT_GRAY_STAINED_GLASS,
                Material.CYAN_STAINED_GLASS,
                Material.PURPLE_STAINED_GLASS,
                Material.BLUE_STAINED_GLASS,
                Material.BROWN_STAINED_GLASS,
                Material.GREEN_STAINED_GLASS,
                Material.RED_STAINED_GLASS,
                Material.BLACK_STAINED_GLASS)));
    this.specialReplacements.put(
        Material.EMERALD_BLOCK, new ArrayList<>(List.of(Material.ANCIENT_DEBRIS)));
  }

  private void setBlocksRadius(World world, Location location) {
    final var startingX = location.getBlockX();
    final var startingY = location.getBlockY() + 5;
    final var startingZ = location.getBlockZ();
    final var radiusSquared = this.radius * this.radius;
    for (var x = startingX - this.radius; x <= startingX + this.radius; x++) {
      for (var z = startingZ - this.radius; z <= startingZ + this.radius; z++) {
        double dist = (startingX - x) * (startingX - x) + (startingZ - z) * (startingZ - z);
        if (dist >= radiusSquared) {
          continue;
        }
        final var floorY = Util.getFloor(world, new Location(world, x, startingY, z));
        final var block = world.getBlockAt(x, floorY, z);

        if (!this.allowedMaterialsToReplace.contains(block.getType())
            || !block.getType().isSolid()
            || !Chance.of(70)) {
          continue;
        }
        final var newType = this.specialReplacements.get(block.getType());
        if (newType != null) {
          final int randomIndex = new Random().nextInt(newType.size());
          block.setType(newType.get(randomIndex));
        } else {
          block.setType(this.materials.next());
        }
      }
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) {
      return;
    }
    final var player = event.getPlayer();
    final var world = player.getWorld();
    if (!player.isOp() || !world.getName().equals(this.plugin.getWorldName())) {
      return;
    }
    final var from = event.getFrom().clone();
    from.setY(Util.getFloor(world, from));
    final var to = event.getTo().clone();
    to.setY(Util.getFloor(world, to));
    world.strikeLightning(from);
    world.strikeLightning(to);
    this.setBlocksRadius(world, from);
    this.setBlocksRadius(world, to);
    if (!Chance.of(this.explosionChance)) {
      return;
    }
    world.createExplosion(
        from, ThreadLocalRandom.current().nextInt(0, (this.radius / 2) + 1), true, true);
    world.createExplosion(
        to, ThreadLocalRandom.current().nextInt(0, (this.radius / 2) + 1), true, true);
  }
}
