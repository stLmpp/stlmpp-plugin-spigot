package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.*;
import com.stlmpp.spigot.plugins.events.netherportalleaking.NetherPortalLeakingEvent;
import com.stlmpp.spigot.plugins.events.superminingmachine.SMMManager;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public class StlmppPlugin extends JavaPlugin {

  public void log(String message) {
    log(message, false);
  }

  public void log(String message, boolean onlyDevMode) {
    if (!onlyDevMode || isDevMode) {
      getLogger().info(message);
    }
  }

  public void sendMessage(String message) {
    getServer().broadcast(Component.text(message));
  }

  private Connection databaseConnection;

  public final FileConfiguration config = getConfig();
  public Boolean isDevMode = false;

  @Nullable private NetherPortalLeakingEvent netherPortalLeakingEvent;
  @Nullable private NetherLightningTask netherLightningTask;
  @Nullable public SMMManager smmManager;

  public String getWorldName() {
    return config.getString(StlmppPluginConfig.world);
  }

  public String getWorldNetherName() {
    return config.getString(StlmppPluginConfig.worldNether);
  }

  @Nullable
  public World getWorld() {
    return getServer().getWorld(getWorldName());
  }

  @Nullable
  public World getWorldNether() {
    return getServer().getWorld(getWorldNetherName());
  }

  @Override
  public void onEnable() {
    new StlmppPluginConfig(this);
    Path filePath = Paths.get(getDataPath().toString(), "database.db");
    try {
      databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + filePath);
    } catch (SQLException e) {
      log("Failed to connect to database.");
      throw new RuntimeException(e);
    }
    CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(!isDevMode));
    CommandAPI.onEnable();
    netherLightningTask = NetherLightningTask.register(this);
    AutoSeedEvent.register(this);
    LightningTeleportEvent.register(this);
    netherPortalLeakingEvent = NetherPortalLeakingEvent.register(this);
    EggRandomEffectEvent.register(this);
    DeathEvent.register(this);
    smmManager = SMMManager.register(this);
    if (smmManager != null) {
      smmManager.onEnable();
    }
  }

  @Override
  public void onDisable() {
    if (netherPortalLeakingEvent != null) {
      netherPortalLeakingEvent.destroy();
    }
    if (netherLightningTask != null) {
      netherLightningTask.stopLastTask();
    }
    if (smmManager != null) {
      smmManager.onDisable();
    }
    CommandAPI.onDisable();
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
