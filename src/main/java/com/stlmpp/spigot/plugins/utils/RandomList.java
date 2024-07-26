package com.stlmpp.spigot.plugins.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomList<T> {
  public RandomList(List<T> list) {
    assert !list.isEmpty() : "list is empty";
    this.list = list;
  }

  private final List<T> list;

  public T next() {
    int randomElementIndex = ThreadLocalRandom.current().nextInt(0, this.list.size());
    return this.list.get(randomElementIndex);
  }
}
