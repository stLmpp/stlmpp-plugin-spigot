package com.stlmpp.spigot.plugins.events.superminingmachine.entity;

public class SMMQueries {

  public static String smmInsert = """
insert into super_mining_machine (
    id, world,
    bottom_left, bottom_right, top_left, top_right,
    last_block, boost_factor, boost_times, is_running, has_finished)
values (
    ?, ?
    ?, ?, ?, ?,
    ?, ?, ?, ?, ?
);
""";

  private static String smmBlockInsert = """
insert into super_mining_machine_block (smm_id, location, type) values (?, ?, ?);
"""
}
