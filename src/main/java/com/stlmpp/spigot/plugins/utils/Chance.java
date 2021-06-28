package com.stlmpp.spigot.plugins.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Chance {

  public static boolean of(int percent) {
    return ThreadLocalRandom.current().nextInt(0, 101) < percent;
  }
}
