package com.stlmpp.spigot.plugins.utils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Util {

  public static final Map<Material, Material> toNetherMaterialMap = new HashMap<>();
  public static final Material toNetherDefaultMaterial = Material.NETHERRACK;

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
    toNetherMaterialMap.put(Material.WATER, Material.LAVA);
    toNetherMaterialMap.put(Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE);
    toNetherMaterialMap.put(Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE);
    toNetherMaterialMap.put(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE);
    toNetherMaterialMap.put(Material.EMERALD_ORE, Material.ANCIENT_DEBRIS);
    toNetherMaterialMap.put(Material.GOLD_ORE, Material.NETHER_GOLD_ORE);
    toNetherMaterialMap.put(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE);
    toNetherMaterialMap.put(Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE);
    toNetherMaterialMap.put(Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE);
    toNetherMaterialMap.put(Material.IRON_BLOCK, Material.COPPER_BLOCK);
    toNetherMaterialMap.put(Material.DIAMOND_BLOCK, Material.COPPER_BLOCK);
    toNetherMaterialMap.put(Material.SAND, Material.SOUL_SAND);
    toNetherMaterialMap.put(Material.COPPER_BLOCK, Material.COPPER_BLOCK);
    toNetherMaterialMap.put(Material.GOLD_BLOCK, Material.GOLD_BLOCK);
    toNetherMaterialMap.put(Material.SANDSTONE, Material.SOUL_SOIL);
    toNetherMaterialMap.put(Material.PUMPKIN, Material.JACK_O_LANTERN);
  }

  public static final Set<Material> netherMaterials = new HashSet<>();

  static {
    netherMaterials.add(Material.ANCIENT_DEBRIS);
    netherMaterials.add(Material.BASALT);
    netherMaterials.add(Material.POLISHED_BASALT);
    netherMaterials.add(Material.SMOOTH_BASALT);
    netherMaterials.add(Material.BLACKSTONE);
    netherMaterials.add(Material.NETHER_BRICK_FENCE);
    netherMaterials.add(Material.GILDED_BLACKSTONE);
    netherMaterials.add(Material.GLOWSTONE);
    netherMaterials.add(Material.MAGMA_BLOCK);
    netherMaterials.add(Material.NETHER_BRICK);
    netherMaterials.add(Material.NETHER_BRICK_SLAB);
    netherMaterials.add(Material.NETHER_BRICK_STAIRS);
    netherMaterials.add(Material.NETHER_BRICK_WALL);
    netherMaterials.add(Material.NETHER_GOLD_ORE);
    netherMaterials.add(Material.NETHER_QUARTZ_ORE);
    netherMaterials.add(Material.QUARTZ_BLOCK);
    netherMaterials.add(Material.QUARTZ_BRICKS);
    netherMaterials.add(Material.QUARTZ_PILLAR);
    netherMaterials.add(Material.QUARTZ_SLAB);
    netherMaterials.add(Material.QUARTZ_STAIRS);
    netherMaterials.add(Material.CHISELED_QUARTZ_BLOCK);
    netherMaterials.add(Material.SMOOTH_QUARTZ);
    netherMaterials.add(Material.SMOOTH_QUARTZ_STAIRS);
    netherMaterials.add(Material.SMOOTH_QUARTZ_SLAB);
    netherMaterials.add(Material.NETHER_SPROUTS);
    netherMaterials.add(Material.NETHER_WART);
    netherMaterials.add(Material.NETHER_WART_BLOCK);
    netherMaterials.add(Material.NETHERRACK);
    netherMaterials.add(Material.POLISHED_BLACKSTONE);
    netherMaterials.add(Material.POLISHED_BLACKSTONE_BRICKS);
    netherMaterials.add(Material.BLACKSTONE_SLAB);
    netherMaterials.add(Material.BLACKSTONE_STAIRS);
    netherMaterials.add(Material.CHISELED_POLISHED_BLACKSTONE);
    netherMaterials.add(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
    netherMaterials.add(Material.POLISHED_BLACKSTONE_BRICK_SLAB);
    netherMaterials.add(Material.POLISHED_BLACKSTONE_BRICK_STAIRS);
    netherMaterials.add(Material.POLISHED_BLACKSTONE_SLAB);
    netherMaterials.add(Material.POLISHED_BLACKSTONE_STAIRS);
    netherMaterials.add(Material.POLISHED_BLACKSTONE_WALL);
    netherMaterials.add(Material.SHROOMLIGHT);
    netherMaterials.add(Material.SOUL_SAND);
    netherMaterials.add(Material.SOUL_SOIL);
    netherMaterials.add(Material.CRIMSON_STEM);
    netherMaterials.add(Material.WARPED_STEM);
    netherMaterials.add(Material.CRIMSON_PLANKS);
    netherMaterials.add(Material.WARPED_PLANKS);
    netherMaterials.add(Material.CRIMSON_HYPHAE);
    netherMaterials.add(Material.CRIMSON_NYLIUM);
    netherMaterials.add(Material.CRIMSON_SLAB);
    netherMaterials.add(Material.CRIMSON_STAIRS);
    netherMaterials.add(Material.WARPED_HYPHAE);
    netherMaterials.add(Material.WARPED_NYLIUM);
    netherMaterials.add(Material.WARPED_SLAB);
    netherMaterials.add(Material.WARPED_STAIRS);
  }

  public static int getFloor(Location location) {
    Integer floor = Util.getFloor(location, false, 128);
    if (floor == null) {
      // This is never going to happen
      floor = -1;
    }
    return floor;
  }

  public static Integer getFloor(
      Location location, boolean returnNullOnMaxIterations, int maxIterations) {
    var iteration = 0;
    var locationY = location.getBlockY();
    while (!location
            .getWorld()
            .getBlockAt(location.getBlockX(), locationY, location.getBlockZ())
            .getType()
            .isSolid()
        && iteration <= maxIterations) {
      iteration++;
      locationY--;
    }
    return iteration > maxIterations && returnNullOnMaxIterations ? null : locationY;
  }

  public static void setMaterialsFromNames(
      @NotNull Set<Material> materialSet, @NotNull List<?> materialNames) {
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

  public static Location getRandomLocationAroundLocation(Location location, BoundingBox distance) {
    final var newLocation = location.clone();
    newLocation.setX(
        ThreadLocalRandom.current()
            .nextInt(
                location.getBlockX() + (int) distance.getMinX(),
                location.getBlockX() + (int) distance.getMaxX()));
    newLocation.setY(
        ThreadLocalRandom.current()
            .nextInt(
                location.getBlockY() + (int) distance.getMinY(),
                location.getBlockY() + (int) distance.getMaxY()));
    newLocation.setZ(
        ThreadLocalRandom.current()
            .nextInt(
                location.getBlockZ() + (int) distance.getMinZ(),
                location.getBlockZ() + (int) distance.getMaxZ()));
    return newLocation;
  }

  public static Location getRandomLocationAroundLocation(Location location) {
    return getRandomLocationAroundLocation(location, new BoundingBox(-50, -10, -50, 51, 11, 51));
  }

  @Nullable
  public static Location getRandomLocationAroundRandomPlayer(World world) {
    final var players = world.getPlayers();
    final var playersSize = players.size();
    if (playersSize == 0) {
      return null;
    }
    return Util.getRandomLocationAroundLocation(
        players.get(ThreadLocalRandom.current().nextInt(0, playersSize)).getLocation());
  }

  public static Material convertToNetherMaterial(Material material) {
    var netherMaterial = Util.toNetherMaterialMap.get(material);
    if (netherMaterial == null) {
      netherMaterial = Util.toNetherDefaultMaterial;
    }
    return netherMaterial;
  }

  public static boolean isFromNether(Material material) {
    return Util.netherMaterials.contains(material);
  }

  public static float randomFloat(float startInclusive, float endExclusive) {
    if (startInclusive == endExclusive) {
      return startInclusive;
    }
    return startInclusive
        + ((endExclusive - startInclusive) * ThreadLocalRandom.current().nextFloat());
  }

  @Nullable
  public static <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
    for (var element : collection) {
      if (predicate.test(element)) {
        return element;
      }
    }
    return null;
  }

  private static final BlockFace[] blockFaces = {
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST,
    BlockFace.UP,
    BlockFace.DOWN,
    BlockFace.NORTH_EAST,
    BlockFace.NORTH_WEST,
    BlockFace.SOUTH_EAST,
    BlockFace.SOUTH_WEST
  };

  private static List<Block> getRelatives(@NotNull Block block) {
    return Arrays.stream(blockFaces).map(block::getRelative).toList();
  }

  public static HashSet<Block> getBlocksAround(Block startBlock) {
    int currentIteration = 0;
    final var blocks = new HashSet<Block>();
    final var visited = new HashSet<Block>();
    blocks.add(startBlock);
    final var queue = new LinkedList<>(getRelatives(startBlock));
    while (!queue.isEmpty() && currentIteration < 1024) {
      currentIteration++;
      final var block = queue.remove();
      if (visited.contains(block)) {
        continue;
      }
      visited.add(block);
      if (!block.getType().equals(startBlock.getType())) {
        continue;
      }
      blocks.add(block);
      queue.addAll(getRelatives(block));
    }
    return blocks;
  }

  private static final HashSet<Material> ores =
      new HashSet<>(
          List.of(
              Material.IRON_ORE,
              Material.COAL_ORE,
              Material.DIAMOND_ORE,
              Material.COPPER_ORE,
              Material.EMERALD_ORE,
              Material.GOLD_ORE,
              Material.LAPIS_ORE,
              Material.REDSTONE_ORE,
              Material.DEEPSLATE_IRON_ORE,
              Material.DEEPSLATE_COAL_ORE,
              Material.DEEPSLATE_DIAMOND_ORE,
              Material.DEEPSLATE_COPPER_ORE,
              Material.DEEPSLATE_EMERALD_ORE,
              Material.DEEPSLATE_GOLD_ORE,
              Material.DEEPSLATE_LAPIS_ORE,
              Material.DEEPSLATE_REDSTONE_ORE,
              Material.NETHER_GOLD_ORE,
              Material.NETHER_QUARTZ_ORE,
              Material.ANCIENT_DEBRIS));

  public static boolean isOre(Block block) {
    return ores.contains(block.getType());
  }
}
