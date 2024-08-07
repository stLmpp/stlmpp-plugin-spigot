package com.stlmpp.spigot.plugins.utils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Util {

  public static final Map<Material, Material> toNetherMaterialMap = new HashMap<>();
  public static final Material toNetherDefaultMaterial = Material.NETHERRACK;

  static {
    toNetherMaterialMap.put(Material.CHERRY_LOG, Material.CRIMSON_STEM);
    toNetherMaterialMap.put(Material.ACACIA_LOG, Material.CRIMSON_STEM);
    toNetherMaterialMap.put(Material.BIRCH_LOG, Material.CRIMSON_STEM);
    toNetherMaterialMap.put(Material.JUNGLE_LOG, Material.CRIMSON_STEM);
    toNetherMaterialMap.put(Material.SPRUCE_LOG, Material.WARPED_STEM);
    toNetherMaterialMap.put(Material.DARK_OAK_LOG, Material.WARPED_STEM);
    toNetherMaterialMap.put(Material.OAK_LOG, Material.WARPED_STEM);
    toNetherMaterialMap.put(Material.CHERRY_LEAVES, Material.NETHER_WART_BLOCK);
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

  public static final Set<Material> netherMaterials =
      new HashSet<>(
          List.of(
              Material.ANCIENT_DEBRIS,
              Material.BASALT,
              Material.POLISHED_BASALT,
              Material.SMOOTH_BASALT,
              Material.BLACKSTONE,
              Material.NETHER_BRICK_FENCE,
              Material.GILDED_BLACKSTONE,
              Material.GLOWSTONE,
              Material.MAGMA_BLOCK,
              Material.NETHER_BRICK,
              Material.NETHER_BRICK_SLAB,
              Material.NETHER_BRICK_STAIRS,
              Material.NETHER_BRICK_WALL,
              Material.NETHER_GOLD_ORE,
              Material.NETHER_QUARTZ_ORE,
              Material.QUARTZ_BLOCK,
              Material.QUARTZ_BRICKS,
              Material.QUARTZ_PILLAR,
              Material.QUARTZ_SLAB,
              Material.QUARTZ_STAIRS,
              Material.CHISELED_QUARTZ_BLOCK,
              Material.SMOOTH_QUARTZ,
              Material.SMOOTH_QUARTZ_STAIRS,
              Material.SMOOTH_QUARTZ_SLAB,
              Material.NETHER_SPROUTS,
              Material.NETHER_WART,
              Material.NETHER_WART_BLOCK,
              Material.NETHERRACK,
              Material.POLISHED_BLACKSTONE,
              Material.POLISHED_BLACKSTONE_BRICKS,
              Material.BLACKSTONE_SLAB,
              Material.BLACKSTONE_STAIRS,
              Material.CHISELED_POLISHED_BLACKSTONE,
              Material.CRACKED_POLISHED_BLACKSTONE_BRICKS,
              Material.POLISHED_BLACKSTONE_BRICK_SLAB,
              Material.POLISHED_BLACKSTONE_BRICK_STAIRS,
              Material.POLISHED_BLACKSTONE_SLAB,
              Material.POLISHED_BLACKSTONE_STAIRS,
              Material.POLISHED_BLACKSTONE_WALL,
              Material.SHROOMLIGHT,
              Material.SOUL_SAND,
              Material.SOUL_SOIL,
              Material.CRIMSON_STEM,
              Material.WARPED_STEM,
              Material.CRIMSON_PLANKS,
              Material.WARPED_PLANKS,
              Material.CRIMSON_HYPHAE,
              Material.CRIMSON_NYLIUM,
              Material.CRIMSON_SLAB,
              Material.CRIMSON_STAIRS,
              Material.WARPED_HYPHAE,
              Material.WARPED_NYLIUM,
              Material.WARPED_SLAB,
              Material.WARPED_STAIRS));

  public static int getFloor(Location location) {
    Integer floor = Util.getFloor(location, false, 256);
    if (floor == null) {
      // This is never going to happen
      floor = -1;
    }
    return floor;
  }

  public static Integer getFloor(
      Location location, boolean returnNullOnMaxIterations, int maxIterations) {
    location = location.clone();
    var iteration = 0;
    while (!location.getBlock().isSolid() && iteration <= maxIterations) {
      iteration++;
      location.subtract(0, 1, 0);
    }
    return iteration > maxIterations && returnNullOnMaxIterations ? null : location.getBlockY();
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
        players.get(ThreadLocalRandom.current().nextInt(playersSize)).getLocation());
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

  private static final List<BlockFace> blockFaces =
      List.of(
          BlockFace.NORTH,
          BlockFace.EAST,
          BlockFace.SOUTH,
          BlockFace.WEST,
          BlockFace.UP,
          BlockFace.DOWN,
          BlockFace.NORTH_EAST,
          BlockFace.NORTH_WEST,
          BlockFace.SOUTH_EAST,
          BlockFace.SOUTH_WEST);

  @NotNull
  public static List<Block> getRelativesFilter(
      @NotNull Collection<BlockFace> faces, @NotNull Block block, Predicate<Block> predicate) {
    final List<Block> list = new ArrayList<>();
    for (var blockFace : faces) {
      final var newBlock = block.getRelative(blockFace);
      if (predicate.test(newBlock)) {
        list.add(newBlock);
      }
    }
    return list;
  }

  @NotNull
  public static List<Block> getRelativesFilter(@NotNull Block block, Predicate<Block> predicate) {
    return getRelativesFilter(blockFaces, block, predicate);
  }

  @NotNull
  public static HashSet<Block> getBlocksAround(Block startBlock, Predicate<Block> predicate) {
    int currentIteration = 0;
    final var blocks = new HashSet<Block>();
    final var visited = new HashSet<Block>();
    blocks.add(startBlock);
    final var queue = new LinkedList<>(getRelativesFilter(startBlock, predicate));
    while (!queue.isEmpty() && currentIteration < 1024) {
      currentIteration++;
      final var block = queue.remove();
      if (visited.contains(block)) {
        continue;
      }
      visited.add(block);
      blocks.add(block);
      queue.addAll(getRelativesFilter(block, predicate));
    }
    return blocks;
  }

  public static HashSet<Block> getBlocksAround(Block startBlock) {
    return Util.getBlocksAround(
        startBlock, (block) -> block.getType().equals(startBlock.getType()));
  }

  public static final HashSet<Material> oreList =
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

  public static void setUntilSolid(
      Location location, BiFunction<Integer, Boolean, Material> function) {
    location = location.clone();
    while (!location.getBlock().isSolid()) {
      final var y = location.getBlockY();
      final var block = location.getBlock();
      location.subtract(0, 1, 0);
      block.setType(function.apply(y, location.getBlock().isSolid()));
    }
  }

  public static void setUntilSolid(Location location, Function<Integer, Material> function) {
    Util.setUntilSolid(location, (y, _isNextSolid) -> function.apply(y));
  }

  public static void setUntilSolid(Location location, Supplier<Material> function) {
    Util.setUntilSolid(location, (_y) -> function.get());
  }

  public static void setUntilSolid(Location location, Material material) {
    Util.setUntilSolid(location, () -> material);
  }

  public static final RandomList<Material> randomGlassList =
      new RandomList<>(
          List.of(
              Material.GLASS,
              Material.WHITE_STAINED_GLASS,
              Material.ORANGE_STAINED_GLASS,
              Material.MAGENTA_STAINED_GLASS,
              Material.LIGHT_BLUE_STAINED_GLASS,
              Material.YELLOW_STAINED_GLASS,
              Material.LIME_STAINED_GLASS,
              Material.PINK_STAINED_GLASS,
              Material.GRAY_STAINED_GLASS,
              Material.LIGHT_GRAY_STAINED_GLASS,
              Material.CYAN_STAINED_GLASS,
              Material.PURPLE_STAINED_GLASS,
              Material.BLUE_STAINED_GLASS,
              Material.BROWN_STAINED_GLASS,
              Material.GREEN_STAINED_GLASS,
              Material.RED_STAINED_GLASS,
              Material.BLACK_STAINED_GLASS));

  private static ItemStack getWithFortune(Material material) {
    final var item = new ItemStack(material);
    item.addEnchantment(Enchantment.FORTUNE, 3);
    return item;
  }

  private static final ArrayList<Supplier<ItemStack>> allTools =
      new ArrayList<>(
          List.of(
              () -> getWithFortune(Material.DIAMOND_PICKAXE),
              () -> getWithFortune(Material.DIAMOND_SHOVEL),
              () -> getWithFortune(Material.DIAMOND_AXE),
              () -> getWithFortune(Material.DIAMOND_HOE)));

  private static final Map<Material, Supplier<ItemStack>> allToolsFastLookup = new HashMap<>();

  public static ItemStack findBestTool(Block block) {
    var tool = allToolsFastLookup.get(block.getType());
    if (tool == null) {
      tool =
          Optional.ofNullable(
                  Util.findFirst(allTools, toolGetter -> block.isPreferredTool(toolGetter.get())))
              .orElse(allTools.getFirst());
      allToolsFastLookup.put(block.getType(), tool);
    }
    return tool.get();
  }
}
