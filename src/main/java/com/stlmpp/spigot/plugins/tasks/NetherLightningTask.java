package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.scheduler.BukkitRunnable;

public class NetherLightningTask extends BukkitRunnable {

  private final StlmppPlugin plugin;
  private final double chance;
  private final double realChance;
  private final double explosionChance;
  private final float explosionMinPower;
  private final float explosionMaxPower;

  public NetherLightningTask(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.chance = this.plugin.config.getDouble(StlmppPluginConfig.netherLightningChance);
    this.explosionChance = this.plugin.config.getDouble(StlmppPluginConfig.netherLightningExplosionChance);
    this.explosionMinPower = (float) this.plugin.config.getDouble(StlmppPluginConfig.netherLightningExplosionMinPower);
    this.explosionMaxPower = (float) this.plugin.config.getDouble(StlmppPluginConfig.netherLightningExplosionMaxPower);
    this.realChance = this.plugin.config.getDouble(StlmppPluginConfig.netherLightningRealChance);
    this.runTaskTimer(
        this.plugin,
        0,
        Tick.fromSeconds(this.plugin.config.getInt(StlmppPluginConfig.netherLightningChancePerSecond))
    );
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
    if (Chance.of(this.realChance)) {
      world.strikeLightning(lightningLocation);
      if (Chance.of(this.explosionChance)) {
        final var explosionPower = Util.randomFloat(this.explosionMinPower, this.explosionMaxPower);
        world.createExplosion(lightningLocation, explosionPower, true, true);
      }
    } else {
      world.strikeLightningEffect(lightningLocation);
    }
  }
}
