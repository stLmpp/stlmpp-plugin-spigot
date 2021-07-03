package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import java.util.Stack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class NetherPortalLeakingEvent implements Listener {

  private final StlmppPlugin plugin;

  public NetherPortalLeakingEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
  }

  @EventHandler
  public void onPortalCreate(PortalCreateEvent event) {
    final var world = event.getWorld();
    if (!world.getName().equals(this.plugin.getWorldName())) {
      return;
    }
    final var initialBlock = event.getBlocks().get(0);
    if (initialBlock == null) {
      return;
    }
    final var replacementBlocks = new Stack<Block>();
    for (var x = -15; x < 15; x++) {
      for (var y = -5; y < 5; y++) {
        for (var z = -15; z < 15; z++) {
          final var blockAt = world.getBlockAt(initialBlock.getLocation().add(x, y, z));
          if (blockAt.getType() == Material.OBSIDIAN) {
            continue;
          }
          final var blockAtMaterial = blockAt.getType();
          if (blockAtMaterial.isSolid() && blockAtMaterial.isBlock()) {
            replacementBlocks.add(blockAt);
          }
        }
      }
    }
    Bukkit.broadcastMessage("SIZE " + replacementBlocks.size());
    if (replacementBlocks.size() == 0) {
      return;
    }
    new NetherPortalLeakingTask(this.plugin, replacementBlocks);
  }
}
