package com.stlmpp.spigot.plugins.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Chance {

  public static boolean of(int percent) {
    return ThreadLocalRandom.current().nextInt(0, 100) < percent;
  }

  public static boolean of(double percent) {
    return ThreadLocalRandom.current().nextDouble(0, 100) < percent;
  }
}
