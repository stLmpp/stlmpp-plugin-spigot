package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import com.stlmpp.spigot.plugins.utils.WeightedRandomCollection;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public class EggRandomEffectEvent implements Listener {

  public static @Nullable EggRandomEffectEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.eggRandomEventChance)) {
      return null;
    }
    return new EggRandomEffectEvent(plugin);
  }

  private EggRandomEffectEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    this.chance = this.plugin.config.getDouble(StlmppPluginConfig.eggRandomEventChance);
    this.plugin.log(
        String.format("Egg Random Effect activated with %s chance of happening", this.chance));
    this.entitiesToSpawn.add(1.0, EntityType.WARDEN);
    this.entitiesToSpawn.add(2.0, EntityType.WITHER);
    this.entitiesToSpawn.add(4.0, EntityType.RAVAGER);
    this.entitiesToSpawn.add(8.0, EntityType.SHULKER);
    this.entitiesToSpawn.add(8.0, EntityType.EVOKER);
    this.entitiesToSpawn.add(16.0, EntityType.BLAZE);
    this.entitiesToSpawn.add(16.0, EntityType.VEX);
    this.entitiesToSpawn.add(16.0, EntityType.VINDICATOR);
    this.entitiesToSpawn.add(16.0, EntityType.GHAST);
    this.entitiesToSpawn.add(64.0, EntityType.WITHER_SKELETON);
    this.entitiesToSpawn.add(64.0, EntityType.PILLAGER);

    this.quantitiesToSpawn.add(1.0, 8);
    this.quantitiesToSpawn.add(2.0, 7);
    this.quantitiesToSpawn.add(4.0, 6);
    this.quantitiesToSpawn.add(8.0, 5);
    this.quantitiesToSpawn.add(64.0, 4);
    this.quantitiesToSpawn.add(64.0, 3);
    this.quantitiesToSpawn.add(64.0, 2);
    this.quantitiesToSpawn.add(64.0, 1);
  }

  private final WeightedRandomCollection<EntityType> entitiesToSpawn =
      new WeightedRandomCollection<>();
  private final WeightedRandomCollection<Integer> quantitiesToSpawn =
      new WeightedRandomCollection<>();
  private final double chance;

  private final StlmppPlugin plugin;

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
    final var damager = event.getDamager();
    final var entity = event.getEntity();
    if (!(damager instanceof Egg)
        || !(entity instanceof Player player)
        || !Chance.of(this.chance)) {
      return;
    }
    final var spawnQuantity = this.quantitiesToSpawn.next();
    for (int index = 0; index < spawnQuantity; index++) {
      final var entityToSpawn = this.entitiesToSpawn.next();
      new BukkitRunnable() {
        @Override
        public void run() {
          final var randomLocation = Util.getRandomLocationAroundPlayer(player);
          final var lightningLocation = randomLocation.clone();
          lightningLocation.setY(Util.getFloor(event.getEntity().getWorld(), lightningLocation));
          event.getEntity().getWorld().strikeLightning(lightningLocation);
          event.getEntity().getWorld().spawnEntity(damager.getLocation(), entityToSpawn);
        }
      }.runTaskLater(this.plugin, Tick.fromSeconds((index + 0.1) * index * 0.15));
    }
  }
}
