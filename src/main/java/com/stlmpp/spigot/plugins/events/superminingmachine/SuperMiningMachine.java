package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.BlockHashSet;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.*;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.ItemStack;
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
    this.corners =
        new BlockHashSet(
            bottomLeftBlock.getWorld(),
            List.of(bottomLeftBlock, bottomRightBlock, topLeftBlock, topRightBlock));
    this.blocks = new BlockHashSet(bottomLeftBlock.getWorld(), blocks);
    this.bottomLeftBlock = bottomLeftBlock;
    this.bottomRightBlock = bottomRightBlock;
    this.topLeftBlock = topLeftBlock;
    this.topRightBlock = topRightBlock;
    final var xList = this.corners.stream().map(Block::getX).toList();
    final var yList = this.corners.stream().map(Block::getY).toList();
    final var zList = this.corners.stream().map(Block::getZ).toList();
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
            "SMM_%s_%s_%s_%s_%s_%s",
            this.boundingBox.getMin().getBlockX(),
            this.boundingBox.getMin().getBlockY(),
            this.boundingBox.getMin().getBlockZ(),
            this.boundingBox.getMax().getBlockX(),
            this.boundingBox.getMax().getBlockY(),
            this.boundingBox.getMax().getBlockZ());
    this.expLevelRequired =
        (int) Math.floor(this.boundingBox.getWidthX() + this.boundingBox.getWidthZ() - 2);
    this.lastBlockVector =
        new BlockVector(
            (int) innerBoundingBox.getMinX(),
            (int) innerBoundingBox.getMaxY() - 1,
            (int) innerBoundingBox.getMinZ());
    this.minY = bottomLeftBlock.getWorld().getName().equals(this.plugin.getWorldName()) ? -63 : 1;
    this.size =
        (int) (Math.max(this.innerBoundingBox.getWidthX(), this.innerBoundingBox.getWidthZ()) - 2);
  }

  // TODO make all this fields private and provide methods
  public final BoundingBox boundingBox;
  public final BoundingBox innerBoundingBox;
  public final Block bottomLeftBlock;
  public final Block bottomRightBlock;
  public final Block topLeftBlock;
  public final Block topRightBlock;

  private final SMMBlockBreaker blockBreaker = new SMMBlockBreaker(this);
  private final BlockHashSet blocks;
  private final int minY;
  private final String id;
  private final StlmppPlugin plugin;
  private final int expLevelRequired;
  private final List<DoubleChest> chests = new ArrayList<>();
  private final double maxBoost = 0.85;
  private final BlockHashSet corners;
  private final int size;

  private double boostFactor = 0;
  private int boostTimes = 0;
  private boolean isRunning = false;

  @Nullable private BlockVector signLocation = null;
  @Nullable private BlockVector lastBlockVector;
  @Nullable private BukkitTask lastTask;

  public World getWorld() {
    return bottomLeftBlock.getWorld();
  }

  public BlockHashSet getCorners() {
    return this.corners;
  }

  public BlockHashSet getBlocks() {
    return this.blocks;
  }

  public String getId() {
    return this.id;
  }

  public boolean getIsRunning() {
    return this.isRunning;
  }

  public int getExpLevelRequired() {
    return this.expLevelRequired;
  }

  public double getBoostFactor() {
    return this.boostFactor;
  }

  public boolean hasMaxBoost() {
    return this.boostFactor == this.maxBoost;
  }

  public void addBoost() {
    if (this.hasMaxBoost()) {
      return;
    }
    this.playSound(Sound.BLOCK_BEACON_POWER_SELECT);
    this.boostTimes++;
    this.boostFactor = Math.min(this.boostFactor + (0.25 / this.boostTimes), this.maxBoost);
    this.plugin.log(String.format("Machine %s has been boosted to %s", id, this.boostFactor));
  }

  public boolean hasNetheriteBlock(@NotNull Block block) {
    return this.corners.contains(block);
  }

  public boolean hasBlock(@NotNull Block block) {
    return this.blocks.contains(block);
  }

  public void start() {
    if (this.isRunning) {
      this.plugin.log(String.format("Machine %s is already running", this.id), true);
      return;
    }
    this.playSound(Sound.BLOCK_BEACON_ACTIVATE);
    // TODO add some effects or particles
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
        if (y < this.minY) {
          this.lastBlockVector = null;
          return;
        }
      }
    }
    this.lastBlockVector.setY(y);
    this.lastBlockVector.setX(x);
    this.lastBlockVector.setZ(z);
  }

  private boolean isValidBlockToMine(@NotNull Block block) {
    final var type = block.getType();
    return (type.isBlock() && block.isSolid() && !type.equals(Material.BEDROCK))
        || type.equals(Material.CHEST);
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
    } while (!this.isValidBlockToMine(block));
    return block;
  }

  private void scheduleNext() {
    final var block = this.getNextBlock();
    if (block == null) {
      this.lastTask = null;
      this.stop();
      this.plugin.sendMessage(
          String.format(
              "Escavadeira na coordenada %s %s %s terminou a escavacao!",
              bottomLeftBlock.getX(), bottomLeftBlock.getY(), bottomLeftBlock.getZ()));
      return;
    }
    var seconds = SuperMiningMachineBlockDelay.get(block.getType());
    if (boostFactor > 0.0) {
      seconds -= seconds * boostFactor;
    }
    plugin.log(
        String.format(
            "breaking block at %s %s %s in %s seconds",
            block.getX(), block.getY(), block.getZ(), seconds),
        true);
    lastTask =
        plugin.runLater(
            Tick.fromSeconds(seconds),
            () -> {
              // TODO spawn particle
              final var items = this.blockBreaker.breakAndGetDrops(block);
              this.playSound(Sound.ENTITY_IRON_GOLEM_STEP);
              addItemsToChest(items);
              scheduleNext();
            });
  }

  private void createSign() {
    final var location =
        bottomLeftBlock
            .getLocation()
            .getBlock()
            .getRelative(BlockFace.SOUTH)
            .getRelative(BlockFace.EAST, 3)
            .getLocation()
            .clone();
    this.signLocation = location.toVector().toBlockVector();
    final var block = location.getBlock();
    block.setType(Material.DARK_OAK_SIGN);
    Util.setUntilSolid(location.subtract(0, 1, 0), Material.IRON_BLOCK);
    final var state = block.getState();
    if (!(state instanceof Sign sign)) {
      return;
    }
    Stream.of(Side.FRONT, Side.BACK)
        .forEach(
            side -> {
              final var signSide = sign.getSide(side);
              signSide.line(0, Component.text("Levels de"));
              signSide.line(1, Component.text("experiencia"));
              signSide.line(2, Component.text("necessario:"));
              signSide.line(3, Component.text(this.getExpLevelRequired()));
              signSide.setGlowingText(true);
              signSide.setColor(DyeColor.WHITE);
            });
    sign.update();
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
    this.playSound(Sound.BLOCK_BEACON_DEACTIVATE);
    this.isRunning = false;
  }

  public void createBaseStructure() {
    this.playSound(Sound.BLOCK_ANVIL_USE);
    this.createSign();
    if (this.chests.isEmpty()) {
      this.createNewChest();
      final var leftFloor =
          bottomLeftBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN);
      final var rightFloor = leftFloor.getRelative(BlockFace.EAST);
      Util.setUntilSolid(leftFloor.getLocation(), Material.IRON_BLOCK);
      Util.setUntilSolid(rightFloor.getLocation(), Material.IRON_BLOCK);
    }
    final var glassY = (int) innerBoundingBox.getMaxY();
    for (var x = (int) innerBoundingBox.getMinX(); x <= innerBoundingBox.getMaxX(); x++) {
      for (var z = (int) innerBoundingBox.getMinZ(); z <= innerBoundingBox.getMaxZ(); z++) {
        final var block = bottomLeftBlock.getWorld().getBlockAt(x, glassY, z);
        block.setType(Util.randomGlassList.next());
      }
    }
    for (var block : this.blocks) {
      final var location = block.getLocation();
      final var isNetherite = location.getBlock().getType().equals(Material.NETHERITE_BLOCK);
      final var blockFloorY = location.getBlockY() - 1;
      Util.setUntilSolid(
          location.subtract(0, 1, 0),
          (y, isNextSolid) -> {
            if (y == blockFloorY || isNextSolid) {
              return Material.IRON_BLOCK;
            }
            return isNetherite ? Material.IRON_BLOCK : Util.randomGlassList.next();
          });
    }
  }

  @Override
  public String toString() {
    return String.format(
        "SuperMiningMachine - blocks.size = %s | bottomLeft = %s | bottomRight = %s "
            + "| topLeft = %s | topRight = %s | boundingBox = %s | innerBoundingBox = %s",
        this.blocks.size(),
        this.bottomLeftBlock,
        this.bottomRightBlock,
        this.topLeftBlock,
        this.topRightBlock,
        this.boundingBox,
        this.innerBoundingBox);
  }

  public HashMap<String, Object> serialize() {
    final HashMap<String, Object> map = new HashMap<>();
    map.put("is_running", this.isRunning);
    map.put("chests", this.chests); // TODO
    map.put("blocks", this.blocks); // TODO;
    map.put("sign_location", this.signLocation); // TODO
    // TODO complete
    return map;
  }

  public float getExplosionPower() {
    return (float) (this.size + (this.boostTimes * 0.30));
  }

  private float getSoundVolume() {
    return (float) this.size / 4;
  }

  private void playSound(Sound sound) {
    this.getWorld()
        .playSound(
            this.boundingBox.getCenter().toLocation(this.getWorld()),
            sound,
            this.getSoundVolume(),
            1);
  }

  public void explode(long delay) {
    var blockNumber = 0;
    for (Block block : getCorners()) {
      blockNumber++;
      final var explosionLocation = block.getLocation().subtract(0, 1, 0);
      final var isFinalBlock = blockNumber == getCorners().size();
      plugin.runLater(
          Tick.fromSeconds(blockNumber + delay),
          () -> {
            explosionLocation
                .getBlock()
                .getWorld()
                .createExplosion(explosionLocation, getExplosionPower(), true, true);
            if (Chance.of(50)) {
              block.setType(Material.LAVA);
            }
            if (!isFinalBlock) {
              return;
            }
            var obsidianNumber = 0;
            for (Block blockToBreak : getBlocks()) {
              assert plugin.superMiningMachineManager != null;
              if (!plugin.superMiningMachineManager.isBlockTypeValid(blockToBreak.getType())) {
                continue;
              }
              plugin.runLater(
                  Tick.fromSeconds(++obsidianNumber / 10), blockToBreak::breakNaturally);
            }
          });
    }
  }
}
