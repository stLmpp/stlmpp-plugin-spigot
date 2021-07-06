package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortal;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortalLeakingEvent;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Deque;

public class NetherPortalLeakingTask extends BukkitRunnable {

  private final Deque<Location> locations;
  private final NetherPortalLeakingEvent netherPortalLeakingEvent;
  private final World world;
  private final NetherPortal netherPortal;
  private final double chanceOfNetherrackFire;
  private final int radius;
  private final double knockbackPower;

  public NetherPortalLeakingTask(
    NetherPortalLeakingEvent netherPortalLeakingEvent,
    Deque<Location> locations,
    World world,
    NetherPortal netherPortal,
    int radius
  ) {
    this.netherPortalLeakingEvent = netherPortalLeakingEvent;
    this.locations = locations;
    this.world = world;
    this.netherPortal = netherPortal;
    this.chanceOfNetherrackFire =
      this.netherPortalLeakingEvent.plugin.config.getDouble(Config.netherPortalLeakingChanceOfNetherrackFire);
    this.radius = radius;
    this.knockbackPower =
      this.netherPortalLeakingEvent.plugin.config.getDouble(Config.netherPortalLeakingKnockbackPower);
    this.knockBackPlayers();
    this.runTaskTimer(this.netherPortalLeakingEvent.plugin, 0, Tick.fromSeconds(2));
  }

  private void knockBackPlayers() {
    if (this.knockbackPower <= 0) {
      return;
    }
    final var players = this.world.getPlayers();
    if (players.size() == 0) {
      return;
    }
    final var netherPortalCenterVector = this.netherPortal.getCenter().toVector();
    for (Player player : players) {
      final var playerLocationVector = player.getLocation().toVector();
      if (!playerLocationVector.isInSphere(netherPortalCenterVector, this.radius)) {
        continue;
      }
      final var distance = playerLocationVector.distance(netherPortalCenterVector);
      final var percent = distance / radius;
      final var multiplier = this.knockbackPower - (this.knockbackPower * percent);
      final var vectorVelocity = netherPortalCenterVector
        .clone()
        .subtract(playerLocationVector)
        .normalize()
        .multiply(multiplier * -1);
      player.setVelocity(vectorVelocity);
    }
  }

  @Override
  public void run() {
    if (this.locations.size() == 0) {
      this.netherPortalLeakingEvent.tryCancelTask(this.netherPortal);
      return;
    }
    final var block = this.world.getBlockAt(this.locations.pop());
    final var blockAtMaterial = block.getType();
    if (!NetherPortalLeakingEvent.isValidMaterial(blockAtMaterial)) {
      return;
    }
    final var netherMaterial = Util.convertToNetherMaterial(blockAtMaterial);
    block.setType(netherMaterial);
    if (netherMaterial == Material.NETHERRACK && Chance.of(this.chanceOfNetherrackFire)) {
      final var blockUp = block.getRelative(BlockFace.UP);
      if (blockUp.getType().isAir()) {
        blockUp.setType(Material.FIRE);
      }
    }
  }
}
