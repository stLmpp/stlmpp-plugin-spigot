package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMBlockEntity;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMEntity;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMStateUpdateDto;
import com.stlmpp.spigot.plugins.utils.BlockHashSet;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
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
import org.jetbrains.annotations.Contract;
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
    this.bottomLeftBlock = bottomLeftBlock;
    this.bottomRightBlock = bottomRightBlock;
    this.topLeftBlock = topLeftBlock;
    this.topRightBlock = topRightBlock;
    corners =
        new BlockHashSet(
            getWorld(), List.of(bottomLeftBlock, bottomRightBlock, topLeftBlock, topRightBlock));
    this.blocks = new BlockHashSet(getWorld(), blocks);
    final var xList = corners.stream().map(Block::getX).toList();
    final var yList = corners.stream().map(Block::getY).toList();
    final var zList = corners.stream().map(Block::getZ).toList();
    boundingBox =
        new BoundingBox(
            xList.stream().mapToInt(v -> v).min().orElse(0),
            yList.stream().mapToInt(v -> v).min().orElse(0),
            zList.stream().mapToInt(v -> v).min().orElse(0),
            xList.stream().mapToInt(v -> v).max().orElse(0),
            yList.stream().mapToInt(v -> v).max().orElse(0),
            zList.stream().mapToInt(v -> v).max().orElse(0));
    innerBoundingBox =
        new BoundingBox(
            boundingBox.getMinX() + 1,
            boundingBox.getMinY(),
            boundingBox.getMinZ() + 1,
            boundingBox.getMaxX() - 1,
            boundingBox.getMaxY(),
            boundingBox.getMaxZ() - 1);
    id =
        String.format(
            "SMM_%s_%s_%s_%s_%s_%s",
            boundingBox.getMin().getBlockX(),
            boundingBox.getMin().getBlockY(),
            boundingBox.getMin().getBlockZ(),
            boundingBox.getMax().getBlockX(),
            boundingBox.getMax().getBlockY(),
            boundingBox.getMax().getBlockZ());
    size = (int) Math.floor(boundingBox.getWidthX() + boundingBox.getWidthZ() - 2);
    expLevelRequired = size;
    lastBlockBroken = getInitialBlockVector();
    minY = getWorld().getName().equals(plugin.getWorldName()) ? -63 : 1;
  }

  private SuperMiningMachine(
      StlmppPlugin plugin,
      @NotNull List<Block> blocks,
      @NotNull Block bottomLeftBlock,
      @NotNull Block bottomRightBlock,
      @NotNull Block topLeftBlock,
      @NotNull Block topRightBlock,
      @NotNull List<DoubleChest> chests,
      @NotNull SMMEntity entity) {
    this(plugin, blocks, bottomLeftBlock, bottomRightBlock, topLeftBlock, topRightBlock);
    boostFactor = entity.boostFactor();
    boostTimes = entity.boostTimes();
    if (entity.lastBlock() != null) {
      lastBlockBroken = deserializeLocation(entity.lastBlock());
    }
    this.chests.addAll(chests);
    hasFinished = entity.hasFinished() == 1;
    if (entity.isRunning() == 1 && !hasFinished) {
      onEnable();
    }
  }

  public final BoundingBox boundingBox;
  public final BoundingBox innerBoundingBox;

  private final Block bottomLeftBlock;
  private final Block bottomRightBlock;
  private final Block topLeftBlock;
  private final Block topRightBlock;
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
  private boolean hasFinished = false;

  @Nullable private BlockVector lastBlockBroken;
  @Nullable private BukkitTask lastTask;
  @Nullable private BukkitTask smokeTask;

  public World getWorld() {
    return bottomLeftBlock.getWorld();
  }

  public BlockHashSet getCorners() {
    return corners;
  }

  public BlockHashSet getBlocks() {
    return blocks;
  }

  public String getId() {
    return id;
  }

  public boolean getIsRunning() {
    return isRunning;
  }

  public boolean getHasFinished() {
    return hasFinished;
  }

  public int getExpLevelRequired() {
    return expLevelRequired;
  }

  public double getBoostFactor() {
    return boostFactor;
  }

  public boolean hasMaxBoost() {
    return boostFactor == maxBoost;
  }

  public void addBoost() {
    if (hasMaxBoost()) {
      return;
    }
    playSound(Sound.BLOCK_BEACON_POWER_SELECT);
    boostTimes++;
    boostFactor = Math.min(boostFactor + (0.25 / boostTimes), maxBoost);
    restartSmoke();
    plugin.log(String.format("Machine %s has been boosted to %s", id, boostFactor));
    updateDatabaseState();
  }

  public boolean hasNetheriteBlock(@NotNull Block block) {
    return corners.contains(block);
  }

  public boolean hasBlock(@NotNull Block block) {
    return blocks.contains(block);
  }

  public boolean hasChest(@NotNull Block block) {
    return block.getState() instanceof org.bukkit.block.Chest chest
        && chest.getInventory().getHolder() instanceof DoubleChest doubleChest
        && doubleChest.getLeftSide() != null
        && doubleChest.getLeftSide().getInventory().getLocation() != null
        && chests.stream()
            .anyMatch(
                smmChest ->
                    smmChest.getLeftSide() != null
                        && smmChest.getLeftSide().getInventory().getLocation() != null
                        && smmChest
                            .getLeftSide()
                            .getInventory()
                            .getLocation()
                            .toVector()
                            .toBlockVector()
                            .equals(
                                doubleChest
                                    .getLeftSide()
                                    .getInventory()
                                    .getLocation()
                                    .toVector()
                                    .toBlockVector()));
  }

  public void start() {
    if (isRunning) {
      plugin.log(String.format("Machine %s is already running", id), true);
      return;
    }
    onEnable();
  }

  public void stop() {
    onDisable();
    playSound(Sound.BLOCK_BEACON_DEACTIVATE);
    isRunning = false;
    updateDatabaseState();
  }

  public void onEnable() {
    if (hasFinished) {
      plugin.log(String.format("Machine %s is already completed", id), true);
      return;
    }
    playSound(Sound.BLOCK_BEACON_ACTIVATE);
    startSmoke();
    plugin.log(String.format("Starting machine %s", id), true);
    isRunning = true;
    scheduleNext();
  }

  public void onDisable() {
    if (lastTask != null) {
      lastTask.cancel();
    }
    stopSmoke();
    if (hasFinished) {
      lastBlockBroken = getInitialBlockVector();
    }
  }

  private void startSmoke() {
    var seconds = 0.7;
    if (boostFactor > 0) {
      seconds -= seconds * boostFactor;
    }
    smokeTask =
        plugin.runTimer(
            0,
            Tick.fromSeconds(seconds),
            () -> {
              for (var corner : corners) {
                final var random = ThreadLocalRandom.current();
                final var location = corner.getLocation().add(0, 1, 0);
                for (var i = 0; i < random.nextInt(1, 3); i++) {
                  final var spawnLocation =
                      location.add(random.nextDouble(0, 0.99), 0, random.nextDouble(0, 0.99));
                  getWorld()
                      .spawnParticle(
                          Particle.CAMPFIRE_SIGNAL_SMOKE, spawnLocation, 0, 0, 0.5, 0, 0.1);
                }
              }
            });
  }

  private void stopSmoke() {
    if (smokeTask != null) {
      smokeTask.cancel();
    }
  }

  private void restartSmoke() {
    stopSmoke();
    startSmoke();
  }

  @Contract(" -> new")
  private @NotNull SMMStateUpdateDto getUpdateDto() {
    return new SMMStateUpdateDto(
        isRunning ? 1 : 0,
        hasFinished ? 1 : 0,
        lastBlockBroken != null ? serializeLocation(lastBlockBroken.toLocation(getWorld())) : null,
        boostTimes,
        boostFactor);
  }

  private BlockVector getInitialBlockVector() {
    return new BlockVector(
        (int) innerBoundingBox.getMinX(),
        (int) innerBoundingBox.getMaxY() - 1,
        (int) innerBoundingBox.getMinZ());
  }

  private void increaseLastBlockVector() {
    assert lastBlockBroken != null;
    int x = lastBlockBroken.getBlockX();
    int y = lastBlockBroken.getBlockY();
    int z = lastBlockBroken.getBlockZ();

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
        if (y < minY) {
          lastBlockBroken = null;
          return;
        }
      }
    }
    lastBlockBroken.setY(y);
    lastBlockBroken.setX(x);
    lastBlockBroken.setZ(z);
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
      if (lastBlockBroken == null) {
        return null;
      }
      block = lastBlockBroken.toLocation(getWorld()).getBlock();
      increaseLastBlockVector();
    } while (!isValidBlockToMine(block));
    return block;
  }

  private void scheduleNext() {
    final var block = getNextBlock();
    if (block == null) {
      lastTask = null;
      hasFinished = true;
      stop();
      plugin.sendMessage(
          String.format(
              "Escavadeira na coordenada %s %s %s terminou a escavacao!",
              bottomLeftBlock.getX(), bottomLeftBlock.getY(), bottomLeftBlock.getZ()));
      return;
    }
    updateDatabaseState();
    var seconds = SMMBlockDelay.get(block.getType());
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
              final var items = blockBreaker.breakAndGetDrops(block);
              if (Chance.of(1)) {
                playSound(Sound.BLOCK_BEACON_AMBIENT);
              }
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
              signSide.line(3, Component.text(getExpLevelRequired()));
              signSide.setGlowingText(true);
              signSide.setColor(DyeColor.WHITE);
            });
    sign.update();
  }

  private void createNewChest() {
    final var location =
        chests.isEmpty()
            ? bottomLeftBlock
                .getLocation()
                .getBlock()
                .getRelative(BlockFace.SOUTH)
                .getLocation()
                .clone()
            : chests
                .getLast()
                .getLocation()
                .getBlock()
                .getRelative(BlockFace.UP)
                .getLocation()
                .clone();
    plugin.log(
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

    chests.add((DoubleChest) state.getInventory().getHolder());

    assert plugin.smmManager != null;
    plugin.smmManager.insertBlock(
        new SMMBlockEntity(null, id, serializeLocation(chest1), SMMBlockEntity.chestType));
  }

  private void addItemsToChest(@NotNull Collection<ItemStack> items) {
    plugin.log(String.format("Adding items (%s) to chest", items.size()), true);
    for (final var item : items) {
      int chestIndex = 0;
      HashMap<Integer, ItemStack> exceededItems;
      do {
        if (chestIndex >= chests.size()) {
          createNewChest();
        }
        final var chest = chests.get(chestIndex);
        exceededItems = chest.getInventory().addItem(item);
        if (!exceededItems.isEmpty()) {
          chestIndex++;
        } else {
          chestIndex = 0;
        }
      } while (!exceededItems.isEmpty());
    }
  }

  public void createBaseStructure() {
    playSound(Sound.BLOCK_ANVIL_USE);
    createSign();
    if (chests.isEmpty()) {
      createNewChest();
      final var leftFloor =
          bottomLeftBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN);
      final var rightFloor = leftFloor.getRelative(BlockFace.EAST);
      Util.setUntilSolid(leftFloor.getLocation(), Material.IRON_BLOCK);
      Util.setUntilSolid(rightFloor.getLocation(), Material.IRON_BLOCK);
    }
    final var glassY = (int) innerBoundingBox.getMaxY();
    for (var x = (int) innerBoundingBox.getMinX(); x <= innerBoundingBox.getMaxX(); x++) {
      for (var z = (int) innerBoundingBox.getMinZ(); z <= innerBoundingBox.getMaxZ(); z++) {
        final var block = getWorld().getBlockAt(x, glassY, z);
        block.setType(Util.randomGlassList.next());
      }
    }
    for (var block : blocks) {
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
        blocks.size(),
        bottomLeftBlock,
        bottomRightBlock,
        topLeftBlock,
        topRightBlock,
        boundingBox,
        innerBoundingBox);
  }

  public float getExplosionPower() {
    return (float) (size + (boostTimes * 0.30));
  }

  private float getSoundVolume() {
    return (float) size / 4;
  }

  private void playSound(Sound sound, Location location) {
    getWorld().playSound(location, sound, getSoundVolume(), 1);
  }

  private void playSound(Sound sound) {
    final var location = boundingBox.getCenter().toLocation(getWorld());
    playSound(sound, location);
  }

  public void explode(long delay) {
    if (delay <= 0) {
      delay = 4;
    }
    final var soundCount = new AtomicInteger(0);
    final long playSoundUntil = delay - 1;
    var countdownTask =
        new AtomicReference<>(
            plugin.runTimer(
                0,
                Tick.fromSeconds(1),
                () -> {
                  if (soundCount.get() > playSoundUntil) {
                    return;
                  }
                  if (soundCount.get() < playSoundUntil) {
                    playSound(Sound.ENTITY_CREEPER_HURT);
                  } else {
                    playSound(Sound.ENTITY_CREEPER_PRIMED);
                  }
                  soundCount.incrementAndGet();
                }));
    var blockNumber = 0;
    for (Block block : getCorners()) {
      blockNumber++;
      final var explosionLocation = block.getLocation().subtract(0, 1, 0);
      final var isFinalBlock = blockNumber == getCorners().size();
      plugin.runLater(
          Tick.fromSeconds(blockNumber + delay),
          () -> {
            if (countdownTask.get() != null) {
              countdownTask.get().cancel();
              countdownTask.set(null);
            }
            explosionLocation.getBlock().setType(Material.AIR);
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
              assert plugin.smmManager != null;
              if (!plugin.smmManager.isBlockTypeValid(blockToBreak.getType())) {
                continue;
              }
              plugin.runLater(
                  Tick.fromSeconds(++obsidianNumber / 10), blockToBreak::breakNaturally);
            }
          });
    }
  }

  public SMMEntity serialize() {
    final var blocks =
        new ArrayList<>(
            getBlocks().stream()
                .map(
                    block ->
                        new SMMBlockEntity(
                            null, id, serializeLocation(block), SMMBlockEntity.blockType))
                .toList());
    blocks.addAll(
        chests.stream()
            .map(
                chest -> {
                  if (chest.getLeftSide() == null
                      || chest.getLeftSide().getInventory().getLocation() == null) {
                    return null;
                  }
                  return new SMMBlockEntity(
                      null,
                      id,
                      serializeLocation(chest.getLeftSide().getInventory().getLocation()),
                      SMMBlockEntity.chestType);
                })
            .filter(Objects::nonNull)
            .toList());
    return new SMMEntity(
        id,
        getWorld().getName(),
        serializeLocation(bottomLeftBlock),
        serializeLocation(bottomRightBlock),
        serializeLocation(topLeftBlock),
        serializeLocation(topRightBlock),
        lastBlockBroken != null ? serializeLocation(lastBlockBroken) : null,
        boostFactor,
        boostTimes,
        isRunning ? 1 : 0,
        hasFinished ? 1 : 0,
        blocks);
  }

  @NotNull
  public static String serializeLocation(@NotNull BlockVector vector) {
    return vector.getBlockX() + "," + vector.getBlockY() + "," + vector.getBlockZ();
  }

  @NotNull
  public static String serializeLocation(@NotNull Block block) {
    return serializeLocation(block.getLocation().toVector().toBlockVector());
  }

  @NotNull
  public static String serializeLocation(@NotNull Location location) {
    return serializeLocation(location.toVector().toBlockVector());
  }

  public static BlockVector deserializeLocation(@NotNull String location) {
    final var coordinates = location.split(",");
    final var x = Integer.parseInt(coordinates[0]);
    final var y = Integer.parseInt(coordinates[1]);
    final var z = Integer.parseInt(coordinates[2]);
    return new BlockVector(x, y, z);
  }

  @Nullable
  public static SuperMiningMachine deserialize(
      @NotNull StlmppPlugin plugin, @NotNull SMMEntity entity) {
    final var world = plugin.getServer().getWorld(entity.world());
    if (world == null) {
      plugin.log(String.format("World = %s not found", entity.world()), true);
      return null;
    }
    final var bottomLeft = deserializeLocation(entity.bottomLeft()).toLocation(world).getBlock();
    final var bottomRight = deserializeLocation(entity.bottomRight()).toLocation(world).getBlock();
    final var topLeft = deserializeLocation(entity.topLeft()).toLocation(world).getBlock();
    final var topRight = deserializeLocation(entity.topRight()).toLocation(world).getBlock();
    final var blocks = new ArrayList<Block>();
    final var chests = new ArrayList<DoubleChest>();
    for (final var blockEntity : entity.blocks()) {
      final var block = deserializeLocation(blockEntity.location()).toLocation(world).getBlock();
      switch (blockEntity.type()) {
        case SMMBlockEntity.blockType:
          {
            blocks.add(block);
            break;
          }
        case SMMBlockEntity.chestType:
          {
            if (!(block.getState() instanceof org.bukkit.block.Chest state)
                || !(state.getInventory().getHolder() instanceof DoubleChest doubleChest)) {
              continue;
            }
            chests.add(doubleChest);
            break;
          }
      }
    }
    return new SuperMiningMachine(
        plugin, blocks, bottomLeft, bottomRight, topLeft, topRight, chests, entity);
  }

  public void updateDatabaseState() {
    assert plugin.smmManager != null;
    plugin.smmManager.updateState(id, getUpdateDto());
  }
}
