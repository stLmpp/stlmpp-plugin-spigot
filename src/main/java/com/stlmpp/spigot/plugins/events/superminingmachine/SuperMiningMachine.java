package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.RandomList;
import com.stlmpp.spigot.plugins.utils.Tick;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class SuperMiningMachine {

  public SuperMiningMachine(
      StlmppPlugin plugin,
      List<Block> blocks,
      Block bottomLeftBlock,
      Block bottomRightBlock,
      Block topLeftBlock,
      Block topRightBlock) {
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
            "SuperMiningMachine-%s-%s-%s-%s-%s-%s",
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
  }

  private final String id;
  public final BoundingBox boundingBox;
  public final BoundingBox innerBoundingBox;
  public final HashSet<BlockVector> blockVectors;
  public final Block bottomLeftBlock;
  public final Block bottomRightBlock;
  public final Block topLeftBlock;
  public final Block topRightBlock;
  private final StlmppPlugin plugin;
  private boolean isRunning = false;
  private final List<DoubleChest> chests = new ArrayList<>();

  private final RandomList<Material> randomGlassList;

  @Nullable private BukkitTask lastTask;

  public String getId() {
    return this.id;
  }

  public boolean getIsRunning() {
    return this.isRunning;
  }

  public boolean hasNetheriteBlock(Block block) {
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

  public boolean hasBlock(Block block) {
    return this.blockVectors.contains(block.getLocation().toVector().toBlockVector());
  }

  public void start() {
    if (this.isRunning) {
      this.plugin.log(String.format("Machine %s is already running", this.id), true);
      return;
    }
    this.plugin.log(String.format("Starting machine %s", this.id), true);
    this.isRunning = true;
    final var blockQueue = new ArrayList<Vector3d>();
    for (var y = -63; y < innerBoundingBox.getMaxY(); y++) {
      for (var x = (int) innerBoundingBox.getMinX(); x <= innerBoundingBox.getMaxX(); x++) {
        for (var z = (int) innerBoundingBox.getMinZ(); z <= innerBoundingBox.getMaxZ(); z++) {
          final var block = bottomLeftBlock.getWorld().getBlockAt(x, y, z);
          if (!block.getType().isSolid()
              || !block.getType().isBlock()
              || !block.getType().equals(Material.BEDROCK)) {
            continue;
          }
          this.plugin.log(String.format("adding block at %s %s %s", x, y, z), true);
          blockQueue.add(new Vector3d(x, y, z));
        }
      }
    }
    final var glassY = (int) innerBoundingBox.getMaxY();
    for (var x = (int) innerBoundingBox.getMinX(); x <= innerBoundingBox.getMaxX(); x++) {
      for (var z = (int) innerBoundingBox.getMinZ(); z <= innerBoundingBox.getMaxZ(); z++) {
        final var block = bottomLeftBlock.getWorld().getBlockAt(x, glassY, z);
        block.setType(randomGlassList.next());
      }
    }
    this.scheduleNext(blockQueue);
  }

  private void scheduleNext(ArrayList<Vector3d> queue) {
    if (queue.isEmpty()) {
      this.lastTask = null;
      this.stop();
      return;
    }
    final var vec = queue.removeLast();
    final var block = bottomLeftBlock.getWorld().getBlockAt((int) vec.x, (int) vec.y, (int) vec.z);
    plugin.log(String.format("breaking block at %s %s %s", vec.x, vec.y, vec.z), true);
    this.lastTask =
        new BukkitRunnable() {
          @Override
          public void run() {
            block.breakNaturally();
            // TODO delete dropped items somehow
            // TODO play sound of block breaking
            // TODO spawn particle
            // TODO play sound around machine
            scheduleNext(queue);
          }
        }.runTaskLater(
            this.plugin, Tick.fromSeconds(SuperMiningMachineBlockDelay.get(block.getType())));
  }

  private DoubleChest createNewChest() {
    // TODO
  }

  private void addItemToChest() {
    // TODO
  }

  public void stop() {
    if (this.lastTask != null) {
      this.lastTask.cancel();
    }
    this.isRunning = false;
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
