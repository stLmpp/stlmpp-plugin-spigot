package com.stlmpp.spigot.plugins.events.creepycave;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.RandomList;
import com.stlmpp.spigot.plugins.utils.Rng;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public class CreepyCaveTask extends BukkitRunnable {

  @Nullable
  public static CreepyCaveTask register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.creepyCavesEnabled)) {
      return null;
    }
    return new CreepyCaveTask(plugin);
  }

  private CreepyCaveTask(StlmppPlugin plugin) {
    this.plugin = plugin;
    chance = plugin.config.getDouble(StlmppPluginConfig.creepyCavesSoundChance);
    cowChance = plugin.config.getDouble(StlmppPluginConfig.creepyCavesCowChance);
    this.runTaskTimer(plugin, 0, Tick.fromSeconds(10));
  }

  private final StlmppPlugin plugin;
  private final double chance;
  private final double cowChance;

  @Override
  public void run() {
    if (plugin.getWorld() == null
        || plugin.getWorld().getPlayerCount() == 0
        || !Rng.chance(chance)) {
      return;
    }
    final var player = new RandomList<>(plugin.getWorld().getPlayers()).next();

    if (player.getLocation().getBlockY() >= 20) {
      return;
    }

    final var soundLocation =
        player.getLocation().add(Rng.nextInt(-1, 1), Rng.nextInt(-1, 1), Rng.nextInt(-1, 1));
    player.getWorld().playSound(soundLocation, Sound.AMBIENT_CAVE, 1, 1);

    if (!Rng.chance(cowChance)) {
      return;
    }

    final var possibleCowLocation =
        player.getLocation().add(player.getLocation().getDirection().normalize().multiply(-2));
    possibleCowLocation.add(0, 1, 0);
    possibleCowLocation.setY(Util.getFloor(possibleCowLocation) + 1);
    if (possibleCowLocation.getBlock().isSolid()
        || possibleCowLocation.clone().add(0, 1, 0).getBlock().isSolid()) {
      return;
    }
    final var cow = player.getWorld().spawn(possibleCowLocation, Cow.class);
    cow.setAdult();
  }
}
