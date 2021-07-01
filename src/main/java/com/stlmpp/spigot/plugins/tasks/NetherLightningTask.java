package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Tick;
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
    if (Chance.of(this.plugin.config.getInt(Config.netherLightningChance))) {
      return;
    }
    final var world = this.plugin.getWorldNether();
    if (world == null) {
      return;
    }
    this.plugin.strikeLightningRandomPlayerWithChanceOfExplosion(
        world,
        this.plugin.config.getInt(Config.netherLightningExplosionChance)
      );
  }
}
