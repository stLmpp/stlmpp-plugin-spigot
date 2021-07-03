package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.Stack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class NetherPortalLeakingTask extends BukkitRunnable {

  private final Stack<Location> locations;
  private final World world;

  public NetherPortalLeakingTask(StlmppPlugin plugin, Stack<Location> locations, World world) {
    this.locations = locations;
    this.world = world;
    this.runTaskTimer(plugin, 0, 5);
  }

  @Override
  public void run() {
    final var block = this.world.getBlockAt(this.locations.pop());
    final var blockType = block.getType();
    if (!blockType.isSolid() || !blockType.isBlock()) {
      return;
    }
    Bukkit.broadcastMessage("Replacing block at X " + block.getX() + " Y " + block.getY() + " Z " + block.getZ());
    block.setType(Util.convertToNetherMaterial(block.getType()));
    if (this.locations.size() == 0) {
      this.cancel();
    }
  }
}
