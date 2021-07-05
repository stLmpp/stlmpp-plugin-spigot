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
  public SuperThunderTask superThunderTask = null;

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
    final var lightningLocation = Util.getRandomLocationAroundRandomPlayer(world);
    if (lightningLocation == null) {
      return null;
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
      new ThunderCheckEvent(this);
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
