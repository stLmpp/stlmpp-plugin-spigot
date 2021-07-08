package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.Sound;
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
  private final int maxHeight;
  private final int minWidth;
  private final int maxWidth;
  private final Set<Material> blocks = new HashSet<>();
  private final BlockFace[] blockFaces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };

  public CaveInEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.maxY = this.plugin.config.getInt(StlmppPluginConfig.caveInMaxY);
    this.minHeight = this.plugin.config.getInt(StlmppPluginConfig.caveInMinHeight);
    this.maxHeight = this.plugin.config.getInt(StlmppPluginConfig.caveInMaxHeight);
    this.minWidth = this.plugin.config.getInt(StlmppPluginConfig.caveInMinWidth);
    this.maxWidth = this.plugin.config.getInt(StlmppPluginConfig.caveInMaxWidth);
    final var materialNames = this.plugin.config.getList(StlmppPluginConfig.caveInBlocks);
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
    final var leftBlockFace = this.getLeftBlockFace(player);

    final var rightBlockFace = leftBlockFace.getOppositeFace();
    final var blockPlayer = world.getBlockAt(playerLocation);
    var notSolidBlocksToTheRight = 0;
    var blockRight = blockPlayer.getRelative(rightBlockFace);
    while (!blockRight.getType().isSolid() && notSolidBlocksToTheRight <= this.maxWidth) {
      notSolidBlocksToTheRight++;
      blockRight = blockRight.getRelative(rightBlockFace);
    }
    var notSolidBlocksToTheLeft = 0;
    var blockLeft = blockPlayer.getRelative(leftBlockFace);
    while (!blockLeft.getType().isSolid() && notSolidBlocksToTheLeft <= this.maxWidth) {
      notSolidBlocksToTheLeft++;
      blockLeft = blockLeft.getRelative(leftBlockFace);
    }
    return (notSolidBlocksToTheLeft + notSolidBlocksToTheRight) + 1;
  }

  private BlockFace getFrontBlockFace(Player player) {
    final var playerFacingDirectionIndex = this.getBlockFaceIndex(player.getEyeLocation().getYaw());
    return this.blockFaces[playerFacingDirectionIndex].getOppositeFace();
  }

  private BlockFace getLeftBlockFace(Player player) {
    final var playerFacingDirectionIndex = this.getBlockFaceIndex(player.getEyeLocation().getYaw());
    var leftDirectionIndex = playerFacingDirectionIndex - 1;
    if (leftDirectionIndex < 0) {
      leftDirectionIndex += this.blockFaces.length;
    }
    return this.blockFaces[leftDirectionIndex].getOppositeFace();
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBlockBreak(BlockBreakEvent event) {
    if (!Chance.of(this.plugin.config.getDouble(StlmppPluginConfig.caveInChance))) {
      return;
    }
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
    if (height < this.minHeight) {
      return;
    }
    final var width = this.getWidth(world, player);
    if (width < this.minWidth) {
      return;
    }
    final var blockAbovePlayer = world.getBlockAt(playerLocation.getBlockX(), ceilingY, playerLocation.getBlockZ());
    final var widthOfCaveIn = Math.max(width - 2, 2);
    final var increaseSidesWidth = widthOfCaveIn / 2;
    final var increaseFront = ThreadLocalRandom.current().nextInt(3, 11);
    final var leftBlockFace = this.getLeftBlockFace(player);
    final var rightBlockFace = leftBlockFace.getOppositeFace();
    final var frontBlockFace = this.getFrontBlockFace(player);
    final var backBlockFace = frontBlockFace.getOppositeFace();
    final var caveInHeight = ThreadLocalRandom.current().nextInt(this.minHeight, this.maxHeight + 1);
    final var firstBlock = blockAbovePlayer
      .getRelative(backBlockFace, increaseFront)
      .getRelative(leftBlockFace, increaseSidesWidth);

    world.playSound(blockAbovePlayer.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);

    for (var x = 0; x < increaseFront * 2; x++) {
      for (var y = 0; y < caveInHeight; y++) {
        for (var z = 0; z < widthOfCaveIn; z++) {
          final var blockAt = firstBlock
            .getRelative(frontBlockFace, x)
            .getRelative(BlockFace.UP, y)
            .getRelative(rightBlockFace, z);
          if (blockAt.getType().isSolid()) {
            final var blockData = blockAt.getState().getBlockData();
            final var blockReplaced = world.getBlockAt(blockAt.getX(), blockAt.getY() - height, blockAt.getZ());
            if (!blockReplaced.getType().isSolid()) {
              blockReplaced.setBlockData(blockData);
              blockAt.setType(Material.AIR);
            }
          }
        }
      }
    }
  }
}
