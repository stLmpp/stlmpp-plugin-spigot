package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.*;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
import com.stlmpp.spigot.plugins.utils.Config;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class StlmppPlugin extends JavaPlugin {

  public void sendConsoleMessage(String message) {
    this.getServer().getConsoleSender().sendMessage("[StlmppPlugin] " + message);
  }

  public final FileConfiguration config = this.getConfig();
  public Boolean isDevMode = false;

  public String getWorldName() {
    return this.config.getString(Config.world);
  }

  public String getWorldNetherName() {
    return this.config.getString(Config.worldNether);
  }

  @Nullable
  public World getWorld() {
    return this.getServer().getWorld(this.getWorldName());
  }

  @Nullable
  public World getWorldNether() {
    return this.getServer().getWorld(this.getWorldNetherName());
  }

  @Override
  public void onEnable() {
    new StlmppPluginConfig(this);
    if (this.config.getBoolean(Config.netherLightningEnabled)) {
      this.sendConsoleMessage(
          "Nether lightning activated with " +
          this.config.getInt(Config.netherLightningChance) +
          "% of chance, every " +
          this.config.getInt(Config.netherLightningChancePerSecond) +
          " seconds!"
        );
      new NetherLightningTask(this);
    }
    if (this.config.getBoolean(Config.autoSeedEnabled)) {
      this.sendConsoleMessage(
          "Auto seed activated with max of " + this.config.getInt(Config.autoSeedMaxBlocks) + " blocks!"
        );
      new AutoSeedEvent(this);
    }
    if (this.config.getBoolean(Config.superThunderEnabled)) {
      this.sendConsoleMessage(
          "Super thunder activated with " + this.config.getInt(Config.superThunderChance) + "% of chance"
        );
      new SuperThunderCheckEvent(this);
    }
    if (this.config.getBoolean(Config.caveInEnabled)) {
      this.sendConsoleMessage("Cave-in enabled with " + this.config.getInt(Config.caveInChance) + "% of chance");
      new CaveInEvent(this);
    }
    if (this.config.getBoolean(Config.tpLightningEnabled)) {
      this.sendConsoleMessage(
          "Teleport lightning enabled with " + this.config.getInt(Config.tpLightningChance) + "% of chance"
        );
      new LightningTeleportEvent(this);
    }
    if (this.config.getBoolean(Config.netherPortalLeakingEnabled)) {
      this.sendConsoleMessage(
          "Nether portal leaking enabled with a radius of " + this.config.getInt(Config.netherPortalLeakingRadius)
        );
      new NetherPortalLeakingEvent(this);
    }
  }
}
