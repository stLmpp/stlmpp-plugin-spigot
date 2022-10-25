package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.events.SuperThunderCheckEvent;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.WeightedRandomCollection;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class SuperThunderTask extends BukkitRunnable {

  private final SuperThunderCheckEvent superThunderCheckEvent;
  private final StlmppPlugin plugin;

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
        Tick.fromSeconds(this.plugin.config.getInt(StlmppPluginConfig.superThunderSecondsIntervalEvents))
      );
    int safeRadius = this.plugin.config.getInt(StlmppPluginConfig.superThunderSafeCoordsRadius);
    final String coordsString = this.plugin.config.getString(StlmppPluginConfig.superThunderSafeCoords, "0 0 0");
    final String[] coordsArrays = coordsString.split(" ");
    Location safeLocation = new Location(
      plugin.getWorld(),
      Integer.parseInt(coordsArrays[0]),
      Integer.parseInt(coordsArrays[1]),
      Integer.parseInt(coordsArrays[2])
    );
    this.plugin.getServer().broadcast(Component.text("R.I.P. Preparem os cuzes"));
    this.events.add(
        this.getWeight(StlmppPluginConfig.superThunderExplosiveLightningWeight),
        new SuperThunderEventExplosiveLightning(this.plugin, safeRadius, safeLocation)
      )
      .add(
        this.getWeight(StlmppPluginConfig.superThunderLightningWeight),
        new SuperThunderEventRegularLightning(this.plugin, safeRadius, safeLocation)
      )
      .add(
        this.getWeight(StlmppPluginConfig.superThunderLightningCreeperWeight),
        new SuperThunderEventLightningCreeper(this.plugin, safeRadius, safeLocation)
      )
      .add(
        this.getWeight(StlmppPluginConfig.superThunderGhastSwarmWeight),
        new SuperThunderEventGhastSwarm(this.plugin, safeRadius, safeLocation)
      );
  }

  @Override
  public void run() {
    if (!Chance.of(this.plugin.config.getDouble(StlmppPluginConfig.superThunderEventChance))) {
      return;
    }
    final var world = this.plugin.getWorld();
    if (world == null || world.getPlayers().size() == 0) {
      return;
    }
    if (!world.isThundering()) {
      this.superThunderCheckEvent.activateSuperThunderEvent();
    }
    final var randomEvent = this.events.next();
    if (this.plugin.isDevMode) {
      this.plugin.getServer()
        .broadcast(Component.text("Super thunder event: " + randomEvent.getClass().getSimpleName()));
    }
    randomEvent.run();
  }
}
