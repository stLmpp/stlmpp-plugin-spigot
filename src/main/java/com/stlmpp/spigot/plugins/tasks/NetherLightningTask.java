package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

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
    final var secondsLater =
        ThreadLocalRandom.current().nextInt(this.minSeconds, this.maxSeconds + 1);
    this.plugin.log(String.format("Next lightning in %s seconds", secondsLater), true);
    this.startTask(secondsLater);
  }

  private void startTask(int seconds) {
    this.lastTask =
        plugin.runLater(
            Tick.fromSeconds(seconds),
            () -> {
              execute();
              startTask();
            });
  }

  private void execute() {
    final var willRun = Chance.of(chance);
    plugin.log(String.format("Running task Nether Lightning | willRun = %s", willRun), true);
    if (!willRun) {
      return;
    }
    final var world = plugin.getWorldNether();
    if (world == null || world.getPlayers().isEmpty()) {
      increasedRealChance = 0;
      increasedExplosionChance = 0;
      return;
    }
    final var lightningLocation = Util.getRandomLocationAroundRandomPlayer(world);
    if (lightningLocation == null) {
      return;
    }
    lightningLocation.setY(Util.getFloor(lightningLocation));
    final var isReal = Chance.of(realChance + increasedRealChance);
    plugin.log(
        String.format(
            "Strike lightning at %s | isReal = %s | increasedRealChance = %s | increasedExplosionChance = %s",
            lightningLocation, isReal, increasedRealChance, increasedExplosionChance),
        true);
    if (!isReal) {
      increasedRealChance = increasedRealChance + (1 * realRound);
      increasedExplosionChance = increasedExplosionChance + (0.05 * explosionRound);
      world.strikeLightningEffect(lightningLocation);
      return;
    }
    increasedRealChance = 0;
    realRound = Math.min(realRound + 0.1, 5.0);
    world.strikeLightning(lightningLocation);
    if (!Chance.of(explosionChance + increasedExplosionChance)) {
      increasedExplosionChance += 0.1;
      return;
    }
    increasedExplosionChance = 0;
    explosionRound = Math.min(explosionRound + 0.05, 5.0);
    final var explosionPower = Util.randomFloat(explosionMinPower, explosionMaxPower);
    world.createExplosion(lightningLocation, explosionPower, true, true);
  }
}
