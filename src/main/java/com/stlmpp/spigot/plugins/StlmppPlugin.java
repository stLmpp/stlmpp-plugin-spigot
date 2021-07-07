package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.*;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortalLeakingEvent;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
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

  @Nullable
  private NetherPortalLeakingEvent netherPortalLeakingEvent;

  public String getWorldName() {
    return this.config.getString(StlmppPluginConfig.world);
  }

  public String getWorldNetherName() {
    return this.config.getString(StlmppPluginConfig.worldNether);
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
    if (this.config.getBoolean(StlmppPluginConfig.netherLightningEnabled)) {
      this.sendConsoleMessage(
          "Nether lightning activated with " +
          this.config.getDouble(StlmppPluginConfig.netherLightningChance) +
          "% of chance, every " +
          this.config.getInt(StlmppPluginConfig.netherLightningChancePerSecond) +
          " seconds!"
        );
      new NetherLightningTask(this);
    }
    if (this.config.getBoolean(StlmppPluginConfig.autoSeedEnabled)) {
      this.sendConsoleMessage(
          "Auto seed activated with max of " + this.config.getInt(StlmppPluginConfig.autoSeedMaxBlocks) + " blocks!"
        );
      new AutoSeedEvent(this);
    }
    if (this.config.getBoolean(StlmppPluginConfig.superThunderEnabled)) {
      this.sendConsoleMessage(
          "Super thunder activated with " + this.config.getDouble(StlmppPluginConfig.superThunderChance) + "% of chance"
        );
      new SuperThunderCheckEvent(this);
    }
    if (this.config.getBoolean(StlmppPluginConfig.caveInEnabled)) {
      this.sendConsoleMessage(
          "Cave-in enabled with " + this.config.getDouble(StlmppPluginConfig.caveInChance) + "% of chance"
        );
      new CaveInEvent(this);
    }
    if (this.config.getBoolean(StlmppPluginConfig.tpLightningEnabled)) {
      this.sendConsoleMessage(
          "Teleport lightning enabled with " +
          this.config.getDouble(StlmppPluginConfig.tpLightningChance) +
          "% of chance"
        );
      new LightningTeleportEvent(this);
    }
    if (this.config.getBoolean(StlmppPluginConfig.netherPortalLeakingEnabled)) {
      this.sendConsoleMessage(
          "Nether portal leaking enabled with a radius of " +
          this.config.getInt(StlmppPluginConfig.netherPortalLeakingRadius)
        );
      this.netherPortalLeakingEvent = new NetherPortalLeakingEvent(this);
    }
  }

  @Override
  public void onDisable() {
    if (this.netherPortalLeakingEvent != null) {
      this.netherPortalLeakingEvent.destroy();
    }
  }
}
