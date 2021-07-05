package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortal;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortalLeakingEvent;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.Deque;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class NetherPortalLeakingTask extends BukkitRunnable {

  private final Deque<Location> locations;
  private final NetherPortalLeakingEvent netherPortalLeakingEvent;
  private final World world;
  private final NetherPortal netherPortal;

  public NetherPortalLeakingTask(
    NetherPortalLeakingEvent netherPortalLeakingEvent,
    Deque<Location> locations,
    World world,
    NetherPortal netherPortal
  ) {
    this.netherPortalLeakingEvent = netherPortalLeakingEvent;
    this.locations = locations;
    this.world = world;
    this.netherPortal = netherPortal;
    this.runTaskTimer(this.netherPortalLeakingEvent.plugin, 0, Tick.fromSeconds(2));
  }

  @Override
  public void run() {
    final var block = this.world.getBlockAt(this.locations.pop());
    final var blockAtMaterial = block.getType();
    if (!NetherPortalLeakingEvent.isValidMaterial(blockAtMaterial)) {
      return;
    }
    Bukkit.broadcastMessage("Replacing block at X " + block.getX() + " Y " + block.getY() + " Z " + block.getZ());
    block.setType(Util.convertToNetherMaterial(block.getType()));
    if (this.locations.size() == 0) {
      this.netherPortalLeakingEvent.tryCancelTask(this.netherPortal);
    }
  }
}
