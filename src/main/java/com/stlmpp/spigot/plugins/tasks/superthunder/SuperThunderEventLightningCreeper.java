package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class SuperThunderEventLightningCreeper extends SuperThunderEvent {

  public SuperThunderEventLightningCreeper(
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
    final var location = Util.getRandomLocationAroundRandomPlayerWithMinY(world, 55);
    if (location == null || Util.isInRadius(location, this.safeLocation, this.safeRadius)) {
      return;
    }
    world.strikeLightning(location);
    final var creeper = (Creeper) world.spawnEntity(location.add(0, 1, 0), EntityType.CREEPER);
    creeper.setPowered(true);
  }
}
