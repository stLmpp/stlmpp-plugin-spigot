package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.events.SuperThunderCheckEvent;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.WeightedRandomCollection;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SuperThunderTask extends BukkitRunnable {

  private final SuperThunderCheckEvent superThunderCheckEvent;
  private final StlmppPlugin plugin;
  private final int safeRadius;
  private final Location safeLocation;

  private final WeightedRandomCollection<SuperThunderEvent> events = new WeightedRandomCollection<>();

  private double getWeight(String name) {
    return this.plugin.config.getDouble(name);
  }

  public SuperThunderTask(SuperThunderCheckEvent superThunderCheckEvent) {
    this.superThunderCheckEvent = superThunderCheckEvent;
    this.plugin = this.superThunderCheckEvent.plugin;
    this.runTaskTimer(
        this.plugin,
        0,
        Tick.fromSeconds(this.plugin.config.getInt(Config.superThunderSecondsIntervalEvents))
      );
    this.safeRadius = this.plugin.config.getInt(Config.superThunderSafeCoordsRadius);
    final String coordsString = this.plugin.config.getString(Config.superThunderSafeCoords);
    assert coordsString != null;
    final String[] coordsArrays = coordsString.split(" ");
    this.safeLocation =
      new Location(
        plugin.getWorld(),
        Integer.parseInt(coordsArrays[0]),
        Integer.parseInt(coordsArrays[1]),
        Integer.parseInt(coordsArrays[2])
      );
    this.plugin.getServer().broadcastMessage("R.I.P. Preparem os cuzes");
    this.events.add(
        this.getWeight(Config.superThunderExplosiveLightningWeight),
        new SuperThunderEventExplosiveLightning()
      )
      .add(this.getWeight(Config.superThunderLightningWeight), new SuperThunderEventRegularLightning())
      .add(this.getWeight(Config.superThunderLightningCreeperWeight), new SuperThunderEventLightningCreeper())
      .add(this.getWeight(Config.superThunderGhastSwarmWeight), new SuperThunderEventGhastSwarm());
  }

  @Override
  public void run() {
    if (!Chance.of(this.plugin.config.getInt(Config.superThunderEventChance))) {
      return;
    }
    final var world = this.plugin.getWorld();
    if (world == null || world.getPlayers().size() == 0) {
      return;
    }
    if (!world.isThundering()) {
      this.superThunderCheckEvent.deactivateSuperThunderEvent();
    }
    final var randomEvent = this.events.next();
    if (this.plugin.isDevMode) {
      this.plugin.getServer().broadcastMessage("Super thunder event: " + randomEvent.getClass().getSimpleName());
    }
    randomEvent.run(this.plugin, this.safeRadius, this.safeLocation);
  }
}
