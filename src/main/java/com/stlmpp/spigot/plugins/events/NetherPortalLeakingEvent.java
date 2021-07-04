package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Pair;
import java.util.ArrayDeque;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.util.BoundingBox;

public class NetherPortalLeakingEvent implements Listener {

  private final StlmppPlugin plugin;
  private final int radius;

  public NetherPortalLeakingEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.radius = this.plugin.config.getInt(Config.netherPortalLeakingRadius);
  }

  public int getPortalMinRadius(List<Block> blocks) {
    final var initialBlock = blocks.get(0);
    var height = 1;
    var iterations = 0;
    while (initialBlock.getRelative(BlockFace.UP, height).getType() != Material.OBSIDIAN && iterations <= 25) {
      height++;
      iterations++;
    }
    return ((blocks.size() - ((height - 1) * 2)) / 2) + 2;
  }

  public Location getStartingLocation(World world, List<Block> blocks) {
    final var firstBlock = blocks.get(0);
    var minY = Integer.MAX_VALUE;
    var maxY = Integer.MIN_VALUE;
    var minX = Integer.MAX_VALUE;
    var maxX = Integer.MIN_VALUE;
    var minZ = Integer.MAX_VALUE;
    var maxZ = Integer.MIN_VALUE;
    for (Block block : blocks) {
      final var blockY = block.getY();
      if (blockY < minY) {
        minY = blockY;
      } else if (blockY > maxY) {
        maxY = blockY;
      } else {
        minY = blockY;
        maxY = blockY;
      }
      final var blockX = block.getX();
      if (blockX < minX) {
        minX = blockX;
      } else if (blockX > maxX) {
        maxX = blockX;
      } else {
        minX = blockX;
        maxX = blockX;
      }
      final var blockZ = block.getZ();
      if (blockZ < minZ) {
        minZ = blockZ;
      } else if (blockZ > maxZ) {
        maxZ = blockZ;
      } else {
        minZ = blockZ;
        maxZ = blockZ;
      }
    }
    var boundingBox = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    return new Location(world, boundingBox.getCenterX(), boundingBox.getCenterY(), boundingBox.getCenterZ());
  }

  @EventHandler
  public void onPortalCreate(PortalCreateEvent event) {
    final var world = event.getWorld();
    if (!world.getName().equals(this.plugin.getWorldName())) {
      return;
    }
    final var blocks = event.getBlocks().stream().map(BlockState::getBlock).toList();
    final var blocksObsidian = blocks.stream().filter(block -> block.getType() == Material.OBSIDIAN).toList();
    final var locations = new ArrayDeque<Pair<Double, Location>>();
    final var radius = Math.max(this.getPortalMinRadius(blocksObsidian), this.radius);
    final var initialBlockLocation = this.getStartingLocation(world, blocksObsidian);
    final var startingX = initialBlockLocation.getBlockX();
    final var startingY = initialBlockLocation.getBlockY();
    final var startingZ = initialBlockLocation.getBlockZ();
    Bukkit.broadcastMessage("INITIAL - X " + startingX + " Y " + startingY + " Z " + startingZ);
    for (int x = startingX - radius; x <= startingX + radius; x++) {
      for (int y = startingY - radius; y <= startingY + radius; y++) {
        for (int z = startingZ - radius; z <= startingZ + radius; z++) {
          final var distance = Math.sqrt(
            Math.pow(startingX - x, 2) + Math.pow(startingY - y, 2) + Math.pow(startingZ - z, 2)
          );
          if (distance <= radius) {
            final var blockAt = world.getBlockAt(x, y, z);
            final var blockAtMaterial = blockAt.getType();
            if (
              blockAtMaterial == Material.OBSIDIAN ||
              ((!blockAtMaterial.isSolid() || !blockAtMaterial.isBlock()) && blockAtMaterial != Material.WATER)
            ) {
              continue;
            }
            locations.add(new Pair<>(distance, blockAt.getLocation()));
          }
        }
      }
    }
    if (locations.size() == 0) {
      return;
    }
    final var locationsSorted = new ArrayDeque<>(
      locations
        .stream()
        .sorted(
          (pairA, pairB) -> {
            final var distanceA = pairA.value0;
            final var distanceB = pairB.value0;
            if (distanceA.equals(distanceB)) {
              return 0;
            } else if (distanceA < distanceB) {
              return -1;
            } else {
              return 1;
            }
          }
        )
        .map(pair -> pair.value1)
        .toList()
    );
    // new NetherPortalLeakingTask(this.plugin, locationsSorted, world);
  }
}
