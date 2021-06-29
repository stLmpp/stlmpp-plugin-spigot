package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.Bukkit;

public class SuperThunderEventExplosiveLightning implements SuperThunderEvent {

  @Override
  public void run(StlmppPlugin plugin) {
    var world = Bukkit.getWorld(plugin.getWorldName());
    if (world == null) {
      return;
    }
    plugin.strikeLightningRandomPlayerWithChanceOfExplosion(world, 95);
  }
}
