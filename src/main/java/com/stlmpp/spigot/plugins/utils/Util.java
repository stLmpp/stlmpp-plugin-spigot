package com.stlmpp.spigot.plugins.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class Util {

  public static Map<Material, Material> toNetherMaterialMap = new HashMap<>();
  public static Material toNetherDefaultMaterial = Material.NETHERRACK;

  static {
    toNetherMaterialMap.put(Material.ACACIA_LOG, Material.CRIMSON_STEM);
    toNetherMaterialMap.put(Material.BIRCH_LOG, Material.CRIMSON_STEM);
    toNetherMaterialMap.put(Material.JUNGLE_LOG, Material.CRIMSON_STEM);
    toNetherMaterialMap.put(Material.SPRUCE_LOG, Material.WARPED_STEM);
    toNetherMaterialMap.put(Material.DARK_OAK_LOG, Material.WARPED_STEM);
    toNetherMaterialMap.put(Material.OAK_LOG, Material.WARPED_STEM);
    toNetherMaterialMap.put(Material.ACACIA_LEAVES, Material.NETHER_WART_BLOCK);
    toNetherMaterialMap.put(Material.BIRCH_LEAVES, Material.NETHER_WART_BLOCK);
    toNetherMaterialMap.put(Material.JUNGLE_LEAVES, Material.NETHER_WART_BLOCK);
    toNetherMaterialMap.put(Material.SPRUCE_LEAVES, Material.WARPED_WART_BLOCK);
    toNetherMaterialMap.put(Material.DARK_OAK_LEAVES, Material.WARPED_WART_BLOCK);
    toNetherMaterialMap.put(Material.OAK_LEAVES, Material.WARPED_WART_BLOCK);
    toNetherMaterialMap.put(Material.STONE_BRICKS, Material.NETHER_BRICK);
  }

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
      !world.getBlockAt(location.getBlockX(), locationY, location.getBlockZ()).getType().isSolid() &&
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

  public static Material convertToNetherMaterial(Material material) {
    var netherMaterial = Util.toNetherMaterialMap.get(material);
    if (netherMaterial == null) {
      netherMaterial = Util.toNetherDefaultMaterial;
    }
    return netherMaterial;
  }
}
