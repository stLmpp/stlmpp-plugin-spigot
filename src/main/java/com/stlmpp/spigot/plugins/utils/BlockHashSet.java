package com.stlmpp.spigot.plugins.utils;

import java.util.*;
import java.util.stream.Stream;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

public class BlockHashSet implements Set<Block> {
  public BlockHashSet(@NotNull World world) {
    this.world = world;
  }

  public BlockHashSet(@NotNull World world, @NotNull Collection<Block> blocks) {
    this.world = world;
    this.addAll(blocks);
  }

  private final World world;
  private final Map<String, BlockCoords> map = new HashMap<>();

  public static String createKey(Block block) {
    return "b-" + block.getX() + "-" + block.getY() + "-" + block.getZ();
  }

  public static BlockCoords createValue(Block block) {
    return new BlockCoords(block);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    if (o instanceof Block block) {
      return map.containsKey(createKey(block));
    }
    return false;
  }

  @NotNull
  @Override
  public Iterator<Block> iterator() {
    final var mapIterator = map.values().iterator();
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return mapIterator.hasNext();
      }

      @Override
      public Block next() {
        return mapIterator.next().toBlock(world);
      }
    };
  }

  @NotNull
  @Override
  public Object @NotNull [] toArray() {
    return map.values().stream().map(coords -> coords.toBlock(world)).toArray();
  }

  @NotNull
  @Override
  public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
    return map.values().stream()
        .map(coords -> coords.toBlock(world))
        .toArray(size -> a.length >= size ? a : Arrays.copyOf(a, size));
  }

  @Override
  public boolean add(Block block) {
    return map.put(createKey(block), createValue(block)) == null;
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof Block block) {
      return map.remove(createKey(block)) != null;
    }
    return false;
  }

  @Override
  public boolean containsAll(@NotNull Collection<?> c) {
    for (Object obj : c) {
      if (!(obj instanceof Block block) || !map.containsKey(createKey(block))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(@NotNull Collection<? extends Block> c) {
    boolean added = false;
    for (Block block : c) {
      if (add(block)) {
        added = true;
      }
    }
    return added;
  }

  @Override
  public boolean retainAll(@NotNull Collection<?> c) {
    Set<String> keysToRetain = new HashSet<>();
    for (Object obj : c) {
      if (obj instanceof Block block) {
        keysToRetain.add(createKey(block));
      }
    }
    return map.keySet().retainAll(keysToRetain);
  }

  @Override
  public boolean removeAll(@NotNull Collection<?> c) {
    boolean changed = false;
    for (Object obj : c) {
      if (obj instanceof Block block && remove(block)) {
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  public List<Block> toList() {
    return this.stream().toList();
  }

  @Override
  public Stream<Block> stream() {
    return this.map.values().stream().map(c -> c.toBlock(world));
  }
}
