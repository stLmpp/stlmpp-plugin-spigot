package com.stlmpp.spigot.plugins.utils;

import java.util.ArrayList;
import java.util.List;

public class RandomList<T> {
  public RandomList(List<T> list) {
    assert !list.isEmpty() : "list is empty";
    this.list = list;
  }

  public RandomList() {
    this(new ArrayList<>());
  }

  private final List<T> list;

  public T next() {
    assert !list.isEmpty() : "list is empty";
    int randomElementIndex = Rng.nextInt(0, this.list.size() - 1);
    return this.list.get(randomElementIndex);
  }

  public boolean add(T element) {
    return this.list.add(element);
  }

  public boolean isEmpty() {
    return this.list.isEmpty();
  }
}
