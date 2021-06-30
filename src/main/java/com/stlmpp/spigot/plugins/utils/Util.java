package com.stlmpp.spigot.plugins.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class Util {

  public static int getFloor(World world, Location location) {
    Integer floor = Util.getFloor(world, location, false, 64);
    if (floor == null) {
      // This is never going to happen
      floor = -1;
    }
    return floor;
  }

  public static Integer getFloor(World world, Location location, boolean returnNullOnMaxIterations) {
    return Util.getFloor(world, location, returnNullOnMaxIterations, 64);
  }

  public static Integer getFloor(World world, Location location, boolean returnNullOnMaxIterations, int maxIterations) {
    var iteration = 0;
    var locationY = location.getBlockY();
    while (
      world.getBlockAt(location.getBlockX(), locationY, location.getBlockZ()).getType().isSolid() &&
      iteration <= maxIterations
    ) {
      iteration++;
      locationY--;
    }
    return iteration > maxIterations && returnNullOnMaxIterations ? null : locationY;
  }

  public static int getCeiling(World world, Location location) {
    Integer floor = Util.getCeiling(world, location, false, 64);
    if (floor == null) {
      // This is never going to happen
      floor = -1;
    }
    return floor;
  }

  public static Integer getCeiling(World world, Location location, boolean returnNullOnMaxIterations) {
    return Util.getCeiling(world, location, returnNullOnMaxIterations, 64);
  }

  public static Integer getCeiling(
    World world,
    Location location,
    boolean returnNullOnMaxIterations,
    int maxIterations
  ) {
    var iteration = 0;
    var locationY = location.getBlockY();
    while (
      !world.getBlockAt(location.getBlockX(), locationY, location.getBlockZ()).getType().isSolid() &&
      iteration <= maxIterations
    ) {
      iteration++;
      locationY++;
    }
    return iteration > maxIterations && returnNullOnMaxIterations ? null : locationY;
  }

  public static void setMaterialsFromNames(@NotNull Set<Material> materialSet, @NotNull List<?> materialNames) {
    for (Object materialName : materialNames) {
      if (!(materialName instanceof String materialNameString)) {
        continue;
      }
      final var material = Material.getMaterial(materialNameString);
      if (material != null) {
        materialSet.add(material);
      }
    }
  }
}
