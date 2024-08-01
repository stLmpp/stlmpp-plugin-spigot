package com.stlmpp.spigot.plugins.events.superminingmachine;

import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public record SMMCorner(Block block, SMMCornerType type) {

  public List<BlockFace> getFacesListInOrder() {
    return facesListInOrder.get(type);
  }

  private static final Map<SMMCornerType, List<BlockFace>> facesListInOrder =
      Map.of(
          SMMCornerType.BottomLeft,
              List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST),
          SMMCornerType.BottomRight,
              List.of(BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH),
          SMMCornerType.TopLeft,
              List.of(BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH),
          SMMCornerType.TopRight,
              List.of(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST));
}
