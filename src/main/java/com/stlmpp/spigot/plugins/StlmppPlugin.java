package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.AutoSeedEvent;
import com.stlmpp.spigot.plugins.events.ThunderCheckEvent;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
import com.stlmpp.spigot.plugins.tasks.superthunder.SuperThunderTask;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StlmppPlugin extends JavaPlugin {

  private final Random random = new Random();

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

  public String getWorldName() {
    return this.config.getString(Config.world);
  }

  public String getWorldNetherName() {
    return this.config.getString(Config.worldNether);
  }

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
    this.config.addDefault(Config.superThunderSecondsIntervalEvents, 2);
    this.config.addDefault(Config.superThunderEventChance, 50);
    this.config.addDefault(Config.superThunderLightningWeight, 20d);
    this.config.addDefault(Config.superThunderExplosiveLightningWeight, 10d);
    this.config.addDefault(Config.superThunderLightningCreeperWeight, 5d);
    this.config.addDefault(Config.world, "world");
    this.config.addDefault(Config.devMode, false);
    this.config.options().copyDefaults(true);
    this.saveConfig();
    this.isDevMode = this.config.getBoolean(Config.devMode);
  }

  public void activateSuperThunderEvent() {
    this.deactivateSuperThunderEvent();
    if (Chance.of(this.config.getInt(Config.superThunderChance))) {
      this.superThunderTask = new SuperThunderTask(this);
    }
  }

  public void deactivateSuperThunderEvent() {
    if (this.superThunderTask != null) {
      this.superThunderTask.cancel();
      this.superThunderTask = null;
    }
  }

  public Location strikeLightningRandomPlayer(@NotNull World world) {
    var players = world.getPlayers();
    var playersSize = players.size();
    if (playersSize == 0) {
      return null;
    }
    var randomPlayer = players.get(this.random.nextInt(playersSize));
    var playerLocation = randomPlayer.getLocation();
    var playerX = playerLocation.getBlockX();
    var playerY = playerLocation.getBlockY();
    var playerZ = playerLocation.getBlockZ();
    var lightningX = ThreadLocalRandom.current().nextInt(playerX - 50, playerX + 51);
    var lightningY = ThreadLocalRandom.current().nextInt(playerY - 10, playerY + 11);
    var lightningZ = ThreadLocalRandom.current().nextInt(playerZ - 50, playerZ + 51);
    var iteration = 0;
    while (world.getBlockAt(lightningX, lightningY, lightningZ).getType().isAir()) {
      iteration++;
      if (iteration > 64) {
        break;
      }
      lightningY--;
    }
    if (this.isDevMode) {
      Bukkit.broadcastMessage("Striking lightning at X=" + lightningX + ", Y=" + lightningY + ", Z=" + lightningZ);
    }
    var lightningLocation = new Location(world, lightningX, lightningY, lightningZ);
    world.strikeLightning(lightningLocation);
    return lightningLocation;
  }

  public Location strikeLightningRandomPlayerWithChanceOfExplosion(@NotNull World world, int chanceOfExplosion) {
    var lightningLocation = this.strikeLightningRandomPlayer(world);
    if (lightningLocation == null) {
      return null;
    }
    if (Chance.of(chanceOfExplosion)) {
      var explosionPower = ThreadLocalRandom.current().nextInt(0, 16);
      if (this.isDevMode) {
        Bukkit.broadcastMessage("Struck lightning with explosive power of " + explosionPower);
      }
      world.createExplosion(lightningLocation, (float) explosionPower, true, true);
    }
    return lightningLocation;
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
      this.netherLightningTask = new NetherLightningTask(this);
    }
    if (this.config.getBoolean(Config.autoSeedEnabled)) {
      StlmppPlugin.sendConsoleMessage(
        "Auto seed activated with max of " + this.config.getInt(Config.autoSeedMaxBlocks) + " blocks!"
      );
      this.autoSeedEvent = new AutoSeedEvent(this);
    }
    if (this.config.getBoolean(Config.superThunderEnabled)) {
      this.thunderCheckEvent = new ThunderCheckEvent(this);
    }
  }

  @Override
  public void onDisable() {
    super.onDisable();
  }
}
