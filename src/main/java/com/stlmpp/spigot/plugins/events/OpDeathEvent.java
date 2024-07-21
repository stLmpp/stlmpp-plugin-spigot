package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.Nullable;

public class OpDeathEvent implements Listener {

  public static @Nullable OpDeathEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.opDeathEventEnabled)) {
      return null;
    }
    return new OpDeathEvent(plugin);
  }

  private OpDeathEvent(StlmppPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.lightningChance = plugin.config.getDouble(StlmppPluginConfig.opDeathEventLightningChance);
    this.explosionChance = plugin.config.getDouble(StlmppPluginConfig.opDeathEventExplosionChance);
    plugin.log(
        String.format(
            "Op Death Event activated with %s chance of lightning and %s chance of explosion",
            this.lightningChance, this.explosionChance));
  }

  private final double lightningChance;
  private final double explosionChance;

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeathEvent(PlayerDeathEvent event) {
    if (!event.getPlayer().isOp() || !Chance.of(this.lightningChance)) {
      return;
    }
    final var location = event.getPlayer().getLocation().clone();
    location.setY(Util.getFloor(event.getPlayer().getWorld(), event.getPlayer().getLocation()));
    event.getPlayer().getWorld().strikeLightning(location);
    if (!Chance.of(this.explosionChance)) {
      return;
    }
    event
        .getPlayer()
        .getWorld()
        .createExplosion(location, Util.randomFloat(0.0f, 16.0f), true, true);
  }
}
