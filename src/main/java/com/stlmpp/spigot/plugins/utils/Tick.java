package com.stlmpp.spigot.plugins.utils;

public class Tick {

  public static long fromSeconds(long seconds) {
    return seconds * 20;
  }

  public static long fromSeconds(double seconds) {
    return Math.round(seconds * 20);
  }

  public static long fromMinutes(long minutes) {
    return minutes * 60 * 20;
  }
}
