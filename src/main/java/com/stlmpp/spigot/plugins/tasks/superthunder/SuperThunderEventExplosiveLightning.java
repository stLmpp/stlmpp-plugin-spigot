package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SuperThunderEventExplosiveLightning implements SuperThunderEvent {

  @Override
  public void run(@NotNull StlmppPlugin plugin, int safeRadius, @NotNull Location safeLocation) {
    final var world = plugin.getWorld();
    if (world == null) {
      return;
    }
    final var lightningLocation = Util.getRandomLocationAroundRandomPlayer(world);
    if (lightningLocation == null || Util.isInRadius(lightningLocation, safeLocation, safeRadius)) {
      return;
    }
    world.strikeLightning(lightningLocation);
    if (!Chance.of(95)) {
      return;
    }
    final var explosionPower = ThreadLocalRandom.current().nextInt(0, 16);
    world.createExplosion(lightningLocation, explosionPower, true, true);
  }
}
