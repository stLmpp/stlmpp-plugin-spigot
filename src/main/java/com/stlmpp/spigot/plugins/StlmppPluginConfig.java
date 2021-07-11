package com.stlmpp.spigot.plugins;

import org.bukkit.Material;

public class StlmppPluginConfig {

  public static final String netherLightningEnabled = "nether-lightning-enabled";
  public static final String netherLightningChance = "nether-lightning-chance";
  public static final String netherLightningChancePerSecond = "nether-lightning-chance-per-second";
  public static final String netherLightningExplosionChance = "nether-lightning-explosion-chance";
  public static final String netherLightningExplosionMinPower = "nether-lightning-explosion-min-power";
  public static final String netherLightningExplosionMaxPower = "nether-lightning-explosion-max-power";
  public static final String netherLightningRealChance = "nether-lightning-real-chance";
  public static final String worldNether = "world-nether";
  public static final String autoSeedMaxBlocks = "auto-seed-max-blocks";
  public static final String autoSeedEnabled = "auto-seed-enabled";
  public static final String autoSeedAllowedSeedList = "auto-seed-allowed-seed-list";
  public static final String superThunderEnabled = "super-thunder-enabled";
  public static final String superThunderChance = "super-thunder-chance";
  public static final String superThunderSecondsIntervalEvents = "super-thunder-seconds-interval-events";
  public static final String superThunderEventChance = "super-thunder-event-chance";
  public static final String superThunderLightningWeight = "super-thunder-lightning-weight";
  public static final String superThunderExplosiveLightningWeight = "super-thunder-explosive-lightning-weight";
  public static final String superThunderLightningCreeperWeight = "super-thunder-lightning-creeper-weight";
  public static final String superThunderGhastSwarmWeight = "super-thunder-ghast-swarm-weight";
  public static final String superThunderSafeCoords = "super-thunder-safe-coords";
  public static final String superThunderSafeCoordsRadius = "super-thunder-safe-coords-radius";
  public static final String caveInEnabled = "cave-in-enabled";
  public static final String caveInMinHeight = "cave-in-min-height";
  public static final String caveInMinWidth = "cave-in-min-width";
  public static final String caveInMaxWidth = "cave-in-max-width";
  public static final String caveInMaxHeight = "cave-in-max-height";
  public static final String caveInMaxY = "cave-in-max-y";
  public static final String caveInImmunityItems = "cave-in-immunity-items";
  public static final String caveInChance = "cave-in-chance";
  public static final String caveInBlocks = "cave-in-blocks";
  public static final String tpLightningEnabled = "tp-lightning-enabled";
  public static final String tpLightningNetherBlocksRadius = "tp-lightning-nether-blocks-radius";
  public static final String tpLightningChance = "tp-lightning-chance";
  public static final String tpLightningExplosionChance = "tp-lightning-explosion-chance";
  public static final String netherPortalLeakingEnabled = "nether-portal-leaking-enabled";
  public static final String netherPortalLeakingRadius = "nether-portal-leaking-radius";
  public static final String netherPortalLeakingChanceOfNetherrackFire =
    "nether-portal-leaking-chance-of-netherrack-fire";
  public static final String netherPortalLeakingKnockbackPower = "nether-portal-leaking-knockback-power";
  public static final String world = "world";
  public static final String devMode = "dev-mode";

  public StlmppPluginConfig(StlmppPlugin plugin) {
    final var config = plugin.config;
    config.addDefault(StlmppPluginConfig.netherLightningChance, 50d);
    config.addDefault(StlmppPluginConfig.netherLightningEnabled, true);
    config.addDefault(StlmppPluginConfig.netherLightningChancePerSecond, 15);
    config.addDefault(StlmppPluginConfig.netherLightningExplosionChance, 5d);
    config.addDefault(StlmppPluginConfig.netherLightningExplosionMinPower, 0f);
    config.addDefault(StlmppPluginConfig.netherLightningExplosionMaxPower, 16f);
    config.addDefault(StlmppPluginConfig.netherLightningRealChance, 0.1d);
    config.addDefault(StlmppPluginConfig.worldNether, "world_nether");
    config.addDefault(StlmppPluginConfig.autoSeedEnabled, true);
    config.addDefault(StlmppPluginConfig.autoSeedMaxBlocks, 60);
    config.addDefault(
      StlmppPluginConfig.autoSeedAllowedSeedList,
      new String[] {
        Material.WHEAT_SEEDS.name(),
        Material.MELON_SEEDS.name(),
        Material.BEETROOT_SEEDS.name(),
        Material.PUMPKIN_SEEDS.name(),
        Material.POTATO.name(),
        Material.CARROT.name(),
      }
    );
    config.addDefault(StlmppPluginConfig.superThunderEnabled, true);
    config.addDefault(StlmppPluginConfig.superThunderChance, 5d);
    config.addDefault(StlmppPluginConfig.superThunderSecondsIntervalEvents, 30);
    config.addDefault(StlmppPluginConfig.superThunderEventChance, 25d);
    config.addDefault(StlmppPluginConfig.superThunderLightningWeight, 250d);
    config.addDefault(StlmppPluginConfig.superThunderExplosiveLightningWeight, 5d);
    config.addDefault(StlmppPluginConfig.superThunderLightningCreeperWeight, 2d);
    config.addDefault(StlmppPluginConfig.superThunderGhastSwarmWeight, 2d);
    config.addDefault(StlmppPluginConfig.superThunderSafeCoordsRadius, 250);
    config.addDefault(StlmppPluginConfig.superThunderSafeCoords, "0 0 0");
    config.addDefault(StlmppPluginConfig.world, "world");
    config.addDefault(StlmppPluginConfig.caveInEnabled, true);
    config.addDefault(StlmppPluginConfig.caveInMaxY, 30);
    config.addDefault(StlmppPluginConfig.caveInMinHeight, 4);
    config.addDefault(StlmppPluginConfig.caveInMinWidth, 4);
    config.addDefault(
      StlmppPluginConfig.caveInImmunityItems,
      new String[] { Material.RABBIT_FOOT.name(), Material.CLOCK.name(), Material.COMPASS.name() }
    );
    config.addDefault(StlmppPluginConfig.caveInChance, 0.05d);
    config.addDefault(
      StlmppPluginConfig.caveInBlocks,
      new String[] { Material.STONE.name(), Material.COBBLESTONE.name() }
    );
    config.addDefault(StlmppPluginConfig.caveInMaxWidth, 10);
    config.addDefault(StlmppPluginConfig.caveInMaxHeight, 10);
    config.addDefault(StlmppPluginConfig.tpLightningEnabled, true);
    config.addDefault(StlmppPluginConfig.tpLightningChance, 75d);
    config.addDefault(StlmppPluginConfig.tpLightningNetherBlocksRadius, 4);
    config.addDefault(StlmppPluginConfig.tpLightningExplosionChance, 0.25d);
    config.addDefault(StlmppPluginConfig.netherPortalLeakingEnabled, true);
    config.addDefault(StlmppPluginConfig.netherPortalLeakingRadius, 15);
    config.addDefault(StlmppPluginConfig.netherPortalLeakingChanceOfNetherrackFire, 0.25d);
    config.addDefault(StlmppPluginConfig.netherPortalLeakingKnockbackPower, 0.5d);
    config.addDefault(StlmppPluginConfig.devMode, false);
    config.options().copyDefaults(true);
    plugin.saveConfig();
    plugin.isDevMode = config.getBoolean(devMode);
  }
}
