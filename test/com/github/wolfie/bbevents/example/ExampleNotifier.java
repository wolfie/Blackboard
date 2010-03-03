package com.github.wolfie.bbevents.example;

import com.github.wolfie.bbevents.Notifier;

public class ExampleNotifier implements Notifier {
  public ExampleNotifier(final String message) {
    ExampleApplication.BLACKBOARD.fire(new ExampleEvent(message), this);
  }
}
