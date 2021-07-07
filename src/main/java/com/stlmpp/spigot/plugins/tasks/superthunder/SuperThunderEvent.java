package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public abstract class SuperThunderEvent {

  protected final StlmppPlugin plugin;
  protected final int safeRadius;
  protected final Location safeLocation;

  public SuperThunderEvent(@NotNull StlmppPlugin plugin, int safeRadius, @NotNull Location safeLocation) {
    this.plugin = plugin;
    this.safeRadius = safeRadius;
    this.safeLocation = safeLocation;
  }

  abstract void run();
}
