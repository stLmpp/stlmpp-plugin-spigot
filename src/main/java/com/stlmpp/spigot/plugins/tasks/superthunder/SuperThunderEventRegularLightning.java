package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SuperThunderEventRegularLightning extends SuperThunderEvent {

  public SuperThunderEventRegularLightning(
    @NotNull StlmppPlugin plugin,
    int safeRadius,
    @NotNull Location safeLocation
  ) {
    super(plugin, safeRadius, safeLocation);
  }

  @Override
  public void run() {
    final var world = this.plugin.getWorld();
    if (world == null) {
      return;
    }
    final var lightningLocation = Util.getRandomLocationAroundRandomPlayerWithMinY(world, 55);
    if (lightningLocation == null || Util.isInRadius(lightningLocation, this.safeLocation, this.safeRadius)) {
      return;
    }
    world.strikeLightning(lightningLocation);
  }
}
