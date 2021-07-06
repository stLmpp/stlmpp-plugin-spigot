package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.utils.Config;
import org.bukkit.Material;

public class StlmppPluginConfig {

  public StlmppPluginConfig(StlmppPlugin plugin) {
    final var config = plugin.config;
    config.addDefault(Config.netherLightningChance, 30d);
    config.addDefault(Config.netherLightningEnabled, true);
    config.addDefault(Config.netherLightningChancePerSecond, 15);
    config.addDefault(Config.netherLightningExplosionChance, 25d);
    config.addDefault(Config.worldNether, "world_nether");
    config.addDefault(Config.autoSeedEnabled, true);
    config.addDefault(Config.autoSeedMaxBlocks, 60);
    final var autoSeedAllowedSeedList = new String[] {
      Material.WHEAT_SEEDS.name(),
      Material.MELON_SEEDS.name(),
      Material.BEETROOT_SEEDS.name(),
      Material.PUMPKIN_SEEDS.name(),
      Material.POTATO.name(),
      Material.CARROT.name(),
    };
    config.addDefault(Config.autoSeedAllowedSeedList, autoSeedAllowedSeedList);
    config.addDefault(Config.superThunderEnabled, true);
    config.addDefault(Config.superThunderChance, 20d);
    config.addDefault(Config.superThunderSecondsIntervalEvents, 15);
    config.addDefault(Config.superThunderEventChance, 40d);
    config.addDefault(Config.superThunderLightningWeight, 250d);
    config.addDefault(Config.superThunderExplosiveLightningWeight, 5d);
    config.addDefault(Config.superThunderLightningCreeperWeight, 2d);
    config.addDefault(Config.superThunderGhastSwarmWeight, 2d);
    config.addDefault(Config.superThunderSafeCoordsRadius, 250);
    config.addDefault(Config.superThunderSafeCoords, "0 0 0");
    config.addDefault(Config.world, "world");
    config.addDefault(Config.caveInEnabled, true);
    config.addDefault(Config.caveInMaxY, 30);
    config.addDefault(Config.caveInMinHeight, 4);
    config.addDefault(Config.caveInMinWidth, 4);
    final var caveInImmunityItems = new String[] {
      Material.RABBIT_FOOT.name(),
      Material.CLOCK.name(),
      Material.COMPASS.name(),
    };
    config.addDefault(Config.caveInImmunityItems, caveInImmunityItems);
    config.addDefault(Config.caveInChance, 2d);
    final var caveInBlocks = new String[] { Material.STONE.name(), Material.COBBLESTONE.name() };
    config.addDefault(Config.caveInBlocks, caveInBlocks);
    config.addDefault(Config.caveInMaxWidth, 10);
    config.addDefault(Config.caveInMaxHeight, 10);
    config.addDefault(Config.tpLightningEnabled, true);
    config.addDefault(Config.tpLightningChance, 75d);
    config.addDefault(Config.tpLightningNetherBlocksRadius, 4);
    config.addDefault(Config.tpLightningExplosionChance, 0.25d);
    config.addDefault(Config.netherPortalLeakingEnabled, true);
    config.addDefault(Config.netherPortalLeakingRadius, 15);
    config.addDefault(Config.netherPortalLeakingChanceOfNetherrackFire, 0.5d);
    config.addDefault(Config.netherPortalLeakingKnockbackPower, 0.5d);
    config.addDefault(Config.devMode, false);
    config.options().copyDefaults(true);
    plugin.saveConfig();
    plugin.isDevMode = config.getBoolean(Config.devMode);
  }
}
