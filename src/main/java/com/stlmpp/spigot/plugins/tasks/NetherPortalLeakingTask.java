package com.stlmpp.spigot.plugins.tasks;

import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortal;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortalLeakingEvent;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.Deque;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

public class NetherPortalLeakingTask extends BukkitRunnable {

  private final Deque<Location> locations;
  private final NetherPortalLeakingEvent netherPortalLeakingEvent;
  private final World world;
  private final NetherPortal netherPortal;
  private final int chanceOfNetherrackFire;

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
    this.chanceOfNetherrackFire =
      this.netherPortalLeakingEvent.plugin.config.getInt(Config.netherPortalLeakingChanceOfNetherrackFire);
    this.runTaskTimer(this.netherPortalLeakingEvent.plugin, 0, /*Tick.fromSeconds(2) TODO*/1);
  }

  @Override
  public void run() {
    if (this.locations.size() == 0) {
      this.netherPortalLeakingEvent.tryCancelTask(this.netherPortal);
      return;
    }
    final var block = this.world.getBlockAt(this.locations.pop());
    final var blockAtMaterial = block.getType();
    if (!NetherPortalLeakingEvent.isValidMaterial(blockAtMaterial)) {
      return;
    }
    final var netherMaterial = Util.convertToNetherMaterial(blockAtMaterial);
    block.setType(netherMaterial);
    if (netherMaterial == Material.NETHERRACK && Chance.of(this.chanceOfNetherrackFire)) {
      final var blockUp = block.getRelative(BlockFace.UP);
      if (blockUp.getType().isAir()) {
        blockUp.setType(Material.FIRE);
      }
    }
  }
}
