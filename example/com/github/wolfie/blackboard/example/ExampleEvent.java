package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Event;

public class ExampleEvent extends Event {
  private final String message;

  public ExampleEvent(final String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
