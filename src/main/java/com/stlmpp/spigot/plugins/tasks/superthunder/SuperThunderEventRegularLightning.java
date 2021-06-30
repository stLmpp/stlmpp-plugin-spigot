package com.stlmpp.spigot.plugins.tasks.superthunder;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import org.jetbrains.annotations.NotNull;

public class SuperThunderEventRegularLightning implements SuperThunderEvent {

  @Override
  public void run(@NotNull StlmppPlugin plugin) {
    final var world = plugin.getWorld();
    if (world == null) {
      return;
    }
    plugin.strikeLightningRandomPlayer(world);
  }
}
