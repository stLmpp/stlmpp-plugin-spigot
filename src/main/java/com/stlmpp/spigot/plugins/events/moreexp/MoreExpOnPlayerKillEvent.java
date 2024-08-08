package com.stlmpp.spigot.plugins.events.moreexp;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MoreExpOnPlayerKillEvent implements Listener {

  @Nullable
  public static MoreExpOnPlayerKillEvent register(@NotNull StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.moreExpEnabled)) {
      return null;
    }
    return new MoreExpOnPlayerKillEvent(plugin);
  }

  private MoreExpOnPlayerKillEvent(StlmppPlugin plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    plugin.log("More Exp enabled");
  }

  @EventHandler
  public void onKill(EntityDeathEvent event) {
    if (!event.getEntity().getType().equals(EntityType.PLAYER)) {
      return;
    }
    Player damager = event.getEntity().getKiller();
    if (damager == null) {
      return;
    }
    // TODO
  }
}
