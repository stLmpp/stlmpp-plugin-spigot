package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.utils.Config;
import org.bukkit.Material;

public class StlmppPluginConfig {

  public StlmppPluginConfig(StlmppPlugin plugin) {
    final var config = plugin.config;
    config.addDefault(Config.netherLightningChance, 40);
    config.addDefault(Config.netherLightningEnabled, true);
    config.addDefault(Config.netherLightningChancePerSecond, 10);
    config.addDefault(Config.netherLightningExplosionChance, 50);
    config.addDefault(Config.worldNether, "world_nether");
    config.addDefault(Config.autoSeedEnabled, true);
    config.addDefault(Config.autoSeedMaxBlocks, 40);
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
    config.addDefault(Config.superThunderChance, 20);
    config.addDefault(Config.superThunderSecondsIntervalEvents, 2);
    config.addDefault(Config.superThunderEventChance, 50);
    config.addDefault(Config.superThunderLightningWeight, 20d);
    config.addDefault(Config.superThunderExplosiveLightningWeight, 10d);
    config.addDefault(Config.superThunderLightningCreeperWeight, 5d);
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
    config.addDefault(Config.caveInChance, 3);
    final var caveInBlocks = new String[] { Material.STONE.name(), Material.COBBLESTONE.name() };
    config.addDefault(Config.caveInBlocks, caveInBlocks);
    config.addDefault(Config.caveInMaxWidth, 10);
    config.addDefault(Config.caveInMaxHeight, 10);
    config.addDefault(Config.devMode, false);
    config.options().copyDefaults(true);
    plugin.saveConfig();
    plugin.isDevMode = config.getBoolean(Config.devMode);
  }
}
