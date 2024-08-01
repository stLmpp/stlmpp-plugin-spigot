package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
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
    this.bottomLeftBlock = bottomLeftBlock;
    this.bottomRightBlock = bottomRightBlock;
    this.topLeftBlock = topLeftBlock;
    this.topRightBlock = topRightBlock;
    this.corners =
        new BlockHashSet(
            getWorld(), List.of(bottomLeftBlock, bottomRightBlock, topLeftBlock, topRightBlock));
    this.blocks = new BlockHashSet(getWorld(), blocks);
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
    this.size = (int) Math.floor(this.boundingBox.getWidthX() + this.boundingBox.getWidthZ() - 2);
    this.expLevelRequired = size;
    this.lastBlockVector = this.getInitialBlockVector();
    this.minY = getWorld().getName().equals(this.plugin.getWorldName()) ? -63 : 1;
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

  @Nullable private BlockVector signLocation = null;
  @Nullable private BlockVector lastBlockVector;
  @Nullable private BukkitTask lastTask;
  @Nullable private BukkitTask smokeTask;

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
    this.restartSmoke();
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
    this.startSmoke();
    this.plugin.log(String.format("Starting machine %s", this.id), true);
    this.isRunning = true;
    this.scheduleNext();
  }

  public void stop() {
    if (this.lastTask != null) {
      this.lastTask.cancel();
    }
    this.stopSmoke();
    this.playSound(Sound.BLOCK_BEACON_DEACTIVATE);
    this.lastBlockVector = this.getInitialBlockVector();
    this.isRunning = false;
  }

  private void startSmoke() {
    var seconds = 0.7;
    if (boostFactor > 0) {
      seconds -= seconds * boostFactor;
    }
    this.smokeTask =
        new BukkitRunnable() {
          @Override
          public void run() {
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
          }
        }.runTaskTimerAsynchronously(plugin, 0, Tick.fromSeconds(seconds));
  }

  private void stopSmoke() {
    if (this.smokeTask != null) {
      this.smokeTask.cancel();
    }
  }

  private void restartSmoke() {
    this.stopSmoke();
    this.startSmoke();
  }

  private BlockVector getInitialBlockVector() {
    return new BlockVector(
        (int) innerBoundingBox.getMinX(),
        (int) innerBoundingBox.getMaxY() - 1,
        (int) innerBoundingBox.getMinZ());
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
      if (lastBlockVector == null) {
        return null;
      }
      block = lastBlockVector.toLocation(getWorld()).getBlock();
      increaseLastBlockVector();
    } while (!isValidBlockToMine(block));
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
              final var items = this.blockBreaker.breakAndGetDrops(block);
              this.playSound(Sound.ENTITY_IRON_GOLEM_STEP);
              this.playSound(Sound.ENTITY_IRON_GOLEM_STEP, block.getLocation());
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
        final var block = getWorld().getBlockAt(x, glassY, z);
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

  private void playSound(Sound sound, Location location) {
    this.plugin.log(
        String.format(
            "Playing sound %s at %s %s %s with volume = %s",
            sound,
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ(),
            this.getSoundVolume()));
    this.getWorld().playSound(location, sound, this.getSoundVolume(), 1);
  }

  private void playSound(Sound sound) {
    final var location = this.boundingBox.getCenter().toLocation(this.getWorld());
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
