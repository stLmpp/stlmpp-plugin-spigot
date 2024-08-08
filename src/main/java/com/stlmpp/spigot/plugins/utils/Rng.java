package com.stlmpp.spigot.plugins.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Rng {

  public static Integer nextInt(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }

  public static boolean chance(int percent) {
    return ThreadLocalRandom.current().nextInt(0, 100) < percent;
  }

  public static boolean chance(double percent) {
    return ThreadLocalRandom.current().nextDouble(0, 100) < percent;
  }
}
