package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.*;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortalLeakingEvent;
import com.stlmpp.spigot.plugins.events.superminingmachine.SMMManager;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class StlmppPlugin extends JavaPlugin {

  public void log(String message) {
    this.log(message, false);
  }

  public void log(String message, boolean onlyDevMode) {
    if (!onlyDevMode || this.isDevMode) {
      this.getLogger().info(message);
    }
  }

  public void sendMessage(String message) {
    this.getServer().broadcast(Component.text(message));
  }

  private Connection databaseConnection;

  public final FileConfiguration config = this.getConfig();
  public Boolean isDevMode = false;

  @Nullable private NetherPortalLeakingEvent netherPortalLeakingEvent;
  @Nullable private NetherLightningTask netherLightningTask;
  @Nullable public SMMManager smmManager;

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
    Path filePath = Paths.get(getDataPath().toString(), "database.db");
    try {
      this.databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
    } catch (SQLException e) {
      log("Failed to connect to database.");
      throw new RuntimeException(e);
    }
    this.netherLightningTask = NetherLightningTask.register(this);
    AutoSeedEvent.register(this);
    LightningTeleportEvent.register(this);
    this.netherPortalLeakingEvent = NetherPortalLeakingEvent.register(this);
    EggRandomEffectEvent.register(this);
    DeathEvent.register(this);
    this.smmManager = SMMManager.register(this);
  }

  @Override
  public void onDisable() {
    if (this.netherPortalLeakingEvent != null) {
      this.netherPortalLeakingEvent.destroy();
    }
    if (this.netherLightningTask != null) {
      this.netherLightningTask.stopLastTask();
    }
    if (this.smmManager != null) {
      this.smmManager.onDisable();
    }
  }

  public BukkitTask runLater(long delay, Runnable function) {
    return new BukkitRunnable() {
      @Override
      public void run() {
        function.run();
      }
    }.runTaskLater(this, delay);
  }

  public BukkitTask runTimer(long delay, long period, Runnable function) {
    return new BukkitRunnable() {
      @Override
      public void run() {
        function.run();
      }
    }.runTaskTimer(this, delay, period);
  }

  public Connection getDatabaseConnection() {
    return databaseConnection;
  }
}
