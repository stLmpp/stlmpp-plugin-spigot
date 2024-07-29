package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.RandomList;
import com.stlmpp.spigot.plugins.utils.Tick;
import java.util.*;

import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperMiningMachine {

  public SuperMiningMachine(
      StlmppPlugin plugin,
      @NotNull List<Block> blocks,
      @NotNull Block bottomLeftBlock,
      @NotNull Block bottomRightBlock,
      @NotNull Block topLeftBlock,
      @NotNull Block topRightBlock) {
    this.plugin = plugin;
    this.blockVectors =
        new HashSet<>(
            blocks.stream().map(block -> block.getLocation().toVector().toBlockVector()).toList());
    this.bottomLeftBlock = bottomLeftBlock;
    this.bottomRightBlock = bottomRightBlock;
    this.topLeftBlock = topLeftBlock;
    this.topRightBlock = topRightBlock;
    final var xList =
        List.of(
            bottomLeftBlock.getX(),
            bottomRightBlock.getX(),
            topLeftBlock.getX(),
            topRightBlock.getX());
    final var yList =
        List.of(
            bottomLeftBlock.getY(),
            bottomRightBlock.getY(),
            topLeftBlock.getY(),
            topRightBlock.getY());
    final var zList =
        List.of(
            bottomLeftBlock.getZ(),
            bottomRightBlock.getZ(),
            topLeftBlock.getZ(),
            topRightBlock.getZ());
    this.boundingBox =
        new BoundingBox(
            xList.stream().mapToInt(v -> v).min().orElse(0),
            yList.stream().mapToInt(v -> v).min().orElse(0),
            zList.stream().mapToInt(v -> v).min().orElse(0),
            xList.stream().mapToInt(v -> v).max().orElse(0),
            yList.stream().mapToInt(v -> v).max().orElse(0),
            zList.stream().mapToInt(v -> v).max().orElse(0));
    this.innerBoundingBox =
        new BoundingBox(
            this.boundingBox.getMinX() + 1,
            this.boundingBox.getMinY(),
            this.boundingBox.getMinZ() + 1,
            this.boundingBox.getMaxX() - 1,
            this.boundingBox.getMaxY(),
            this.boundingBox.getMaxZ() - 1);
    this.id =
        String.format(
            "SuperMiningMachine_%s_%s_%s_%s_%s_%s",
            this.boundingBox.getMin().getBlockX(),
            this.boundingBox.getMin().getBlockY(),
            this.boundingBox.getMin().getBlockZ(),
            this.boundingBox.getMax().getBlockX(),
            this.boundingBox.getMax().getBlockY(),
            this.boundingBox.getMax().getBlockZ());
    this.randomGlassList =
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
    this.expLevelRequired =
        (int) Math.floor(this.boundingBox.getWidthX() + this.boundingBox.getWidthZ() - 2);
    this.lastBlockVector =
        new BlockVector(
            (int) innerBoundingBox.getMinX(),
            (int) innerBoundingBox.getMaxY() - 1,
            (int) innerBoundingBox.getMinZ());
  }

  public final BoundingBox boundingBox;
  public final BoundingBox innerBoundingBox;
  public final HashSet<BlockVector> blockVectors;
  public final Block bottomLeftBlock;
  public final Block bottomRightBlock;
  public final Block topLeftBlock;
  public final Block topRightBlock;

  private final String id;
  private final StlmppPlugin plugin;
  private final int expLevelRequired;
  private final List<DoubleChest> chests = new ArrayList<>();
  private final RandomList<Material> randomGlassList;
  private final double maxBoost = 0.95;

  private double boost = 0;
  private boolean isRunning = false;
  @Nullable private BlockVector lastBlockVector;

  @Nullable private BukkitTask lastTask;

  public String getId() {
    return this.id;
  }

  public boolean getIsRunning() {
    return this.isRunning;
  }

  public int getExpLevelRequired() {
    return this.expLevelRequired;
  }

  public double getBoost() {
    return this.boost;
  }

  public boolean hasMaxBoost() {
    return this.boost == this.maxBoost;
  }

  public void addBoost() {
    var boost = 0.1;
    if (this.boost > 0.4 && this.boost < 0.7) {
      boost *= 0.5;
    } else if (this.boost >= 0.7) {
      boost *= 0.2;
    }
    this.boost = Math.min(this.boost + boost, this.maxBoost);
    this.plugin.log(String.format("Machine %s has been boosted to %s", id, boost));
  }

  public boolean hasNetheriteBlock(@NotNull Block block) {
    return this.bottomLeftBlock
            .getLocation()
            .toVector()
            .toBlockVector()
            .equals(block.getLocation().toVector().toBlockVector())
        || this.bottomRightBlock
            .getLocation()
            .toVector()
            .toBlockVector()
            .equals(block.getLocation().toVector().toBlockVector())
        || this.topLeftBlock
            .getLocation()
            .toVector()
            .toBlockVector()
            .equals(block.getLocation().toVector().toBlockVector())
        || this.topRightBlock
            .getLocation()
            .toVector()
            .toBlockVector()
            .equals(block.getLocation().toVector().toBlockVector());
  }

  public boolean hasBlock(@NotNull Block block) {
    return this.blockVectors.contains(block.getLocation().toVector().toBlockVector());
  }

  public void start() {
    if (this.isRunning) {
      this.plugin.log(String.format("Machine %s is already running", this.id), true);
      return;
    }
    this.plugin.log(String.format("Starting machine %s", this.id), true);
    this.isRunning = true;
    this.scheduleNext();
  }

  private void increaseLastBlockVector() {
    assert lastBlockVector != null;
    int x = lastBlockVector.getBlockX();
    int y = lastBlockVector.getBlockY();
    int z = lastBlockVector.getBlockZ();

    // Move to the next x-coordinate
    x++;
    // If x exceeds maxX, reset x and increment z
    if (x > innerBoundingBox.getMaxX()) {
      x = (int) innerBoundingBox.getMinX();
      z++;
      // If z exceeds maxZ, reset z and decrement y
      if (z > innerBoundingBox.getMaxZ()) {
        z = (int) innerBoundingBox.getMinZ();
        y--;
        // If y goes below minY, no more blocks to mine
        // TODO this needs to be dynamic because of the nether
        if (y < -63) {
          this.lastBlockVector = null;
          return;
        }
      }
    }
    this.lastBlockVector.setY(y);
    this.lastBlockVector.setX(x);
    this.lastBlockVector.setZ(z);
  }

  @Nullable
  private Block getNextBlock() {
    Block block;
    do {
      if (this.lastBlockVector == null) {
        return null;
      }
      block = this.lastBlockVector.toLocation(this.bottomLeftBlock.getWorld()).getBlock();
      this.increaseLastBlockVector();
    } while (!block.getType().isSolid()
        || !block.getType().isBlock()
        || block.getType().equals(Material.BEDROCK));
    return block;
  }

  private void scheduleNext() {
    final var block = this.getNextBlock();
    if (block == null) {
      this.lastTask = null;
      this.stop();
      return;
    }
    var seconds = SuperMiningMachineBlockDelay.get(block.getType());
    if (this.boost > 0.0) {
      seconds -= seconds * this.boost;
    }
    plugin.log(
        String.format(
            "breaking block at %s %s %s in %s seconds",
            block.getX(), block.getY(), block.getZ(), seconds),
        true);
    this.lastTask =
        new BukkitRunnable() {
          @Override
          public void run() {
            // TODO delete dropped items somehow
            // TODO play sound of block breaking
            // TODO spawn particle
            // TODO play sound around machine
            // TODO check if block is special: water, lava, chest, spawner, etc, and put it in the
            // chest
            // TODO instead of breaking block naturally, maybe replace it with AIR
            // TODO check walls of mining area for ores and mine them
            final var pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
            pickaxe.addEnchantment(Enchantment.FORTUNE, 3);
            Collection<ItemStack> drops = new ArrayList<>();
            if (Util.isOre(block)) {
              final var allOres = Util.getBlocksAround(block);
              for (Block oreBlock : allOres) {
                drops.addAll(oreBlock.getDrops(pickaxe));
                oreBlock.breakNaturally();
              }
            } else {
              drops.addAll(block.getDrops(pickaxe));
              block.breakNaturally();
            }
            addItemsToChest(drops);
            scheduleNext();
          }
        }.runTaskLater(this.plugin, Tick.fromSeconds(seconds));
  }

  private DoubleChest createNewChest() {
    final var location =
        this.chests.isEmpty()
            ? bottomLeftBlock
                .getLocation()
                .getBlock()
                .getRelative(BlockFace.SOUTH)
                .getLocation()
                .clone()
            : this.chests
                .getLast()
                .getLocation()
                .getBlock()
                .getRelative(BlockFace.UP)
                .getLocation()
                .clone();
    this.plugin.log(
        String.format(
            "Creating new chest at %s %s %s",
            location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    final var chest1 = location.getBlock();
    final var chest2 = chest1.getRelative(BlockFace.EAST);
    chest1.setType(Material.CHEST);
    chest2.setType(Material.CHEST);

    final var chest1Data = (Chest) chest1.getBlockData();
    final var chest2Data = (Chest) chest2.getBlockData();

    chest1Data.setFacing(BlockFace.SOUTH);
    chest2Data.setFacing(BlockFace.SOUTH);
    chest1Data.setType(Chest.Type.RIGHT);
    chest2Data.setType(Chest.Type.LEFT);

    chest1.setBlockData(chest1Data);
    chest2.setBlockData(chest2Data);

    final var state = (org.bukkit.block.Chest) chest1.getState();

    return (DoubleChest) state.getInventory().getHolder();
  }

  private void addItemsToChest(@NotNull Collection<ItemStack> items) {
    this.plugin.log(String.format("Adding items (%s) to chest", items.size()), true);
    for (final var item : items) {
      int chestIndex = 0;
      HashMap<Integer, ItemStack> exceededItems;
      do {
        if (chestIndex >= this.chests.size()) {
          this.chests.add(this.createNewChest());
        }
        final var chest = this.chests.get(chestIndex);
        exceededItems = chest.getInventory().addItem(item);
        if (!exceededItems.isEmpty()) {
          chestIndex++;
        } else {
          chestIndex = 0;
        }
      } while (!exceededItems.isEmpty());
    }
  }

  public void stop() {
    if (this.lastTask != null) {
      this.lastTask.cancel();
    }
    this.isRunning = false;
  }

  public void construct() {
    // TODO figure out a better construction
    // TODO play sound of construction
    // TODO add sign with experience level required and id
    final var glassY = (int) innerBoundingBox.getMaxY();
    for (var x = (int) innerBoundingBox.getMinX(); x <= innerBoundingBox.getMaxX(); x++) {
      for (var z = (int) innerBoundingBox.getMinZ(); z <= innerBoundingBox.getMaxZ(); z++) {
        final var block = bottomLeftBlock.getWorld().getBlockAt(x, glassY, z);
        block.setType(randomGlassList.next());
        this.blockVectors.add(block.getLocation().toVector().toBlockVector());
      }
    }
  }

  @Override
  public String toString() {
    return String.format(
        "SuperMiningMachine - blocks.size = %s | bottomLeft = %s | bottomRight = %s "
            + "| topLeft = %s | topRight = %s | boundingBox = %s | innerBoundingBox = %s",
        this.blockVectors.size(),
        this.bottomLeftBlock,
        this.bottomRightBlock,
        this.topLeftBlock,
        this.topRightBlock,
        this.boundingBox,
        this.innerBoundingBox);
  }
}
