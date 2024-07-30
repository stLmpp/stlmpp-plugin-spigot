package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Pair;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SuperMiningMachineCreationEvent implements Listener {

  public static SuperMiningMachineCreationEvent register(@NotNull StlmppPlugin plugin) {
    return new SuperMiningMachineCreationEvent(plugin);
  }

  private SuperMiningMachineCreationEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.maxSize = plugin.config.getInt(StlmppPluginConfig.superMiningMachineMaxSize);
    this.minY = new HashMap<>();
    this.minY.put(plugin.getWorldName(), 62);
    this.minY.put(plugin.getWorldNetherName(), 42);
    this.maxY = new HashMap<>();
    this.maxY.put(plugin.getWorldName(), 256);
    this.maxY.put(plugin.getWorldNetherName(), 100);
    this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  private final int maxSize;

  private final StlmppPlugin plugin;
  private final BlockFace[] blockFaces = {
    BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
  };

  private final Map<String, Integer> minY;
  private final Map<String, Integer> maxY;

  private void log(String message) {
    this.plugin.log(String.format("(SuperMiningMachineCreationEvent) %s", message), true);
  }

  @EventHandler
  public void onBlockPlaceEvent(BlockPlaceEvent event) {
    assert this.plugin.superMiningMachineManager != null;
    final var blockY = event.getBlock().getY();
    final var worldName = event.getBlock().getWorld().getName();
    if (!this.plugin.superMiningMachineManager.isBlockTypeValid(event.getBlock().getType())
        || !this.plugin.superMiningMachineManager.isWorldValid(event.getBlock().getWorld())
        || blockY > this.maxY.get(worldName)
        || blockY < this.minY.get(worldName)) {
      return;
    }
    final var blocksWithFaces =
        Arrays.stream(this.blockFaces)
            .map(face -> new Pair<>(event.getBlock().getRelative(face), face))
            .filter(
                pair ->
                    this.plugin.superMiningMachineManager.isBlockTypeValid(pair.value0.getType()))
            .toList();
    if (blocksWithFaces.isEmpty()) {
      this.log("{1} No blocks found while searching for faces");
      return;
    }
    if (blocksWithFaces.size() != 2) {
      this.log("{2} There has to be exactly 2 faces");
      return;
    }
    final Block netheriteBlock = getNetheriteBlock(event, blocksWithFaces);

    if (netheriteBlock == null) {
      this.log("{3} Could not find netherite block");
      return;
    }

    final var netheriteFaces =
        Arrays.stream(this.blockFaces)
            .map(face -> new Pair<>(netheriteBlock.getRelative(face), face))
            .filter(pair -> pair.value0.getType().equals(Material.OBSIDIAN))
            .toList();

    if (netheriteFaces.isEmpty()) {
      this.log("{4} No faces found on netherite block");
      return;
    }

    if (netheriteFaces.size() != 2) {
      this.log("{5} There has to be exactly 2 faces on netherite block");
      return;
    }

    final var block1 = netheriteFaces.getFirst().value0;
    final var face1 = netheriteFaces.getFirst().value1;
    final var block2 = netheriteFaces.getLast().value0;
    final var face2 = netheriteFaces.getLast().value1;

    if (!block1.getType().equals(Material.OBSIDIAN)
        || !block2.getType().equals(Material.OBSIDIAN)) {
      this.log("{6} Netherite faces must be obsidian");
      return;
    }

    SuperMiningMachine machine = null;

    // TODO enhance this algorithm by creating a helper class or something, this is ugly
    if (face1.equals(BlockFace.NORTH)) {
      if (face2.equals(BlockFace.SOUTH)) {
        this.log("{7} invalid faces (north and south)");
        return;
      }
      if (face2.equals(BlockFace.EAST)) {
        final var bottomRight = this.findNextNetheriteAndObsidians(netheriteBlock, BlockFace.EAST);
        if (bottomRight == null) {
          return;
        }
        final var topLeft = this.findNextNetheriteAndObsidians(netheriteBlock, BlockFace.NORTH);
        if (topLeft == null) {
          return;
        }
        final var topRight = this.findNextNetheriteAndObsidians(topLeft.value0, BlockFace.EAST);
        if (topRight == null) {
          return;
        }
        final var remaining = this.findNextNetheriteAndObsidians(topRight.value0, BlockFace.SOUTH);
        if (remaining == null) {
          return;
        }
        final List<Block> allBlocks = new ArrayList<>();
        allBlocks.add(netheriteBlock);
        allBlocks.add(bottomRight.value0);
        allBlocks.add(topLeft.value0);
        allBlocks.add(topRight.value0);
        allBlocks.addAll(remaining.value1);
        allBlocks.addAll(bottomRight.value1);
        allBlocks.addAll(topLeft.value1);
        allBlocks.addAll(topRight.value1);
        machine =
            new SuperMiningMachine(
                this.plugin,
                allBlocks,
                netheriteBlock,
                bottomRight.value0,
                topLeft.value0,
                topRight.value0);
      } else {
        final var bottomLeft = this.findNextNetheriteAndObsidians(netheriteBlock, BlockFace.WEST);
        if (bottomLeft == null) {
          return;
        }
        final var topRight = this.findNextNetheriteAndObsidians(netheriteBlock, BlockFace.NORTH);
        if (topRight == null) {
          return;
        }
        final var topLeft = this.findNextNetheriteAndObsidians(topRight.value0, BlockFace.WEST);
        if (topLeft == null) {
          return;
        }
        final var remaining = this.findNextNetheriteAndObsidians(topLeft.value0, BlockFace.SOUTH);
        if (remaining == null) {
          return;
        }
        final List<Block> allBlocks = new ArrayList<>();
        allBlocks.add(bottomLeft.value0);
        allBlocks.add(netheriteBlock);
        allBlocks.add(topLeft.value0);
        allBlocks.add(topRight.value0);
        allBlocks.addAll(bottomLeft.value1);
        allBlocks.addAll(remaining.value1);
        allBlocks.addAll(topLeft.value1);
        allBlocks.addAll(topRight.value1);
        machine =
            new SuperMiningMachine(
                this.plugin,
                allBlocks,
                bottomLeft.value0,
                remaining.value0,
                topLeft.value0,
                topRight.value0);
      }
    } else if (face1.equals(BlockFace.SOUTH)) {
      if (face2.equals(BlockFace.NORTH)) {
        this.log("{8} invalid faces (south and north)");
        return;
      }
      if (face2.equals(BlockFace.EAST)) {
        final var bottomLeft = this.findNextNetheriteAndObsidians(netheriteBlock, BlockFace.SOUTH);
        if (bottomLeft == null) {
          return;
        }
        final var topRight = this.findNextNetheriteAndObsidians(netheriteBlock, BlockFace.EAST);
        if (topRight == null) {
          return;
        }
        final var bottomRight =
            this.findNextNetheriteAndObsidians(topRight.value0, BlockFace.SOUTH);
        if (bottomRight == null) {
          return;
        }
        final var remaining =
            this.findNextNetheriteAndObsidians(bottomRight.value0, BlockFace.WEST);
        if (remaining == null) {
          return;
        }
        final List<Block> allBlocks = new ArrayList<>();
        allBlocks.add(bottomLeft.value0);
        allBlocks.add(bottomRight.value0);
        allBlocks.add(netheriteBlock);
        allBlocks.add(topRight.value0);
        allBlocks.addAll(bottomLeft.value1);
        allBlocks.addAll(bottomRight.value1);
        allBlocks.addAll(remaining.value1);
        allBlocks.addAll(topRight.value1);
        machine =
            new SuperMiningMachine(
                this.plugin,
                allBlocks,
                bottomLeft.value0,
                bottomRight.value0,
                netheriteBlock,
                topRight.value0);
      } else {
        final var bottomRight = this.findNextNetheriteAndObsidians(netheriteBlock, BlockFace.SOUTH);
        if (bottomRight == null) {
          return;
        }
        final var topLeft = this.findNextNetheriteAndObsidians(netheriteBlock, BlockFace.WEST);
        if (topLeft == null) {
          return;
        }
        final var bottomLeft = this.findNextNetheriteAndObsidians(topLeft.value0, BlockFace.SOUTH);
        if (bottomLeft == null) {
          return;
        }
        final var remaining = this.findNextNetheriteAndObsidians(bottomLeft.value0, BlockFace.EAST);
        if (remaining == null) {
          return;
        }
        final List<Block> allBlocks = new ArrayList<>();
        allBlocks.add(bottomLeft.value0);
        allBlocks.add(bottomRight.value0);
        allBlocks.add(topLeft.value0);
        allBlocks.add(netheriteBlock);
        allBlocks.addAll(bottomLeft.value1);
        allBlocks.addAll(bottomRight.value1);
        allBlocks.addAll(topLeft.value1);
        allBlocks.addAll(remaining.value1);
        machine =
            new SuperMiningMachine(
                this.plugin,
                allBlocks,
                bottomLeft.value0,
                bottomRight.value0,
                topLeft.value0,
                netheriteBlock);
      }
    }

    if (machine == null) {
      this.log("{10} could not create super mining machine");
      return;
    }

    assert plugin.superMiningMachineManager != null;
    if (plugin.superMiningMachineManager.isOverlappingAnotherMachine(machine.boundingBox)) {
      this.log("{11} super mining machine is overlapping another machine");
      return;
    }

    // TODO check if machine has been created before at the same spot and deny it

    this.log(String.format("SuperMiningMachine - %s", machine));

    this.plugin.superMiningMachineManager.addMachine(machine);

    this.plugin.sendMessage(
        String.format(
            "%s criou uma escavadeira de %s por %s, com custo de %s levels",
            event.getPlayer().getName(),
            (int) machine.boundingBox.getWidthX() - 1,
            (int) machine.boundingBox.getWidthZ() - 1,
            machine.getExpLevelRequired()));

    machine.createBaseStructure();
  }

  private @Nullable Block getNetheriteBlock(
      @NotNull BlockPlaceEvent event, @NotNull List<Pair<Block, BlockFace>> blocksWithFaces) {
    Block netheriteBlock = null;
    if (!event.getBlock().getType().equals(Material.NETHERITE_BLOCK)) {
      final var block1 = blocksWithFaces.getFirst().value0;
      final var face1 = blocksWithFaces.getFirst().value1;
      final var block2 = blocksWithFaces.getLast().value0;
      if (block1.getType().equals(Material.NETHERITE_BLOCK)) {
        netheriteBlock = block1;
      }
      if (block2.getType().equals(Material.NETHERITE_BLOCK)) {
        netheriteBlock = block2;
      }
      Block next = block1;
      int iteration = 0;
      while (netheriteBlock == null
          && next.getType().equals(Material.OBSIDIAN)
          && iteration < maxSize) {
        if (next.getType().equals(Material.NETHERITE_BLOCK)) {
          netheriteBlock = block1;
        }
        next = next.getRelative(face1);
        iteration++;
      }
    } else {
      netheriteBlock = event.getBlock();
    }
    return netheriteBlock;
  }

  @Nullable
  private Pair<Block, List<Block>> findNextNetheriteAndObsidians(
      Block netheriteBlock, BlockFace direction) {
    final List<Block> allBlocks = new ArrayList<>();
    Block nextBlock = netheriteBlock.getRelative(direction);
    if (!nextBlock.getType().equals(Material.OBSIDIAN)) {
      this.log("{8} netherite faces must be obsidian");
      return null;
    }
    int iteration = 0;
    while (!nextBlock.getType().equals(Material.NETHERITE_BLOCK)
        && nextBlock.getType().equals(Material.OBSIDIAN)
        && iteration < this.maxSize) {
      allBlocks.add(nextBlock);
      nextBlock = nextBlock.getRelative(direction);
      iteration++;
    }
    if (!nextBlock.getType().equals(Material.NETHERITE_BLOCK)) {
      this.log(
          String.format("{9} could not find next netherite block | direction = %s", direction));
      return null;
    }
    return new Pair<>(nextBlock, allBlocks);
  }
}
