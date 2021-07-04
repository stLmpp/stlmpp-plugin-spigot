package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class SuperThunderEventLightningCreeper implements SuperThunderEvent {

  @Override
  public void run(@NotNull StlmppPlugin plugin, int safeRadius, @NotNull Location safeLocation) {
    final var world = plugin.getWorld();
    if (world == null) {
      return;
    }
    final var location = Util.getRandomLocationAroundRandomPlayer(world);
    if (location == null || Util.isInRadius(location, safeLocation, safeRadius)) {
      return;
    }
    world.strikeLightning(location);
    final var creeper = (Creeper) world.spawnEntity(location.add(0, 1, 0), EntityType.CREEPER);
    creeper.setPowered(true);
  }
}
