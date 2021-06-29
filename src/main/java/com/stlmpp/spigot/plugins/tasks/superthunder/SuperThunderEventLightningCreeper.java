package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;

public class SuperThunderEventLightningCreeper implements SuperThunderEvent {

  @Override
  public void run(StlmppPlugin plugin) {
    var world = Bukkit.getWorld(plugin.getWorldName());
    if (world == null) {
      return;
    }
    var location = plugin.strikeLightningRandomPlayer(world);
    if (location == null) {
      return;
    }
    var creeper = (Creeper) world.spawnEntity(location, EntityType.CREEPER);
    creeper.setPowered(true);
  }
}
