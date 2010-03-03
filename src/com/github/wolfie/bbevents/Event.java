package com.github.wolfie.bbevents;

public abstract class Event {
  Notifier notifier;

  public final Notifier getNotifier() {
    return notifier;
  }
}
