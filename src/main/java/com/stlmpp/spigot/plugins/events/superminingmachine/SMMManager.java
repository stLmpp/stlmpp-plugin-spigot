package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMBlockEntity;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMEntity;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMQueries;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMStateUpdateDto;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SMMManager {

  @Nullable
  public static SMMManager register(StlmppPlugin plugin) {
    if (!plugin.config.getBoolean(StlmppPluginConfig.superMiningMachineEnabled)) {
      return null;
    }
    return new SMMManager(plugin);
  }

  private SMMManager(StlmppPlugin plugin) {
    this.plugin = plugin;
    prepareDatabase();
    SMMCreationEvent.register(plugin);
    SMMStartEvent.register(plugin);
    SMMDestroyEvent.register(plugin);
  }

  private void prepareDatabase() {
    try (final var statement = plugin.getDatabaseConnection().createStatement()) {
      statement.executeUpdate(SMMQueries.ddl);
    } catch (Exception error) {
      throw new RuntimeException(error.getMessage());
    }
  }

  private final StlmppPlugin plugin;

  private final HashMap<String, SuperMiningMachine> machines = new HashMap<>();

  public boolean isOverlappingAnotherMachine(BoundingBox boundingBox) {
    return machines.values().stream()
        .anyMatch(
            machine ->
                machine.boundingBox.clone().expand(0, 64, 0, 0, 320, 0).overlaps(boundingBox));
  }

  @Nullable
  public SuperMiningMachine getMachineByNetheriteBlock(Block block) {
    return Util.findFirst(machines.values(), machine -> machine.hasNetheriteBlock(block));
  }

  @Nullable
  public SuperMiningMachine getMachineByBlock(Block block) {
    return Util.findFirst(machines.values(), machine -> machine.hasBlock(block));
  }

  public boolean isWorldValid(@NotNull World world) {
    return world.getName().equals(plugin.getWorldName())
        || world.getName().equals(plugin.getWorldNetherName());
  }

  public boolean isBlockTypeValid(@NotNull Material material) {
    return material.equals(Material.NETHERITE_BLOCK) || material.equals(Material.OBSIDIAN);
  }

  public void addMachine(SuperMiningMachine machine) {
    machines.put(machine.getId(), machine);
    final var entity = machine.serialize();
    final var insertBlockQuery =
        SMMQueries.smmBlockInsertBase
            + StringUtils.chop("(?, ?, ?),".repeat(entity.blocks().size()));
    try (final var statement =
            plugin.getDatabaseConnection().prepareStatement(SMMQueries.smmInsert);
        final var statementBlocks =
            plugin.getDatabaseConnection().prepareStatement(insertBlockQuery)) {
      statement.setString(1, entity.id());
      statement.setString(2, entity.world());
      statement.setString(3, entity.bottomLeft());
      statement.setString(4, entity.bottomRight());
      statement.setString(5, entity.topLeft());
      statement.setString(6, entity.topRight());
      statement.setString(7, entity.lastBlock());
      statement.setDouble(8, entity.boostFactor());
      statement.setInt(9, entity.boostTimes());
      statement.setInt(10, entity.isRunning());
      statement.setInt(11, entity.hasFinished());
      var parameterIndex = 0;
      final var blocks = entity.blocks();
      for (final SMMBlockEntity block : blocks) {
        statementBlocks.setString(++parameterIndex, block.smmId());
        statementBlocks.setString(++parameterIndex, block.location());
        statementBlocks.setString(++parameterIndex, block.type());
      }
      statement.executeUpdate();
      statementBlocks.executeUpdate();
    } catch (Exception error) {
      plugin.log("Error trying to insert the machine: " + error.getMessage());
    }
  }

  public void removeMachine(SuperMiningMachine machine) {
    machines.remove(machine.getId());
    try (final var statement = plugin.getDatabaseConnection().prepareStatement(SMMQueries.delete);
        final var statementBlocks =
            plugin.getDatabaseConnection().prepareStatement(SMMQueries.deleteBlocks)) {
      statement.setString(1, machine.getId());
      statementBlocks.setString(1, machine.getId());
      statement.executeUpdate();
      statementBlocks.executeUpdate();
    } catch (Exception error) {
      plugin.log("Error trying to delete the machine: " + error.getMessage());
    }
  }

  public void updateState(String id, SMMStateUpdateDto dto) {
    plugin.log(String.format("Updating state of machine %s | %s", id, dto));
    try (final var statement =
        plugin.getDatabaseConnection().prepareStatement(SMMQueries.updateState)) {
      statement.setInt(1, dto.isRunning());
      statement.setInt(2, dto.hasFinished());
      statement.setString(3, dto.lastBlock());
      statement.setDouble(4, dto.boostFactor());
      statement.setInt(5, dto.boostTimes());
      statement.setString(6, id);
      statement.executeUpdate();
    } catch (Exception error) {
      plugin.log("Error trying to update the state: " + error.getMessage());
    }
  }

  public void insertBlock(SMMBlockEntity block) {
    plugin.log(String.format("Inserting block on machine %s | %s", block.smmId(), block));
    try (final var statement =
        plugin.getDatabaseConnection().prepareStatement(SMMQueries.smmBlockInsert)) {
      statement.setString(1, block.smmId());
      statement.setString(2, block.location());
      statement.setString(3, block.type());
      statement.executeUpdate();
    } catch (Exception error) {
      plugin.log("Error trying to insert block: " + error.getMessage());
    }
  }

  public void onEnable() {
    try (final var statement = plugin.getDatabaseConnection().createStatement()) {
      final var results = statement.executeQuery(SMMQueries.selectAll);
      final var map = new HashMap<String, SMMEntity>();
      while (results.next()) {
        final String world = results.getString(1);
        final String bottomLeft = results.getString(2);
        final String bottomRight = results.getString(3);
        final String topLeft = results.getString(4);
        final String topRight = results.getString(5);
        final String lastBlock = results.getString(6);
        final Double boostFactor = results.getDouble(7);
        final Integer boostTimes = results.getInt(8);
        final Integer isRunning = results.getInt(9);
        final Integer hasFinished = results.getInt(10);
        final Integer blockId = results.getInt(11);
        final String smmId = results.getString(12);
        final String location = results.getString(13);
        final String type = results.getString(14);
        final var entity =
            map.getOrDefault(
                smmId,
                new SMMEntity(
                    smmId,
                    world,
                    bottomLeft,
                    bottomRight,
                    topLeft,
                    topRight,
                    lastBlock,
                    boostFactor,
                    boostTimes,
                    isRunning,
                    hasFinished,
                    new ArrayList<>()));
        map.putIfAbsent(smmId, entity);
        entity.blocks().add(new SMMBlockEntity(blockId, smmId, location, type));
      }

      for (final var entity : map.values()) {
        machines.put(entity.id(), SuperMiningMachine.deserialize(plugin, entity));
      }
    } catch (Exception error) {
      plugin.log("Error trying find machines in the database: " + error.getMessage());
    }
  }

  public void onDisable() {
    for (SuperMiningMachine machine : machines.values()) {
      machine.onDisable();
    }
  }
}
