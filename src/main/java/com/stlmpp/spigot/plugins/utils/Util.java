package com.stlmpp.spigot.plugins.utils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Util {

  public static int getFloor(World world, Location location) {
    Integer floor = Util.getFloor(world, location, false, 64);
    if (floor == null) {
      // This is never going to happen
      floor = -1;
    }
    return floor;
  }

  public static Integer getFloor(World world, Location location, boolean returnNullOnMaxIterations) {
    return Util.getFloor(world, location, returnNullOnMaxIterations, 64);
  }

  public static Integer getFloor(World world, Location location, boolean returnNullOnMaxIterations, int maxIterations) {
    var iteration = 0;
    var locationY = location.getBlockY();
    while (
      world.getBlockAt(location.getBlockX(), locationY, location.getBlockZ()).getType().isSolid() &&
      iteration <= maxIterations
    ) {
      iteration++;
      locationY--;
    }
    return iteration > maxIterations && returnNullOnMaxIterations ? null : locationY;
  }

  public static int getCeiling(World world, Location location) {
    Integer floor = Util.getCeiling(world, location, false, 64);
    if (floor == null) {
      // This is never going to happen
      floor = -1;
    }
    return floor;
  }

  public static Integer getCeiling(World world, Location location, boolean returnNullOnMaxIterations) {
    return Util.getCeiling(world, location, returnNullOnMaxIterations, 64);
  }

  public static Integer getCeiling(
    World world,
    Location location,
    boolean returnNullOnMaxIterations,
    int maxIterations
  ) {
    var iteration = 0;
    var locationY = location.getBlockY();
    while (
      !world.getBlockAt(location.getBlockX(), locationY, location.getBlockZ()).getType().isSolid() &&
      iteration <= maxIterations
    ) {
      iteration++;
      locationY++;
    }
    return iteration > maxIterations && returnNullOnMaxIterations ? null : locationY;
  }

  public static void setMaterialsFromNames(@NotNull Set<Material> materialSet, @NotNull List<?> materialNames) {
    for (Object materialName : materialNames) {
      if (!(materialName instanceof String materialNameString)) {
        continue;
      }
      final var material = Material.getMaterial(materialNameString);
      if (material != null) {
        materialSet.add(material);
      }
    }
  }

  public static boolean isInRadius(Location check, Location start, int radius) {
    return (
      Math.abs(check.getX() - start.getX()) <= radius &&
      Math.abs(check.getY() - start.getY()) <= radius &&
      Math.abs(check.getZ() - start.getZ()) <= radius
    );
  }

  public static Location getRandomLocationAroundPlayer(Player player) {
    final var playerLocation = player.getLocation();
    final var playerX = playerLocation.getBlockX();
    final var playerY = playerLocation.getBlockY();
    final var playerZ = playerLocation.getBlockZ();
    final var lightningX = ThreadLocalRandom.current().nextInt(playerX - 50, playerX + 51);
    final var lightningY = ThreadLocalRandom.current().nextInt(playerY - 10, playerY + 11);
    final var lightningZ = ThreadLocalRandom.current().nextInt(playerZ - 50, playerZ + 51);
    final var lightningLocation = new Location(player.getWorld(), lightningX, lightningY, lightningZ);
    lightningLocation.setY(Util.getFloor(player.getWorld(), lightningLocation));
    return lightningLocation;
  }

  @Nullable
  public static Location getRandomLocationAroundRandomPlayer(World world) {
    final var players = world.getPlayers();
    final var playersSize = players.size();
    if (playersSize == 0) {
      return null;
    }
    return Util.getRandomLocationAroundPlayer(players.get(ThreadLocalRandom.current().nextInt(0, playersSize)));
  }
}
