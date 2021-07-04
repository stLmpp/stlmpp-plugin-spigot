package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface SuperThunderEvent {
  void run(@NotNull StlmppPlugin plugin, int safeRadius, @NotNull Location safeLocation);
}
