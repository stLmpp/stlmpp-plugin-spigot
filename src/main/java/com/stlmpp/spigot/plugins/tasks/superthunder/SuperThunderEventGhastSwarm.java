package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SuperThunderEventGhastSwarm implements SuperThunderEvent {

  private void spawnGhastForPlayer(World world, Player player, int safeRadius, @NotNull Location safeLocation) {
    if (Chance.of(50)) {
      return;
    }
    final var playerLocation = player.getLocation();
    if (Util.isInRadius(playerLocation, safeLocation, safeRadius)) {
      return;
    }
    final var ghasts = ThreadLocalRandom.current().nextInt(0, 4);
    for (var i = 0; i < ghasts; i++) {
      final var ghastX = playerLocation.getBlockX() + ThreadLocalRandom.current().nextInt(-10, 10);
      final var ghastY = playerLocation.getBlockY() + ThreadLocalRandom.current().nextInt(10, 20);
      final var ghastZ = playerLocation.getBlockZ() + ThreadLocalRandom.current().nextInt(-10, 10);
      world.spawnEntity(new Location(world, ghastX, ghastY, ghastZ), EntityType.GHAST);
    }
  }

  @Override
  public void run(@NotNull StlmppPlugin plugin, int safeRadius, @NotNull Location safeLocation) {
    final var world = plugin.getWorld();
    if (world == null) {
      return;
    }
    world.getPlayers().forEach(player -> this.spawnGhastForPlayer(world, player, safeRadius, safeLocation));
  }
}
