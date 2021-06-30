package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public class SuperThunderEventLightningCreeper implements SuperThunderEvent {

  @Override
  public void run(@NotNull StlmppPlugin plugin) {
    final var world = plugin.getWorld();
    if (world == null) {
      return;
    }
    final var location = plugin.strikeLightningRandomPlayer(world);
    if (location == null) {
      return;
    }
    final var creeper = (Creeper) world.spawnEntity(location.add(0, 1, 0), EntityType.CREEPER);
    creeper.setPowered(true);
  }
}
