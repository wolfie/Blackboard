package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Notifier;

public class ExampleNotifier implements Notifier {
  public ExampleNotifier(final String message) {
    ExampleApplication.blackboard().fire(new ExampleEvent(message), this);
  }
}
