package com.github.wolfie.blackboard;

public abstract class Event {
  Notifier notifier;

  public final Notifier getNotifier() {
    return notifier;
  }
}
