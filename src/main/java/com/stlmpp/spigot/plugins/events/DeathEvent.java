package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.RandomList;
import com.stlmpp.spigot.plugins.utils.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DeathEvent implements Listener {

  public static @Nullable DeathEvent register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.deathEventEnabled)) {
      return null;
    }
    return new DeathEvent(plugin);
  }

  private DeathEvent(StlmppPlugin plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.chance = plugin.config.getDouble(StlmppPluginConfig.deathEventChance);
    this.lightningChance = plugin.config.getDouble(StlmppPluginConfig.deathEventLightningChance);
    this.explosionChance = plugin.config.getDouble(StlmppPluginConfig.deathEventExplosionChance);
    plugin.log(
        String.format(
            "Op Death Event activated with %s chance of lightning and %s chance of explosion",
            this.chance, this.explosionChance));
    this.structures =
        new RandomList<>(
            List.of(
                Structure.PILLAGER_OUTPOST,
                Structure.MINESHAFT,
                Structure.MINESHAFT_MESA,
                Structure.MANSION,
                Structure.JUNGLE_PYRAMID,
                Structure.DESERT_PYRAMID,
                Structure.IGLOO,
                Structure.SHIPWRECK,
                Structure.SHIPWRECK_BEACHED,
                Structure.SWAMP_HUT,
                Structure.STRONGHOLD,
                Structure.MONUMENT,
                Structure.OCEAN_RUIN_COLD,
                Structure.OCEAN_RUIN_WARM,
                Structure.BURIED_TREASURE,
                Structure.VILLAGE_PLAINS,
                Structure.VILLAGE_DESERT,
                Structure.VILLAGE_SAVANNA,
                Structure.VILLAGE_SNOWY,
                Structure.VILLAGE_TAIGA,
                Structure.RUINED_PORTAL,
                Structure.RUINED_PORTAL_DESERT,
                Structure.RUINED_PORTAL_JUNGLE,
                Structure.RUINED_PORTAL_SWAMP,
                Structure.RUINED_PORTAL_MOUNTAIN,
                Structure.RUINED_PORTAL_OCEAN,
                Structure.RUINED_PORTAL_NETHER,
                Structure.ANCIENT_CITY,
                Structure.TRAIL_RUINS,
                Structure.TRIAL_CHAMBERS));
    this.messages =
        new RandomList<>(
            List.of(
                "%s morreu e seus itens foram parar na coordenada %s %s %s ... espero que um dia voce consiga recuperar",
                "%s esta morto... Seus itens estao na coordenada %s %s %s"));
  }

  private final StlmppPlugin plugin;
  private final double chance;
  private final double lightningChance;
  private final double explosionChance;
  private final RandomList<Structure> structures;
  private final RandomList<String> messages;

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDeathEvent(PlayerDeathEvent event) {
    if (event.getDrops().isEmpty()
        || !event.getPlayer().getWorld().getName().equals(this.plugin.getWorldName())
        || !Chance.of(this.chance)) {
      return;
    }

    new BukkitRunnable() {

      @Override
      public void run() {
        Location location = null;
        Structure structure = null;
        int attempt = 0;

        while (location == null && attempt < 8) {
          attempt++;
          structure = structures.next();
          final var structureSearch =
              event
                  .getPlayer()
                  .getWorld()
                  .locateNearestStructure(
                      event.getPlayer().getLocation(),
                      structure,
                      2048,
                      ThreadLocalRandom.current().nextBoolean());
          if (structureSearch != null) {
            location = structureSearch.getLocation();
          }
        }

        if (location == null) {
          return;
        }

        plugin.log(String.format("Structure = %s | Location = %s", structure.key(), location));

        final var itemsLocation =
            Util.getRandomLocationAroundLocation(
                location, new BoundingBox(-30, 0, -30, 30, 90, 30));
        itemsLocation.setY(Util.getFloor(itemsLocation));

        final var chest1 = itemsLocation.getBlock();
        final var chest2 = itemsLocation.clone().add(1, 0, 0).getBlock();
        chest1.setType(Material.CHEST);
        chest2.setType(Material.CHEST);

        final var chest1Data = chest1.getBlockData();
        final var chest2Data = chest2.getBlockData();

        if (chest1Data instanceof Chest c1d) {
          c1d.setType(Chest.Type.LEFT);
        }

        if (chest2Data instanceof Chest c2d) {
          c2d.setType(Chest.Type.RIGHT);
        }

        chest1.setBlockData(chest1Data);
        chest2.setBlockData(chest2Data);

        final var state = chest1.getState();

        plugin.log(String.format("is chest %s", state instanceof org.bukkit.block.Chest), true);

        if (state instanceof org.bukkit.block.Chest c1
            && c1.getInventory().getHolder() instanceof DoubleChest doubleChest) {
          for (ItemStack item : event.getDrops()) {
            if (item == null) {
              continue;
            }
            plugin.log(String.format("item = %s", item));
            doubleChest.getInventory().addItem(item);
          }
        }

        plugin
            .getServer()
            .broadcast(
                Component.text(
                    String.format(
                        messages.next(),
                        event.getPlayer().getName(),
                        itemsLocation.getBlockX(),
                        itemsLocation.getBlockY(),
                        itemsLocation.getBlockZ())));

        if (!Chance.of(lightningChance)) {
          return;
        }

        final var lightiningLocation = event.getPlayer().getLocation().clone();
        lightiningLocation.setY(Util.getFloor(event.getPlayer().getLocation()));
        event.getPlayer().getWorld().strikeLightning(lightiningLocation);
        if (!Chance.of(explosionChance)) {
          return;
        }
        event
            .getPlayer()
            .getWorld()
            .createExplosion(lightiningLocation, Util.randomFloat(0.0f, 16.0f), true, true);
      }
    }.runTask(this.plugin);
  }
}
