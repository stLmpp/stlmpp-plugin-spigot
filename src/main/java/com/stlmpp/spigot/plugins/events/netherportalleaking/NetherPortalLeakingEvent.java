package com.stlmpp.spigot.plugins.events.netherportalleaking;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.tasks.NetherPortalLeakingTask;
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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class NetherPortalLeakingEvent implements Listener {

  public static @Nullable NetherPortalLeakingEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.netherPortalLeakingEnabled)) {
      return null;
    }
    return new NetherPortalLeakingEvent(plugin);
  }

  public static final Set<Material> notAllowedMaterials = new HashSet<>();

  static {
    notAllowedMaterials.addAll(
        List.of(
            Material.CHEST,
            Material.SPONGE,
            Material.CHAIN,
            Material.MELON,
            Material.CAKE,
            Material.END_PORTAL,
            Material.END_PORTAL_FRAME,
            Material.WET_SPONGE,
            Material.GRINDSTONE,
            Material.FURNACE,
            Material.FURNACE_MINECART,
            Material.BLAST_FURNACE,
            Material.ENCHANTING_TABLE,
            Material.BOOKSHELF,
            Material.CUT_SANDSTONE,
            Material.ENDER_CHEST,
            Material.TRAPPED_CHEST,
            Material.BEEHIVE,
            Material.CAMPFIRE));
  }

  public static boolean isValidMaterial(Material material) {
    return (!notAllowedMaterials.contains(material)
        && !Util.isFromNether(material)
        && material != Material.OBSIDIAN
        && ((material.isSolid() && material.isBlock()) || material == Material.WATER));
  }

  public final StlmppPlugin plugin;
  private final int radius;
  private final BehaviorSubject<List<Block>> netherPortalBreak$ =
      BehaviorSubject.createDefault(new ArrayList<>());
  private final Disposable disposable;

  public final Map<NetherPortal, NetherPortalLeakingTask> netherPortals = new HashMap<>();

  private NetherPortalLeakingEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.radius = this.plugin.config.getInt(StlmppPluginConfig.netherPortalLeakingRadius);
    this.plugin.log(
        String.format("Nether portal leaking enabled with a radius of %s", this.radius));
    this.disposable =
        this.netherPortalBreak$
            .debounce(1, TimeUnit.SECONDS)
            .filter(value -> !value.isEmpty())
            .subscribe(
                value -> {
                  this.netherPortalBreak$.onNext(new ArrayList<>());
                  this.checkBrokenPortal(value);
                });
  }

  private void checkBrokenPortal(List<Block> blocks) {
    if (blocks.isEmpty()) {
      return;
    }
    final var world = blocks.getFirst().getWorld();
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
    final var netherPortalBlocks = event.getBlocks().stream().map(BlockState::getBlock).toList();
    if (netherPortalBlocks.isEmpty()) {
      return;
    }
    final var netherPortal = new NetherPortal(world, netherPortalBlocks);
    final var locations = new ArrayList<Pair<Double, Location>>();
    final var radius = Math.max(netherPortal.width, this.radius);
    this.plugin.log(
        String.format("width = %s, radius = %s", netherPortal.width, this.radius), true);
    this.plugin.log(
        String.format(
            "widthZ = %s, widthX = %s",
            netherPortal.boundingBox.getWidthZ(), netherPortal.boundingBox.getWidthX()));
    this.plugin.log(String.format("boundingBox = %s", netherPortal.boundingBox));
    final var centerVector = netherPortal.getCenter();
    final var startingX = centerVector.getBlockX();
    final var startingY = centerVector.getBlockY();
    final var startingZ = centerVector.getBlockZ();
    final var particlesLocation = new ArrayList<Vector>();
    for (int x = startingX - radius; x <= startingX + radius; x++) {
      for (int y = startingY - radius; y <= startingY + radius; y++) {
        for (int z = startingZ - radius; z <= startingZ + radius; z++) {
          final var vector = new Vector(x, y, z);
          if (!vector.isInSphere(centerVector, radius)) {
            continue;
          }
          final var blockAt = world.getBlockAt(x, y, z);
          final var blockAtMaterial = blockAt.getType();
          final double distance = vector.distance(centerVector);
          if (NetherPortalLeakingEvent.isValidMaterial(blockAtMaterial)) {
            locations.add(new Pair<>(vector.distance(centerVector), blockAt.getLocation()));
          }
          final var distanceFloored = (int) Math.ceil(distance);
          if (distanceFloored == radius) {
            particlesLocation.add(vector);
          }
        }
      }
    }
    locations.sort(
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
        });
    final var locationsSize = locations.size();
    final var locationsDeque = new ArrayDeque<Location>();
    for (var index = 0; index < locationsSize; index++) {
      var percent = (double) index / (double) locations.size();
      if (percent < 0.1) {
        percent = 0;
      }
      if (percent > 0.9) {
        percent = 0.9;
      }
      if (Math.random() > percent) {
        locationsDeque.add(locations.get(index).value1);
      }
    }
    this.netherPortals.put(
        netherPortal,
        new NetherPortalLeakingTask(
            this, locationsDeque, world, netherPortal, radius, particlesLocation));
  }

  @EventHandler
  public void onBlockPhysics(BlockPhysicsEvent event) {
    final var block = event.getBlock();
    if (event.getChangedType() == Material.NETHER_PORTAL
        && block.getType() == Material.NETHER_PORTAL
        && block.getWorld().getName().equals(this.plugin.getWorldName())) {
      final var list = this.netherPortalBreak$.getValue();
      if (list == null) {
        return;
      }
      list.add(block);
      this.netherPortalBreak$.onNext(list);
    }
  }
}
