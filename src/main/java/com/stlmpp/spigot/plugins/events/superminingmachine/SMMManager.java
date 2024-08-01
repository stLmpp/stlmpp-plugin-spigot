package com.stlmpp.spigot.plugins.events.superminingmachine;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.StlmppPluginConfig;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMBlockEntity;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMEntity;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMQueries;
import com.stlmpp.spigot.plugins.events.superminingmachine.entity.SMMStateUpdateDto;
import com.stlmpp.spigot.plugins.utils.Util;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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
    this.prepareDatabase();
    SMMCreationEvent.register(plugin);
    SMMStartEvent.register(plugin);
    SMMDestroyEvent.register(plugin);
    this.onEnable();
  }

  private void prepareDatabase() {
    try (final var statement = plugin.getDatabaseConnection().createStatement()) {
      statement.executeUpdate(
          """
create table if not exists super_mining_machine(
    id text primary key,
    world text not null,
    bottom_left text not null,
    bottom_right text not null,
    top_left text not null,
    top_right text not null,
    last_block text null,
    boost_factor real not null,
    boost_times int not null,
    is_running int not null,
    has_finished int not null
);
""");
      statement.executeUpdate(
          """
create table if not exists super_mining_machine_block (
    id integer primary key autoincrement,
    smm_id text not null,
    location text not null,
    type text not null,
    foreign key (smm_id) references super_mining_machine (id)
);
""");
    } catch (SQLException error) {
      error.printStackTrace();
      throw new RuntimeException(error.getMessage());
    }
  }

  private final StlmppPlugin plugin;

  private final HashMap<String, SuperMiningMachine> machines = new HashMap<>();

  public boolean isOverlappingAnotherMachine(BoundingBox boundingBox) {
    return machines.values().stream()
        .anyMatch(
            machine -> {
              final var extendedBoundingBox =
                  machine.boundingBox.clone().expand(0, 64, 0, 0, 320, 0);
              return extendedBoundingBox.overlaps(boundingBox);
            });
  }

  @Nullable
  public SuperMiningMachine getMachineByNetheriteBlock(Block block) {
    return Util.findFirst(this.machines.values(), machine -> machine.hasNetheriteBlock(block));
  }

  @Nullable
  public SuperMiningMachine getMachineByBlock(Block block) {
    return Util.findFirst(this.machines.values(), machine -> machine.hasBlock(block));
  }

  public boolean isWorldValid(@NotNull World world) {
    return world.getName().equals(this.plugin.getWorldName())
        || world.getName().equals(this.plugin.getWorldNetherName());
  }

  public boolean isBlockTypeValid(@NotNull Material material) {
    return material.equals(Material.NETHERITE_BLOCK) || material.equals(Material.OBSIDIAN);
  }

  public void addMachine(SuperMiningMachine machine) {
    this.machines.put(machine.getId(), machine);
    final var entity = machine.serialize();
    var insertQuery = new StringBuilder(SMMQueries.smmInsert);
    for (final var block : entity.blocks()) {
      insertQuery.append()
    }
    try (final var statement =
        plugin
            .getDatabaseConnection()
            .prepareStatement(
                """
insert into super_mining_machine (
    id, world,
    bottom_left, bottom_right, top_left, top_right,
    last_block, boost_factor, boost_times, is_running, has_finished)
values (
    ?, ?
    ?, ?, ?, ?,
    ?, ?, ?, ?, ?
);

""")) {
      final var entity = machine.serialize();
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
      statement.execute();
    } catch (SQLException error) {
      plugin.log("Error trying to insert the machine: " + error.getMessage());
    }
  }

  public void removeMachine(SuperMiningMachine machine) {
    this.machines.remove(machine.getId());
    // TODO persist machine
  }

  public void onEnable() throws SQLException {
    try (final var statement = plugin.getDatabaseConnection().createStatement()) {
      final var results =
          statement.executeQuery(
              """
select world,
       bottom_left,
       bottom_right,
       top_left,
       top_right,
       last_block,
       boost_factor,
       boost_times,
       is_running,
       has_finished,
       b.id b_id,
       smm_id,
       location,
       type
from super_mining_machine s
         inner join super_mining_machine_block b on b.smm_id = s.id;
""");
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
    } catch (SQLException error) {
      plugin.log("Error trying find machines in the database: " + error.getMessage());
    }
  }

  public void updateState(String id, SMMStateUpdateDto dto) {
    try (final var statement =
        plugin
            .getDatabaseConnection()
            .prepareStatement(
                """
update super_mining_machine
   set is_running = ?,
       has_finished = ?,
       last_block = ?,
       boost_factor = ?,
       boost_times = ?
 where id = ?
""")) {
      statement.setInt(1, dto.isRunning());
      statement.setInt(2, dto.hasFinished());
      statement.setString(3, dto.lastBlock());
      statement.setDouble(4, dto.boostFactor());
      statement.setInt(5, dto.boostTimes());
      statement.setString(6, id);
      statement.execute();
    } catch (SQLException error) {
      plugin.log("Error trying to update the state: " + error.getMessage());
    }
  }

  public void onDisable() {
    for (SuperMiningMachine machine : machines.values()) {
      machine.stop();
    }
  }
}
