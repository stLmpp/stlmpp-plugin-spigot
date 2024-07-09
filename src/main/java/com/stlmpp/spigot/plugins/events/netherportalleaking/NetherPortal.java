package com.stlmpp.spigot.plugins.events.netherportalleaking;

import java.util.List;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class NetherPortal {

  private final World world;
  public final List<Block> blocks;
  public final BoundingBox boundingBox;
  public final int width;

  private BoundingBox createBoundingBox() {
    final var firstBlock = this.blocks.getFirst();
    var minY = firstBlock.getY();
    var maxY = minY;
    var minX = firstBlock.getX();
    var maxX = minX;
    var minZ = firstBlock.getZ();
    var maxZ = minZ;
    for (Block block : this.blocks) {
      final var blockY = block.getY();
      if (blockY < minY) {
        minY = blockY;
      } else {
        maxY = blockY;
      }
      final var blockX = block.getX();
      if (blockX < minX) {
        minX = blockX;
      } else {
        maxX = blockX;
      }
      final var blockZ = block.getZ();
      if (blockZ < minZ) {
        minZ = blockZ;
      } else {
        maxZ = blockZ;
      }
    }
    return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
  }

  public NetherPortal(World world, List<Block> blocks) {
    this.world = world;
    this.blocks = blocks;
    this.blocks.sort(
        (blockA, blockB) -> {
          if (blockA.getX() != blockB.getX()) {
            return blockA.getX() - blockB.getX();
          } else if (blockA.getY() != blockB.getY()) {
            return blockA.getY() - blockB.getY();
          } else if (blockA.getZ() != blockB.getZ()) {
            return blockA.getZ() - blockB.getZ();
          } else {
            return 0;
          }
        });
    this.boundingBox = this.createBoundingBox();
    this.width =
        (int) Math.ceil(Math.max(this.boundingBox.getWidthX(), this.boundingBox.getWidthZ()));
  }

  public Location getCenterLocation() {
    return this.boundingBox.getCenter().toLocation(world);
  }

  public Vector getCenter() {
    return this.boundingBox.getCenter();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    NetherPortal that = (NetherPortal) object;
    return Objects.equals(this.boundingBox, that.boundingBox);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.boundingBox);
  }
}
