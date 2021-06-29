package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Config;
import org.bukkit.scheduler.BukkitRunnable;

public class SuperThunderTask extends BukkitRunnable {

  private final StlmppPlugin plugin;

  public SuperThunderTask(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.runTaskTimer(this.plugin, 0, this.plugin.config.getInt(Config.superThunderSecondsIntervalEvents));
  }

  public void destroy() {}

  @Override
  public void run() {}
}
