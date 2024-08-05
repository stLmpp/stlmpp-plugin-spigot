package com.stlmpp.spigot.plugins.events.superminingmachine.entity;

public class SMMQueries {

  public static final String smmInsert =
      """
insert into super_mining_machine (
    id, world,
    bottom_left, bottom_right, top_left, top_right,
    last_block, boost_factor, boost_times, is_running, has_finished)
values (
    ?, ?,
    ?, ?, ?, ?,
    ?, ?, ?, ?, ?
);
""";

  public static final String smmBlockInsertBase =
      "insert into super_mining_machine_block (smm_id, location, type) values ";

  public static final String smmBlockInsert = smmBlockInsertBase + "(?, ?, ?)";

  public static final String delete = "delete from super_mining_machine where id = ?;";

  public static final String deleteBlocks =
      "delete from super_mining_machine_block where smm_id = ?;";

  public static final String selectAll =
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
""";

  public static final String updateState =
      """
update super_mining_machine
   set is_running = ?,
       has_finished = ?,
       last_block = ?,
       boost_factor = ?,
       boost_times = ?
 where id = ?
""";

  public static final String ddl =
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
create table if not exists super_mining_machine_block (
    id integer primary key autoincrement,
    smm_id text not null,
    location text not null,
    type text not null,
    foreign key (smm_id) references super_mining_machine (id)
);
""";
}
