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

  public static Boolean nextBoolean() {
    return ThreadLocalRandom.current().nextBoolean();
  }

  public static Double nextDouble(double min, double max) {
    return ThreadLocalRandom.current().nextDouble(min, max + 1);
  }

  public static Float nextFloat(float min, float max) {
    return ThreadLocalRandom.current().nextFloat(min, max + 1);
  }
}
