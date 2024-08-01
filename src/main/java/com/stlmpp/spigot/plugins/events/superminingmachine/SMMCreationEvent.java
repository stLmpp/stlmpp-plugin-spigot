package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Pair;
import java.util.*;

import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SMMCreationEvent implements Listener {

  public static SMMCreationEvent register(@NotNull StlmppPlugin plugin) {
    return new SMMCreationEvent(plugin);
  }

  private SMMCreationEvent(StlmppPlugin plugin) {
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

  private final Map<SMMCornerType, HashSet<BlockFace>> cornerIdentificationMap =
      Map.of(
          SMMCornerType.BottomLeft, new HashSet<>(List.of(BlockFace.NORTH, BlockFace.EAST)),
          SMMCornerType.BottomRight, new HashSet<>(List.of(BlockFace.NORTH, BlockFace.WEST)),
          SMMCornerType.TopLeft, new HashSet<>(List.of(BlockFace.SOUTH, BlockFace.EAST)),
          SMMCornerType.TopRight, new HashSet<>(List.of(BlockFace.SOUTH, BlockFace.WEST)));

  private void log(String message) {
    this.plugin.log(String.format("(SuperMiningMachineCreationEvent) %s", message), true);
  }

  @EventHandler
  public void onBlockPlaceEvent(BlockPlaceEvent event) {
    assert this.plugin.smmManager != null;
    final var blockY = event.getBlock().getY();
    final var worldName = event.getBlock().getWorld().getName();
    if (!this.plugin.smmManager.isBlockTypeValid(event.getBlock().getType())
        || !this.plugin.smmManager.isWorldValid(event.getBlock().getWorld())
        || blockY > this.maxY.get(worldName)
        || blockY < this.minY.get(worldName)) {
      return;
    }

    SMMCorner corner1 = parseCorner(event.getBlock());

    if (corner1 == null) {
      final var edge = parseEdge(event.getBlock());
      if (edge == null) {
        return;
      }
      corner1 = getCornerFromEdge(edge);
    }

    if (corner1 == null) {
      return;
    }

    final var allBlocks = new ArrayList<>(List.of(corner1.block()));
    var corner = corner1;

    SMMCorner bottomLeft = null;
    SMMCorner bottomRight = null;
    SMMCorner topLeft = null;
    SMMCorner topRight = null;

    for (final var face : corner1.getFacesListInOrder()) {
      final var r = getEdgesAndCornerFromCorner(corner, face);
      if (r == null) {
        return;
      }
      allBlocks.addAll(r.value1.stream().map(SMMEdge::block).toList());
      allBlocks.add(r.value0.block());
      corner = r.value0;
      switch (corner.type()) {
        case BottomLeft -> bottomLeft = corner;
        case BottomRight -> bottomRight = corner;
        case TopLeft -> topLeft = corner;
        case TopRight -> topRight = corner;
      }
    }

    if (bottomLeft == null || bottomRight == null || topLeft == null || topRight == null) {
      return;
    }

    final var machine =
        new SuperMiningMachine(
            plugin,
            allBlocks,
            bottomLeft.block(),
            bottomRight.block(),
            topLeft.block(),
            topRight.block());

    assert plugin.smmManager != null;
    if (plugin.smmManager.isOverlappingAnotherMachine(machine.boundingBox)) {
      this.log("{11} super mining machine is overlapping another machine");
      return;
    }

    this.log(String.format("SuperMiningMachine - %s", machine));

    this.plugin.smmManager.addMachine(machine);

    this.plugin.sendMessage(
        String.format(
            "%s criou uma escavadeira de %s por %s, com custo de %s levels",
            event.getPlayer().getName(),
            (int) machine.boundingBox.getWidthX() - 1,
            (int) machine.boundingBox.getWidthZ() - 1,
            machine.getExpLevelRequired()));

    machine.createBaseStructure();
  }

  @Nullable
  private SMMCorner parseCorner(@NotNull Block block) {
    if (!block.getType().equals(Material.NETHERITE_BLOCK)) {
      return null;
    }
    final var faces =
        Arrays.stream(blockFaces)
            .filter(face -> block.getRelative(face).getType().equals(Material.OBSIDIAN))
            .toList();
    if (faces.size() != 2) {
      return null;
    }
    final var face1 = faces.getFirst();
    final var face2 = faces.getLast();
    if (face1.getOppositeFace().equals(face2)) {
      return null;
    }
    final var corner =
        Util.findFirst(
            this.cornerIdentificationMap.entrySet().stream().toList(),
            kv -> kv.getValue().containsAll(faces));
    if (corner == null) {
      return null;
    }
    return new SMMCorner(block, corner.getKey());
  }

  @Nullable
  private SMMEdge parseEdge(@NotNull Block block) {
    if (!block.getType().equals(Material.OBSIDIAN)) {
      return null;
    }
    assert plugin.smmManager != null;
    final var faces =
        Arrays.stream(blockFaces)
            .filter(face -> plugin.smmManager.isBlockTypeValid(block.getRelative(face).getType()))
            .toList();
    if (faces.size() != 2) {
      return null;
    }
    final var face1 = faces.getFirst();
    final var face2 = faces.getLast();
    if (!face1.getOppositeFace().equals(face2)) {
      return null;
    }
    return new SMMEdge(block, face1);
  }

  @Nullable
  private SMMCorner getCornerFromEdge(@NotNull SMMEdge edge) {
    Block blockFace1 = edge.block();
    Block blockFace2 = edge.block();
    int iteration = 0;
    do {
      blockFace1 = blockFace1.getRelative(edge.face());
      blockFace2 = blockFace2.getRelative(edge.face().getOppositeFace());
      iteration++;
    } while (!blockFace1.getType().equals(Material.NETHERITE_BLOCK)
        && !blockFace2.getType().equals(Material.NETHERITE_BLOCK)
        && iteration < maxSize);
    return blockFace1.getType().equals(Material.NETHERITE_BLOCK)
        ? parseCorner(blockFace1)
        : parseCorner(blockFace2);
  }

  @Nullable
  private Pair<SMMCorner, List<SMMEdge>> getEdgesAndCornerFromCorner(
      @NotNull SMMCorner corner, BlockFace direction) {
    Block block = corner.block();
    final List<SMMEdge> obsidians = new ArrayList<>();
    int iteration = 0;
    do {
      block = block.getRelative(direction);
      obsidians.add(new SMMEdge(block, direction));
      iteration++;
    } while (block.getType().equals(Material.OBSIDIAN) && iteration < maxSize);
    if (!block.getType().equals(Material.NETHERITE_BLOCK)) {
      return null;
    }
    final var nextCorner = parseCorner(block);
    if (nextCorner == null) {
      return null;
    }
    return new Pair<>(nextCorner, obsidians);
  }
}
