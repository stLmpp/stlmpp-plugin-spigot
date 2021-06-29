package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;

public class ThunderCheckEvent implements Listener {

  private final StlmppPlugin plugin;

  public ThunderCheckEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  @EventHandler
  public void onThunderChange(ThunderChangeEvent event) {
    if (event.toThunderState()) {
      this.plugin.activateThunderEvent();
    } else {
      this.plugin.deactivateThunderEvent();
    }
  }
}
