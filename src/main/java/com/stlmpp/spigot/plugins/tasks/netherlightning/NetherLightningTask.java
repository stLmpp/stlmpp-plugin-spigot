package com.stlmpp.spigot.plugins.tasks.netherlightning;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.RandomList;
import com.stlmpp.spigot.plugins.utils.Rng;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class NetherLightningTask extends BukkitRunnable {

  public static @Nullable NetherLightningTask register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.netherLightningEnabled)) {
      return null;
    }
    return new NetherLightningTask(plugin);
  }

  private final StlmppPlugin plugin;
  private final double chance;
  private final double realChance;
  private final double explosionChance;
  private final float explosionMinPower;
  private final float explosionMaxPower;
  private final int maxSeconds;
  private final int minSeconds;

  private double increasedRealChance = 0.0d;
  private double realRound = 1.0d;
  private double increasedExplosionChance = 0.0d;
  private double explosionRound = 1.0d;

  @Nullable private BukkitTask lastTask;

  private NetherLightningTask(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.chance = this.plugin.config.getDouble(StlmppPluginConfig.netherLightningChance);
    this.explosionChance =
        this.plugin.config.getDouble(StlmppPluginConfig.netherLightningExplosionChance);
    this.explosionMinPower =
        (float) this.plugin.config.getDouble(StlmppPluginConfig.netherLightningExplosionMinPower);
    this.explosionMaxPower =
        (float) this.plugin.config.getDouble(StlmppPluginConfig.netherLightningExplosionMaxPower);
    this.realChance = this.plugin.config.getDouble(StlmppPluginConfig.netherLightningRealChance);
    this.maxSeconds = this.plugin.config.getInt(StlmppPluginConfig.netherLightningMaxSeconds);
    this.minSeconds = this.plugin.config.getInt(StlmppPluginConfig.netherLightningMinSeconds);
    this.plugin.log(
        String.format(
            "Nether lightning activated with %s%% of chance, every %s to %s seconds!",
            this.chance, this.minSeconds, this.maxSeconds));
    this.run();
  }

  public void stopLastTask() {
    if (this.lastTask != null) {
      this.lastTask.cancel();
    }
  }

  @Override
  public void run() {
    this.startTask(0);
  }

  private void startTask() {
    final var secondsLater = Rng.nextInt(this.minSeconds, this.maxSeconds);
    this.plugin.log(String.format("Next lightning in %s seconds", secondsLater), true);
    this.startTask(secondsLater);
  }

  private void startTask(int seconds) {
    this.lastTask =
        plugin.runLater(
            Tick.fromSeconds(seconds),
            () -> {
              final var hasScheduledNext = executeLightning(execute());
              if (!hasScheduledNext) {
                startTask();
              }
            });
  }

  private boolean executeLightning(NetherLightningReturn result) {
    if (result.world() == null) {
      return false;
    }
    if (!result.isReal() && result.lightningLocation() != null) {
      plugin.log(
          String.format(
              "Strike lightning at %s | isReal = %s | increasedRealChance = %s | increasedExplosionChance = %s",
              result.lightningLocation(), false, increasedRealChance, increasedExplosionChance),
          true);
      result.world().strikeLightningEffect(result.lightningLocation());
      return false;
    }
    if (!result.isReal() || result.lightningLocation() == null) {
      return false;
    }
    if (result.explosionPower() == null) {
      plugin.log(
          String.format(
              "Strike lightning at %s | isReal = %s | increasedRealChance = %s | increasedExplosionChance = %s",
              result.lightningLocation(), false, increasedRealChance, increasedExplosionChance),
          true);
      result.world().strikeLightning(result.lightningLocation());
      return false;
    }
    plugin.log(
        String.format(
            "Strike lightning at %s | isReal = %s | increasedRealChance = %s | increasedExplosionChance = %s | explosionPower = %s",
            result.lightningLocation(),
            false,
            increasedRealChance,
            increasedExplosionChance,
            result.explosionPower()),
        true);
    final var radius = (int) Math.ceil(result.explosionPower());
    final RandomList<Location> particles = new RandomList<>();
    final var cx = result.lightningLocation().getBlockX();
    final var cz = result.lightningLocation().getBlockZ();
    final var y = result.lightningLocation().getBlockY();
    for (int x = cx - radius; x <= cx + radius; x++) {
      for (int z = cz - radius; z <= cz + radius; z++) {
        if (!new Vector(x, y, z).isInSphere(result.lightningLocation().toVector(), radius)) {
          continue;
        }
        particles.add(result.lightningLocation().clone().set(x, y, z));
      }
    }
    AtomicInteger effectIndex = new AtomicInteger();
    final var effect =
        plugin.runTimer(
            0,
            Tick.fromSeconds(0.2),
            () -> {
              final var index = effectIndex.getAndIncrement();
              final int quantity = Rng.nextInt(15, 20);
              for (var i = 0; i < quantity; i++) {
                result
                    .world()
                    .spawnParticle(
                        Particle.ELECTRIC_SPARK,
                        particles
                            .next()
                            .clone()
                            .add(
                                Rng.nextDouble(0, 0.99),
                                Rng.nextDouble(0.5, 2.5),
                                Rng.nextDouble(0, 0.99)),
                        0,
                        Rng.nextDouble(-0.15, 0.15),
                        Rng.nextDouble(0.01, 0.3),
                        Rng.nextDouble(-0.15, 0.15));
              }
              if (index % 10 == 0) {
                result
                    .world()
                    .playSound(
                        result.lightningLocation(), Sound.ENTITY_GUARDIAN_ATTACK, 3.0f, 1.2f);
              }
            });
    plugin.runLater(
        Tick.fromSeconds(5),
        () -> {
          effect.cancel();
          result.world().strikeLightning(result.lightningLocation());
          result
              .world()
              .createExplosion(result.lightningLocation(), result.explosionPower(), true, true);
          startTask();
        });
    return true;
  }

  private NetherLightningReturn execute() {
    final var willRun = Rng.chance(chance);
    plugin.log(String.format("Running task Nether Lightning | willRun = %s", willRun), true);
    if (!willRun) {
      return new NetherLightningReturn(null, false, null, null);
    }
    final var world = plugin.getWorldNether();
    if (world == null || world.getPlayers().isEmpty()) {
      increasedRealChance = 0;
      increasedExplosionChance = 0;
      return new NetherLightningReturn(null, false, null, null);
    }
    final var lightningLocation = Util.getRandomLocationAroundRandomPlayer(world);
    if (lightningLocation == null) {
      return new NetherLightningReturn(world, false, null, null);
    }
    Util.setFloor(lightningLocation);
    final var isReal = Rng.chance(realChance + increasedRealChance);
    if (!isReal) {
      increasedRealChance = increasedRealChance + (1 * realRound);
      increasedExplosionChance = increasedExplosionChance + (0.05 * explosionRound);
      return new NetherLightningReturn(world, false, lightningLocation, null);
    }
    increasedRealChance = 0;
    realRound = Math.min(realRound + 0.1, 5.0);
    if (!Rng.chance(explosionChance + increasedExplosionChance)) {
      increasedExplosionChance += 0.1;
      return new NetherLightningReturn(world, true, lightningLocation, null);
    }
    increasedExplosionChance = 0;
    explosionRound = Math.min(explosionRound + 0.05, 5.0);
    final var explosionPower = Rng.nextFloat(explosionMinPower, explosionMaxPower);
    return new NetherLightningReturn(world, true, lightningLocation, explosionPower);
  }
}
