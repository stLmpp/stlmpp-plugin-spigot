package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortal;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortalLeakingEvent;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class NetherPortalLeakingTask extends BukkitRunnable {

  private final Deque<Location> locations;
  private final NetherPortalLeakingEvent netherPortalLeakingEvent;
  private final World world;
  private final NetherPortal netherPortal;
  private final double chanceOfNetherrackFire;
  private final int radius;
  private final double knockbackPower;
  private final ArrayList<Vector> particlesLocations;

  public NetherPortalLeakingTask(
      NetherPortalLeakingEvent netherPortalLeakingEvent,
      Deque<Location> locations,
      World world,
      NetherPortal netherPortal,
      int radius,
      ArrayList<Vector> particlesLocations) {
    this.netherPortalLeakingEvent = netherPortalLeakingEvent;
    this.locations = locations;
    this.world = world;
    this.netherPortal = netherPortal;
    this.chanceOfNetherrackFire =
        this.netherPortalLeakingEvent.plugin.config.getDouble(
            StlmppPluginConfig.netherPortalLeakingChanceOfNetherrackFire);
    this.radius = radius;

    this.knockbackPower = this.calculateKnockbackPower();
    this.particlesLocations = particlesLocations;
    this.knockbackEntities();
    this.createParticlesEffect();
    this.runTaskTimer(
        this.netherPortalLeakingEvent.plugin, Tick.fromSeconds(5), Tick.fromSeconds(2));
  }

  private double calculateKnockbackPower() {
    final var height = this.netherPortal.boundingBox.getHeight();
    return (height + this.netherPortal.width) / 2;
  }

  private void createParticlesEffect() {
    final var netherPortalCenterLocation = this.netherPortal.getCenterLocation();
    this.world.playSound(netherPortalCenterLocation, Sound.BLOCK_PORTAL_TRAVEL, 1, 1);
    for (Vector vector : this.particlesLocations) {
      final var offset = vector.clone().subtract(netherPortalCenterLocation.toVector()).normalize();
      world.spawnParticle(
          Particle.DRAGON_BREATH,
          netherPortalCenterLocation,
          0,
          offset.getX(),
          offset.getY(),
          offset.getZ());
    }
  }

  private void knockbackEntities() {
    if (this.knockbackPower <= 0) {
      return;
    }
    final var boundingBox = this.netherPortal.boundingBox.clone().expand(this.radius);
    final var entities = this.world.getNearbyEntities(boundingBox);
    if (entities.isEmpty()) {
      return;
    }
    final var netherPortalCenterVector = this.netherPortal.getCenter();
    if (this.netherPortalLeakingEvent.plugin.isDevMode) {
      this.netherPortalLeakingEvent
          .plugin
          .getLogger()
          .info(
              "Entities = "
                  + entities.stream()
                      .map(item -> item.getType().name())
                      .collect(Collectors.joining(", ")));
    }
    for (Entity anyEntity : entities) {
      if (!(anyEntity instanceof LivingEntity entity)) {
        continue;
      }
      final var entityLocation = entity.getLocation().toVector();
      if (!entityLocation.isInSphere(netherPortalCenterVector, this.radius)) {
        continue;
      }
      final var distance = entityLocation.distance(netherPortalCenterVector);
      final var percent = distance / this.radius;
      final var multiplier = this.knockbackPower - (this.knockbackPower * percent);
      final var vectorVelocity =
          netherPortalCenterVector
              .clone()
              .subtract(entityLocation)
              .normalize()
              .multiply(multiplier * -1);
      entity.setVelocity(vectorVelocity);
      entity.damage(multiplier);
    }
  }

  @Override
  public void run() {
    if (this.locations.isEmpty()) {
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
