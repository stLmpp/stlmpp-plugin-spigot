package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.WeightedRandomCollection;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class SuperThunderTask extends BukkitRunnable {

  private final StlmppPlugin plugin;

  private final WeightedRandomCollection<SuperThunderEvent> events = new WeightedRandomCollection<>();

  private double getWeight(String name) {
    return this.plugin.config.getDouble(name);
  }

  public SuperThunderTask(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.runTaskTimer(
        this.plugin,
        0,
        Tick.fromSeconds(this.plugin.config.getInt(Config.superThunderSecondsIntervalEvents))
      );
    Bukkit.broadcastMessage("R.I.P. Preparem os cuzes");
    this.events.add(
        this.getWeight(Config.superThunderExplosiveLightningWeight),
        new SuperThunderEventExplosiveLightning()
      )
      .add(this.getWeight(Config.superThunderLightningWeight), new SuperThunderEventRegularLightning())
      .add(this.getWeight(Config.superThunderLightningCreeperWeight), new SuperThunderEventLightningCreeper());
  }

  @Override
  public void run() {
    if (!Chance.of(this.plugin.config.getInt(Config.superThunderEventChance))) {
      return;
    }
    var randomEvent = this.events.next();
    if (this.plugin.isDevMode) {
      Bukkit.broadcastMessage("Super thunder event: " + randomEvent.getClass().getName());
    }
    randomEvent.run(this.plugin);
  }
}
