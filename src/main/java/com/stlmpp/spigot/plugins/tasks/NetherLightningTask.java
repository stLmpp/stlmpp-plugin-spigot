package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.scheduler.BukkitRunnable;

public class NetherLightningTask extends BukkitRunnable {

  private final StlmppPlugin plugin;

  public NetherLightningTask(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.runTaskTimer(
        this.plugin,
        0,
        Tick.fromSeconds(this.plugin.config.getInt(Config.netherLightningChancePerSecond))
      );
  }

  @Override
  public void run() {
    if (Chance.of(this.plugin.config.getDouble(Config.netherLightningChance))) {
      return;
    }
    final var world = this.plugin.getWorldNether();
    if (world == null || world.getPlayers().size() == 0) {
      return;
    }
    final var lightningLocation = Util.getRandomLocationAroundRandomPlayer(world);
    if (lightningLocation == null) {
      return;
    }
    world.strikeLightning(lightningLocation);
    if (Chance.of(this.plugin.config.getDouble(Config.netherLightningExplosionChance))) {
      final var explosionPower = ThreadLocalRandom.current().nextInt(0, 16);
      world.createExplosion(lightningLocation, (float) explosionPower, true, true);
    }
  }
}
