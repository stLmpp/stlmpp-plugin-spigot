package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.tasks.LightningTask;
import com.stlmpp.spigot.plugins.utils.Config;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class StlmppPlugin extends JavaPlugin {

  public FileConfiguration config = this.getConfig();

  private void createConfig() {
    this.config.addDefault(Config.lightningChance, 40);
    this.config.addDefault(Config.lightningEnabled, true);
    this.config.addDefault(Config.lightningChancePerSecond, 10);
    this.config.addDefault(Config.devMode, false);
    this.config.options().copyDefaults(true);
    this.saveConfig();
  }

  @Override
  public void onEnable() {
    this.createConfig();
    if (this.config.getBoolean(Config.lightningEnabled)) {
      Bukkit
        .getConsoleSender()
        .sendMessage(
          "Nether lightning activated with " +
          this.config.getInt(Config.lightningChance) +
          "% of chance, every " +
          this.config.getInt(Config.lightningChancePerSecond) +
          " seconds!"
        );
      new LightningTask(this);
    }
  }

  @Override
  public void onDisable() {
    super.onDisable();
  }
}
