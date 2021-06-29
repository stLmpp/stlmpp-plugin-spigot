package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.AutoSeedEvent;
import com.stlmpp.spigot.plugins.events.ThunderCheckEvent;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
import com.stlmpp.spigot.plugins.tasks.SuperThunderTask;
import com.stlmpp.spigot.plugins.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class StlmppPlugin extends JavaPlugin {

  public static void sendConsoleMessage(String message) {
    Bukkit.getConsoleSender().sendMessage("[StlmppPlugin] " + message);
  }

  public final FileConfiguration config = this.getConfig();
  public Boolean isDevMode = false;

  @Nullable
  public NetherLightningTask netherLightningTask = null;

  @Nullable
  public AutoSeedEvent autoSeedEvent = null;

  @Nullable
  public ThunderCheckEvent thunderCheckEvent = null;

  @Nullable
  public SuperThunderTask superThunderTask = null;

  private void createConfig() {
    this.config.addDefault(Config.lightningChance, 40);
    this.config.addDefault(Config.lightningEnabled, true);
    this.config.addDefault(Config.lightningChancePerSecond, 10);
    this.config.addDefault(Config.lightningExplosionChance, 50);
    this.config.addDefault(Config.worldNether, "world_nether");
    this.config.addDefault(Config.autoSeedEnabled, true);
    this.config.addDefault(Config.autoSeedMaxBlocks, 40);
    var autoSeedAllowedSeedList = new String[] {
      Material.WHEAT_SEEDS.name(),
      Material.MELON_SEEDS.name(),
      Material.BEETROOT_SEEDS.name(),
      Material.PUMPKIN_SEEDS.name(),
      Material.POTATO.name(),
      Material.CARROT.name(),
    };
    this.config.addDefault(Config.autoSeedAllowedSeedList, autoSeedAllowedSeedList);
    this.config.addDefault(Config.superThunderEnabled, true);
    this.config.addDefault(Config.superThunderChance, 25);
    this.config.addDefault(Config.superThunderSecondsIntervalEvents, 30);
    this.config.addDefault(Config.devMode, false);
    this.config.options().copyDefaults(true);
    this.saveConfig();
    this.isDevMode = this.config.getBoolean(Config.devMode);
  }

  public void activateThunderEvent() {}

  public void deactivateThunderEvent() {}

  @Override
  public void onEnable() {
    this.createConfig();
    if (this.config.getBoolean(Config.lightningEnabled)) {
      StlmppPlugin.sendConsoleMessage(
        "Nether lightning activated with " +
        this.config.getInt(Config.lightningChance) +
        "% of chance, every " +
        this.config.getInt(Config.lightningChancePerSecond) +
        " seconds!"
      );
      this.netherLightningTask = new NetherLightningTask(this);
    }
    if (this.config.getBoolean(Config.autoSeedEnabled)) {
      StlmppPlugin.sendConsoleMessage(
        "Auto seed activated with max of " + this.config.getInt(Config.autoSeedMaxBlocks) + " blocks!"
      );
      this.autoSeedEvent = new AutoSeedEvent(this);
    }
    new ThunderCheckEvent(this);
  }

  @Override
  public void onDisable() {
    super.onDisable();
  }
}
