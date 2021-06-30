package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class CaveInEvent implements Listener {

  private final StlmppPlugin plugin;
  private final int maxY;
  private final int minHeight;
  private final int minWidth;
  private final Set<Material> blocks = new HashSet<>();
  private final BlockFace[] blockFaces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

  public CaveInEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.maxY = this.plugin.config.getInt(Config.caveInMaxY);
    this.minHeight = this.plugin.config.getInt(Config.caveInMinHeight);
    this.minWidth = this.plugin.config.getInt(Config.caveInMinWidth);
    final var materialNames = this.plugin.config.getList(Config.caveInBlocks);
    if (materialNames != null) {
      Util.setMaterialsFromNames(this.blocks, materialNames);
    }
  }

  private int getBlockFaceIndex(double yaw) {
    var yawMod = yaw % 360;
    if (yawMod < 0) {
      yawMod += 360;
    }
    return (int) Math.round((yawMod / 90)) % 4;
  }

  private int getWidth(World world, Player player) {
    final var playerLocation = player.getLocation();
    final var playerFacingDirectionIndex = this.getBlockFaceIndex(player.getEyeLocation().getYaw());
    var rightDirectionIndex = playerFacingDirectionIndex + 1;
    if (rightDirectionIndex > this.blockFaces.length - 1) {
      rightDirectionIndex -= this.blockFaces.length;
    }
    var leftDirectionIndex = playerFacingDirectionIndex - 1;
    if (leftDirectionIndex < 0) {
      leftDirectionIndex += this.blockFaces.length;
    }
    final var rightBlockFace = this.blockFaces[rightDirectionIndex];
    final var leftBlockFace = this.blockFaces[leftDirectionIndex];
    final var blockPlayer = world.getBlockAt(playerLocation);
    var airBlocksToTheRight = 0;
    var blockRight = blockPlayer.getRelative(rightBlockFace);
    while (!blockRight.getType().isSolid() && airBlocksToTheRight <= 10) {
      airBlocksToTheRight++;
      blockRight = blockRight.getRelative(rightBlockFace);
    }
    var airBlocksToTheLeft = 0;
    var blockLeft = blockPlayer.getRelative(leftBlockFace);
    while (!blockLeft.getType().isSolid() && airBlocksToTheLeft <= 10) {
      airBlocksToTheLeft++;
      blockLeft = blockLeft.getRelative(leftBlockFace);
    }
    return (airBlocksToTheLeft + airBlocksToTheRight) + 1;
  }

  private Integer getHeight(World world, Location location) {
    final var floorY = Util.getFloor(world, location, true, 15);
    if (floorY == null) {
      return null;
    }
    final var ceilingY = Util.getCeiling(world, location, true, 15);
    if (ceilingY == null) {
      return null;
    }
    return (ceilingY - floorY) - 1;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBlockBreak(BlockBreakEvent event) {
    /*if (!Chance.of(this.plugin.config.getInt(Config.caveInChance))) {
      return;
    } TODO activate*/
    final var player = event.getPlayer();
    final var world = player.getWorld();
    if (!world.getName().equals(this.plugin.getWorldName())) {
      return;
    }
    final var playerLocation = player.getLocation();
    if (playerLocation.getY() > this.maxY) {
      return;
    }
    if (player.isSneaking()) {
      return;
    }
    final var block = event.getBlock();
    if (!this.blocks.contains(block.getType())) {
      return;
    }
    final var height = this.getHeight(world, playerLocation);
    Bukkit.broadcastMessage("HEIGHT = " + height);
    if (height == null || height < this.minHeight) {
      return;
    }
    Bukkit.broadcastMessage("WIDTH = " + this.getWidth(world, player));
    if (this.getWidth(world, player) < this.minWidth) {
      return;
    }
    final var blockAbovePlayer = world.getBlockAt(playerLocation.getBlockX(), height, playerLocation.getBlockZ());
    world.spawnFallingBlock(blockAbovePlayer.getLocation(), blockAbovePlayer.getBlockData());
    blockAbovePlayer.setType(Material.AIR);
  }
}
