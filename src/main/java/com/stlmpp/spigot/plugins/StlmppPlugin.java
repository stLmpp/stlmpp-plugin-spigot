package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.*;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortalLeakingEvent;
import com.stlmpp.spigot.plugins.events.superminingmachine.SuperMiningMachineManager;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class StlmppPlugin extends JavaPlugin {

  public void log(String message) {
    this.log(message, false);
  }

  public void log(String message, boolean onlyDevMode) {
    if (!onlyDevMode || this.isDevMode) {
      this.getLogger().info(String.format("[StlmppPlugin] %s", message));
    }
  }

  public void sendMessage(String message) {
    this.getServer().broadcast(Component.text(message));
  }

  public final FileConfiguration config = this.getConfig();
  public Boolean isDevMode = false;

  @Nullable private NetherPortalLeakingEvent netherPortalLeakingEvent;
  @Nullable private NetherLightningTask netherLightningTask;
  @Nullable public SuperMiningMachineManager superMiningMachineManager;

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
    this.netherLightningTask = NetherLightningTask.register(this);
    AutoSeedEvent.register(this);
    LightningTeleportEvent.register(this);
    this.netherPortalLeakingEvent = NetherPortalLeakingEvent.register(this);
    EggRandomEffectEvent.register(this);
    DeathEvent.register(this);
    this.superMiningMachineManager = SuperMiningMachineManager.register(this);
  }

  @Override
  public void onDisable() {
    if (this.netherPortalLeakingEvent != null) {
      this.netherPortalLeakingEvent.destroy();
    }
    if (this.netherLightningTask != null) {
      this.netherLightningTask.stopLastTask();
    }
    if (this.superMiningMachineManager != null) {
      this.superMiningMachineManager.onDisable();
    }
  }
}
