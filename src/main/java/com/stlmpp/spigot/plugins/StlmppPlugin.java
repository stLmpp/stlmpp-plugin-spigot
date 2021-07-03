package com.stlmpp.spigot.plugins;

import com.stlmpp.spigot.plugins.events.*;
import com.stlmpp.spigot.plugins.tasks.NetherLightningTask;
import com.stlmpp.spigot.plugins.tasks.superthunder.SuperThunderTask;
import com.stlmpp.spigot.plugins.utils.Chance;
import com.stlmpp.spigot.plugins.utils.Config;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StlmppPlugin extends JavaPlugin {

  private final Random random = new Random();

  public void sendConsoleMessage(String message) {
    this.getServer().getConsoleSender().sendMessage("[StlmppPlugin] " + message);
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

  @Nullable
  public CaveInEvent caveInEvent = null;

  @Nullable
  public LightningTeleportEvent lightningTeleportEvent = null;

  public StlmppPluginConfig stlmppPluginConfig;

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

  @Nullable
  public Location strikeLightningRandomPlayer(@NotNull World world) {
    final var players = world.getPlayers();
    final var playersSize = players.size();
    if (playersSize == 0) {
      return null;
    }
    final var playerLocation = players.get(this.random.nextInt(playersSize)).getLocation();
    final var playerX = playerLocation.getBlockX();
    final var playerY = playerLocation.getBlockY();
    final var playerZ = playerLocation.getBlockZ();
    final var lightningX = ThreadLocalRandom.current().nextInt(playerX - 50, playerX + 51);
    final var lightningY = ThreadLocalRandom.current().nextInt(playerY - 10, playerY + 11);
    final var lightningZ = ThreadLocalRandom.current().nextInt(playerZ - 50, playerZ + 51);
    final var lightningLocation = new Location(world, lightningX, lightningY, lightningZ);
    lightningLocation.setY(Util.getFloor(world, lightningLocation));
    if (this.isDevMode) {
      this.getServer()
        .broadcastMessage("Striking lightning at X=" + lightningX + ", Y=" + lightningY + ", Z=" + lightningZ);
    }
    world.strikeLightning(lightningLocation);
    return lightningLocation;
  }

  @Nullable
  public Location strikeLightningRandomPlayerWithChanceOfExplosion(@NotNull World world, int chanceOfExplosion) {
    final var lightningLocation = this.strikeLightningRandomPlayer(world);
    if (lightningLocation == null) {
      return null;
    }
    if (Chance.of(chanceOfExplosion)) {
      final var explosionPower = ThreadLocalRandom.current().nextInt(0, 16);
      if (this.isDevMode) {
        this.getServer().broadcastMessage("Struck lightning with explosive power of " + explosionPower);
      }
      world.createExplosion(lightningLocation, (float) explosionPower, true, true);
    }
    return lightningLocation;
  }

  @Override
  public void onEnable() {
    this.stlmppPluginConfig = new StlmppPluginConfig(this);
    if (this.config.getBoolean(Config.netherLightningEnabled)) {
      this.sendConsoleMessage(
          "Nether lightning activated with " +
          this.config.getInt(Config.netherLightningChance) +
          "% of chance, every " +
          this.config.getInt(Config.netherLightningChancePerSecond) +
          " seconds!"
        );
      this.netherLightningTask = new NetherLightningTask(this);
    }
    if (this.config.getBoolean(Config.autoSeedEnabled)) {
      this.sendConsoleMessage(
          "Auto seed activated with max of " + this.config.getInt(Config.autoSeedMaxBlocks) + " blocks!"
        );
      this.autoSeedEvent = new AutoSeedEvent(this);
    }
    if (this.config.getBoolean(Config.superThunderEnabled)) {
      this.sendConsoleMessage(
          "Super thunder activated with " + this.config.getInt(Config.superThunderChance) + "% of chance"
        );
      this.thunderCheckEvent = new ThunderCheckEvent(this);
    }
    if (this.config.getBoolean(Config.caveInEnabled)) {
      this.sendConsoleMessage("Cave-in enabled with " + this.config.getInt(Config.caveInChance) + "% of chance");
      this.caveInEvent = new CaveInEvent(this);
    }
    if (this.config.getBoolean(Config.tpLightningEnabled)) {
      this.sendConsoleMessage(
          "Teleport lightning enabled with " + this.config.getInt(Config.tpLightningChance) + "% of chance"
        );
      this.lightningTeleportEvent = new LightningTeleportEvent(this);
    }
    new NetherPortalLeakingEvent(this);
  }
}
