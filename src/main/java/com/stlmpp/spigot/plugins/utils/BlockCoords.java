package com.stlmpp.spigot.plugins.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.Objects;

public class BlockCoords {

  public final int x;
  public final int y;
  public final int z;

  public BlockCoords(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public BlockCoords(@NotNull Block block) {
    this(block.getX(), block.getY(), block.getZ());
  }

  public BlockCoords(@NotNull Location location) {
    this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  public BlockCoords(@NotNull Vector vector) {
    this(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
  }

  public BlockCoords(@NotNull BlockVector vector) {
    this(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
  }

  public BlockCoords(@NotNull Vector3d vector) {
    this((int) vector.x(), (int) vector.y(), (int) vector.z());
  }

  public Block toBlock(@NotNull World world) {
    return world.getBlockAt(x, y, z);
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj
        || (obj instanceof BlockCoords blockCoords
            && blockCoords.x == x
            && blockCoords.y == y
            && blockCoords.z == z);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, z);
  }

  @Override
  public String toString() {
    return "BlockCoords{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
