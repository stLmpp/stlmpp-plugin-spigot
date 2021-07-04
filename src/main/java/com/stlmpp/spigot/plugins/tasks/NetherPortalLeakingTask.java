package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.Deque;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class NetherPortalLeakingTask extends BukkitRunnable {

  private final Deque<Location> locations;
  private final World world;

  public NetherPortalLeakingTask(StlmppPlugin plugin, Deque<Location> locations, World world) {
    this.locations = locations;
    this.world = world;
    this.runTaskTimer(plugin, 0, Tick.fromSeconds(2));
  }

  @Override
  public void run() {
    final var block = this.world.getBlockAt(this.locations.pop());
    final var blockAtMaterial = block.getType();
    if ((!blockAtMaterial.isBlock() || !blockAtMaterial.isSolid()) && blockAtMaterial != Material.WATER) {
      return;
    }
    Bukkit.broadcastMessage("Replacing block at X " + block.getX() + " Y " + block.getY() + " Z " + block.getZ());
    block.setType(Util.convertToNetherMaterial(block.getType()));
    if (this.locations.size() == 0) {
      this.cancel();
    }
  }
}
