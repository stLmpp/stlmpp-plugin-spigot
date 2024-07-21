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
    this.runTaskLater(this.plugin, Tick.fromSeconds(2));
  }

  public void stopLastTask() {
    if (this.lastTask != null) {
      this.lastTask.cancel();
    }
  }

  @Override
  public void run() {
    if (Chance.of(this.chance)) {
      return;
    }
    final var world = this.plugin.getWorldNether();
    if (world == null || world.getPlayers().isEmpty()) {
      return;
    }
    final var lightningLocation = Util.getRandomLocationAroundRandomPlayer(world);
    if (lightningLocation == null) {
      return;
    }
    if (!Chance.of(this.realChance)) {
      world.strikeLightningEffect(lightningLocation);
      return;
    }
    world.strikeLightning(lightningLocation);
    if (!Chance.of(this.explosionChance)) {
      return;
    }
    final var explosionPower = Util.randomFloat(this.explosionMinPower, this.explosionMaxPower);
    world.createExplosion(lightningLocation, explosionPower, true, true);
    final var secondsLater = ThreadLocalRandom.current().nextInt(this.minSeconds, this.maxSeconds);
    this.lastTask = this.runTaskLater(this.plugin, Tick.fromSeconds(secondsLater));
  }
}
