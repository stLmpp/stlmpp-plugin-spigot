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
    final var floorY = Util.getFloor(world, playerLocation, true, 15);
    if (floorY == null) {
      return;
    }
    final var ceilingY = Util.getCeiling(world, playerLocation, true, 15);
    if (ceilingY == null) {
      return;
    }
    final var height = ceilingY - floorY;
    Bukkit.broadcastMessage("HEIGHT = " + height);
    if (height < this.minHeight) {
      return;
    }
    final var width = this.getWidth(world, player);
    Bukkit.broadcastMessage("WIDTH = " + width);
    Bukkit.broadcastMessage("Min-width = " + this.minWidth);
    if (width < this.minWidth) {
      return;
    }
    final var blockAbovePlayer = world.getBlockAt(playerLocation.getBlockX(), ceilingY, playerLocation.getBlockZ());
    final var blockAbovePlayerType = blockAbovePlayer.getState().getData();
    var x = playerLocation.getBlockX();
    int y = ceilingY;
    var z = playerLocation.getBlockZ();
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        for (int k = 0; k < 2; k++) {
          var blockAt = world.getBlockAt(x, y, z);
          Bukkit.broadcastMessage(x + " " + y + " " + z + " = " + blockAt.getType().name());
          if (blockAt.getType().isSolid()) {
            var blockAtData = blockAt.getState().getData();
            blockAt.setType(Material.AIR);
            var location = new Location(world, x, y, z);
            world.spawnFallingBlock(location, blockAtData);
          }
          x++;
        }
        z++;
      }
      y++;
    }
  }
}
