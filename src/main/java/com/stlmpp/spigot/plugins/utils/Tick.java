package com.stlmpp.spigot.plugins.utils;

public class Tick {

  public static int fromSeconds(int seconds) {
    return seconds * 20;
  }

  public static int fromMinutes(int minutes) {
    return minutes * 60 * 20;
  }
}
