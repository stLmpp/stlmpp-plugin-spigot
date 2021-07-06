package com.stlmpp.spigot.plugins.events.netherportalleaking;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.tasks.NetherPortalLeakingTask;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Pair;
import com.stlmpp.spigot.plugins.utils.Util;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.world.PortalCreateEvent;

public class NetherPortalLeakingEvent implements Listener {

  public static boolean isValidMaterial(Material material) {
    return (
      !Util.isFromNether(material) &&
      material != Material.OBSIDIAN &&
      ((material.isSolid() && material.isBlock()) || material == Material.WATER)
    );
  }

  public final StlmppPlugin plugin;
  private final int radius;
  private final BehaviorSubject<List<Block>> netherPortalBreak$ = BehaviorSubject.createDefault(new ArrayList<>());
  private final Disposable disposable;

  public final Map<NetherPortal, NetherPortalLeakingTask> netherPortals = new HashMap<>();

  public NetherPortalLeakingEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.radius = this.plugin.config.getInt(Config.netherPortalLeakingRadius);
    this.disposable =
      this.netherPortalBreak$.debounce(1, TimeUnit.SECONDS)
        .filter(value -> value.size() != 0)
        .subscribe(
          value -> {
            this.netherPortalBreak$.onNext(new ArrayList<>());
            this.checkBrokenPortal(value);
          }
        );
  }

  private void checkBrokenPortal(List<Block> blocks) {
    if (blocks.size() == 0) {
      return;
    }
    final var world = blocks.get(0).getWorld();
    final var possibleNetherPortal = new NetherPortal(world, blocks);
    this.tryCancelTask(possibleNetherPortal);
  }

  public void tryCancelTask(NetherPortal netherPortal) {
    final var possibleTask = this.netherPortals.remove(netherPortal);
    if (possibleTask == null) {
      return;
    }
    possibleTask.cancel();
  }

  public void destroy() {
    this.disposable.dispose();
  }

  @EventHandler
  public void onPortalCreate(PortalCreateEvent event) {
    final var world = event.getWorld();
    if (!world.getName().equals(this.plugin.getWorldName())) {
      return;
    }
    final var netherPortalBlocks = new ArrayList<Block>();
    for (BlockState blockState : event.getBlocks()) {
      if (blockState.getType() == Material.NETHER_PORTAL) {
        netherPortalBlocks.add(blockState.getBlock());
      }
    }
    final var netherPortal = new NetherPortal(world, netherPortalBlocks);
    final var locations = new ArrayDeque<Pair<Double, Location>>();
    final var radius = Math.max(netherPortal.width, this.radius);
    final var initialBlockLocation = netherPortal.getCenter();
    final var startingX = initialBlockLocation.getBlockX();
    final var startingY = initialBlockLocation.getBlockY();
    final var startingZ = initialBlockLocation.getBlockZ();
    for (int x = startingX - radius; x <= startingX + radius; x++) {
      for (int y = startingY - radius; y <= startingY + radius; y++) {
        for (int z = startingZ - radius; z <= startingZ + radius; z++) {
          final var distance = Math.sqrt(
            Math.pow(startingX - x, 2) + Math.pow(startingY - y, 2) + Math.pow(startingZ - z, 2)
          );
          if (distance <= radius) {
            final var blockAt = world.getBlockAt(x, y, z);
            final var blockAtMaterial = blockAt.getType();
            if (NetherPortalLeakingEvent.isValidMaterial(blockAtMaterial)) {
              locations.add(new Pair<>(distance, blockAt.getLocation()));
            }
          }
        }
      }
    }
    if (locations.size() == 0) {
      return;
    }
    final var locationsSorted = locations
      .stream()
      .sorted(
        (pairA, pairB) -> {
          final var distanceA = pairA.value0;
          final var distanceB = pairB.value0;
          if (distanceA.equals(distanceB)) {
            return 0;
          } else if (distanceA < distanceB) {
            return -1;
          } else {
            return 1;
          }
        }
      )
      .map(pair -> pair.value1)
      .toList();
    final var locationsSortedDistributed = new ArrayDeque<Location>();
    for (var index = 0; index < locationsSorted.size(); index++) {
      double percent = (double) index / (double) locationsSorted.size();
      if (percent < 0.1) {
        percent = 0;
      }
      if (percent > 0.9) {
        percent = 0.9;
      }
      if (Math.random() > percent) {
        locationsSortedDistributed.add(locationsSorted.get(index));
      }
    }
    this.netherPortals.put(
        netherPortal,
        new NetherPortalLeakingTask(this, locationsSortedDistributed, world, netherPortal)
      );
  }

  @EventHandler
  public void onBlockPhysics(BlockPhysicsEvent event) {
    final var block = event.getBlock();
    if (
      event.getChangedType() == Material.NETHER_PORTAL &&
      block.getType() == Material.NETHER_PORTAL &&
      block.getWorld().getName().equals(this.plugin.getWorldName())
    ) {
      final var list = this.netherPortalBreak$.getValue();
      if (list == null) {
        return;
      }
      list.add(block);
      this.netherPortalBreak$.onNext(list);
    }
  }
}