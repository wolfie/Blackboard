package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Event;

/**
 * The event that is passed from {@link ExampleNotifier} to
 * {@link ExampleListener}.
 * 
 * @author Henrik Paul
 */
public class ExampleEvent extends Event {
  private final String message;
  
  /**
   * Creates a new {@link ExampleEvent} with a given message that will be passed
   * from notifier to listener.
   * 
   * @param message
   *          The message to pass
   */
  public ExampleEvent(final String message) {
    this.message = message;
  }
  
  public String getMessage() {
    return message;
  }
}
