package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.tasks.superthunder.SuperThunderTask;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.jetbrains.annotations.Nullable;

public class SuperThunderCheckEvent implements Listener {

  public final StlmppPlugin plugin;

  @Nullable
  private SuperThunderTask superThunderTask = null;

  public SuperThunderCheckEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  public void activateSuperThunderEvent() {
    this.deactivateSuperThunderEvent();
    if (Chance.of(this.plugin.config.getDouble(Config.superThunderChance))) {
      this.superThunderTask = new SuperThunderTask(this);
    }
  }

  public void deactivateSuperThunderEvent() {
    if (this.superThunderTask != null) {
      this.superThunderTask.cancel();
      this.superThunderTask = null;
    }
  }

  @EventHandler
  public void onThunderChange(ThunderChangeEvent event) {
    if (event.toThunderState()) {
      this.activateSuperThunderEvent();
    } else {
      this.deactivateSuperThunderEvent();
    }
  }
}
