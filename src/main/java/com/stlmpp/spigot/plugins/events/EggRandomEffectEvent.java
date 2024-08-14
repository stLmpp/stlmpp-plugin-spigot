package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EggRandomEffectEvent implements Listener {

  public static @Nullable EggRandomEffectEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.eggRandomEventEnabled)) {
      return null;
    }
    return new EggRandomEffectEvent(plugin);
  }

  private EggRandomEffectEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    chance = plugin.config.getDouble(StlmppPluginConfig.eggRandomEventChance);
    plugin.log(String.format("Egg Random Effect activated with %s chance of happening", chance));
    entitiesToSpawn
        .add(1.0, EntityType.WARDEN)
        .add(2.0, EntityType.WITHER)
        .add(4.0, EntityType.RAVAGER)
        .add(4.0, EntityType.SILVERFISH)
        .add(4.0, EntityType.ENDERMITE)
        .add(6.0, EntityType.EVOKER)
        .add(10.0, EntityType.SHULKER)
        .add(16.0, EntityType.BLAZE)
        .add(16.0, EntityType.VINDICATOR)
        .add(16.0, EntityType.GHAST)
        .add(20.0, EntityType.SPIDER)
        .add(36.0, EntityType.WITHER_SKELETON)
        .add(36.0, EntityType.PILLAGER);

    quantitiesToSpawn
        .add(0.25, 12)
        .add(1.0, 10)
        .add(2.0, 8)
        .add(4.0, 7)
        .add(8.0, 6)
        .add(16.0, 5)
        .add(28.0, 4)
        .add(32.0, 3)
        .add(48.0, 2)
        .add(52.0, 1);
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
        || !(entity instanceof LivingEntity livingEntity)
        || entity.getLocation().getY() < 62
        || !Rng.chance(chance)) {
      return;
    }
    final var spawnQuantity = quantitiesToSpawn.next();
    for (int index = 0; index < spawnQuantity; index++) {
      final var entityToSpawn = entitiesToSpawn.next();
      final var randomLocation =
          Optional.of(livingEntity.getLocation())
              .map(
                  location ->
                      Util.getRandomLocationAroundLocation(
                          location, new BoundingBox(-10, 0, -10, 11, 30, 11)))
              .map(Util::setFloor)
              .get();
      final var spawnLocation = randomLocation.clone();
      spawnLocation.add(0, 5, 0);
      final var seconds = (index + 0.1) * index * 0.50;
      plugin.runLater(
          Tick.fromSeconds(seconds),
          () -> {
            plugin.log(String.format("Spawning lightning at %s", randomLocation), true);
            event.getEntity().getWorld().strikeLightning(randomLocation);
          });
      plugin.runLater(
          Tick.fromSeconds(seconds + 0.5),
          () -> {
            plugin.log(String.format("Spawning %s at %s", entityToSpawn, spawnLocation), true);
            final var entitySpawned =
                event.getEntity().getWorld().spawnEntity(spawnLocation, entityToSpawn);
            try {
              final var source = event.getDamageSource().getCausingEntity();
              if (entitySpawned instanceof Monster monster
                  && source instanceof LivingEntity damagerEntity) {
                monster.setTarget(damagerEntity);
              }
            } catch (Exception ignored) {

            }
          });
    }
  }
}
