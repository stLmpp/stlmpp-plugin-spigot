package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Util;
import com.stlmpp.spigot.plugins.utils.WeightedRandomCollection;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class LightningTeleportEvent implements Listener {

  private final StlmppPlugin plugin;
  private final int chance;
  private final int radius;
  private final int explosionChance;
  private final WeightedRandomCollection<Material> materials = new WeightedRandomCollection<>();

  public LightningTeleportEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.chance = this.plugin.config.getInt(Config.tpLightningChance);
    this.radius = this.plugin.config.getInt(Config.tpLightningNetherBlocksRadius);
    this.explosionChance = this.plugin.config.getInt(Config.tpLightningExplosionChance);
    this.materials.add(1.0, Material.ANCIENT_DEBRIS)
      .add(200.0, Material.NETHERRACK)
      .add(10.0, Material.MAGMA_BLOCK)
      .add(2.0, Material.NETHER_GOLD_ORE)
      .add(5.0, Material.GILDED_BLACKSTONE);
  }

  private void setBlocksRadius(World world, Location location) {
    final var startingX = location.getBlockX();
    final var startingY = location.getBlockY() + 5;
    final var startingZ = location.getBlockZ();
    final var radiusSquared = this.radius * this.radius;
    for (var x = startingX - this.radius; x <= startingX + this.radius; x++) {
      for (var z = startingZ - this.radius; z <= startingZ + this.radius; z++) {
        double dist = (startingX - x) * (startingX - x) + (startingZ - z) * (startingZ - z);
        if (dist < radiusSquared) {
          final var floorY = Util.getFloor(world, new Location(world, x, startingY, z));
          final var block = world.getBlockAt(x, floorY, z);
          if (block.getType().isSolid() && Chance.of(70)) {
            block.setType(this.materials.next());
          }
        }
      }
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    final var player = event.getPlayer();
    final var world = player.getWorld();
    if (!player.isOp() || !Chance.of(this.chance) || !world.getName().equals(this.plugin.getWorldName())) {
      return;
    }
    final var playerLocation = player.getLocation();
    final var floorLocation = new Location(
      world,
      playerLocation.getBlockX(),
      Util.getFloor(world, playerLocation),
      playerLocation.getBlockZ()
    );
    world.strikeLightning(floorLocation);
    this.setBlocksRadius(world, playerLocation);
    if (!Chance.of(this.explosionChance)) {
      return;
    }
    world.createExplosion(floorLocation, ThreadLocalRandom.current().nextInt(0, (this.radius / 2) + 1), true, true);
  }
}
