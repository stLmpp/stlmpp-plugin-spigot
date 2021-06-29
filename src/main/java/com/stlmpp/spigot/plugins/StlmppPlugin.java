package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.AutoSeedEvent;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
import com.stlmpp.spigot.plugins.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class StlmppPlugin extends JavaPlugin {

  public static void sendConsoleMessage(String message) {
    Bukkit.getConsoleSender().sendMessage("[StlmppPlugin] " + message);
  }

  public FileConfiguration config = this.getConfig();
  public Boolean isDevMode = false;

  private void createConfig() {
    this.config.addDefault(Config.lightningChance, 40);
    this.config.addDefault(Config.lightningEnabled, true);
    this.config.addDefault(Config.lightningChancePerSecond, 10);
    this.config.addDefault(Config.lightningExplosionChance, 50);
    this.config.addDefault(Config.worldNether, "world_nether");
    this.config.addDefault(Config.autoSeedEnabled, true);
    this.config.addDefault(Config.autoSeedMaxBlocks, 40);
    this.config.addDefault(Config.devMode, false);
    this.config.options().copyDefaults(true);
    this.saveConfig();
    this.isDevMode = this.config.getBoolean(Config.devMode);
  }

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
      new NetherLightningTask(this);
    }
    if (this.config.getBoolean(Config.autoSeedEnabled)) {
      StlmppPlugin.sendConsoleMessage(
        "Auto seed activated with max of " + this.config.getInt(Config.autoSeedMaxBlocks) + " blocks!"
      );
      new AutoSeedEvent(this);
    }
  }

  @Override
  public void onDisable() {
    super.onDisable();
  }
}
