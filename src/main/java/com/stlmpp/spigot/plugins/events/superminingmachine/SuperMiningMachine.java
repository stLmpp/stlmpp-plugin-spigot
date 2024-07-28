package com.stlmpp.spigot.plugins.events.superminingmachine;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

public class SuperMiningMachine {

  public SuperMiningMachine(
      HashSet<Block> blocks,
      Block bottomLeftBlock,
      Block bottomRightBlock,
      Block topLeftBlock,
      Block topRightBlock) {
    this.blocks = blocks;
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
  }

  public final BoundingBox boundingBox;
  public final BoundingBox innerBoundingBox;
  public final HashSet<Block> blocks;
  public final Block bottomLeftBlock;
  public final Block bottomRightBlock;
  public final Block topLeftBlock;
  public final Block topRightBlock;
}
