package com.github.wolfie.bbevents.example;

import com.github.wolfie.bbevents.Event;

public class ExampleEvent extends Event {
  private final String message;

  public ExampleEvent(final String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
