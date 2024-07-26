package com.stlmpp.spigot.plugins;

import org.bukkit.Material;

public class StlmppPluginConfig {

  public static final String netherLightningEnabled = "nether-lightning-enabled";
  public static final String netherLightningChance = "nether-lightning-chance";
  public static final String netherLightningMinSeconds = "nether-lightning-min-seconds";
  public static final String netherLightningMaxSeconds = "nether-lightning-max-seconds";
  public static final String netherLightningExplosionChance = "nether-lightning-explosion-chance";
  public static final String netherLightningExplosionMinPower =
      "nether-lightning-explosion-min-power";
  public static final String netherLightningExplosionMaxPower =
      "nether-lightning-explosion-max-power";
  public static final String netherLightningRealChance = "nether-lightning-real-chance";
  public static final String worldNether = "world-nether";
  public static final String autoSeedMaxBlocks = "auto-seed-max-blocks";
  public static final String autoSeedEnabled = "auto-seed-enabled";
  public static final String autoSeedAllowedSeedList = "auto-seed-allowed-seed-list";
  public static final String tpLightningEnabled = "tp-lightning-enabled";
  public static final String tpLightningNetherBlocksRadius = "tp-lightning-nether-blocks-radius";
  public static final String tpLightningChance = "tp-lightning-chance";
  public static final String tpLightningExplosionChance = "tp-lightning-explosion-chance";
  public static final String netherPortalLeakingEnabled = "nether-portal-leaking-enabled";
  public static final String netherPortalLeakingRadius = "nether-portal-leaking-radius";
  public static final String netherPortalLeakingChanceOfNetherrackFire =
      "nether-portal-leaking-chance-of-netherrack-fire";
  public static final String netherPortalLeakingKnockbackPower =
      "nether-portal-leaking-knockback-power";
  public static final String world = "world";
  public static final String devMode = "dev-mode";
  public static final String eggRandomEventEnabled = "egg-random-event-enabled";
  public static final String eggRandomEventChance = "egg-random-event-chance";
  public static final String deathEventEnabled = "death-event-enabled";

  public StlmppPluginConfig(StlmppPlugin plugin) {
    final var config = plugin.config;
    config.addDefault(StlmppPluginConfig.netherLightningChance, 90d);
    config.addDefault(StlmppPluginConfig.netherLightningEnabled, true);
    config.addDefault(StlmppPluginConfig.netherLightningMaxSeconds, 15);
    config.addDefault(StlmppPluginConfig.netherLightningMinSeconds, 5);
    config.addDefault(StlmppPluginConfig.netherLightningExplosionChance, 5d);
    config.addDefault(StlmppPluginConfig.netherLightningExplosionMinPower, 0f);
    config.addDefault(StlmppPluginConfig.netherLightningExplosionMaxPower, 16f);
    config.addDefault(StlmppPluginConfig.netherLightningRealChance, 0.5d);
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
        });
    config.addDefault(StlmppPluginConfig.world, "world");
    config.addDefault(StlmppPluginConfig.tpLightningEnabled, true);
    config.addDefault(StlmppPluginConfig.tpLightningChance, 75d);
    config.addDefault(StlmppPluginConfig.tpLightningNetherBlocksRadius, 4);
    config.addDefault(StlmppPluginConfig.tpLightningExplosionChance, 0.25d);
    config.addDefault(StlmppPluginConfig.netherPortalLeakingEnabled, true);
    config.addDefault(StlmppPluginConfig.netherPortalLeakingRadius, 7);
    config.addDefault(StlmppPluginConfig.netherPortalLeakingChanceOfNetherrackFire, 0.25d);
    config.addDefault(StlmppPluginConfig.netherPortalLeakingKnockbackPower, 0.5d);
    config.addDefault(StlmppPluginConfig.devMode, false);
    config.addDefault(StlmppPluginConfig.eggRandomEventEnabled, true);
    config.addDefault(StlmppPluginConfig.eggRandomEventChance, 10.0d);
    config.addDefault(StlmppPluginConfig.deathEventEnabled, true);
    config.options().copyDefaults(true);
    plugin.saveConfig();
    plugin.isDevMode = config.getBoolean(devMode);
  }
}
