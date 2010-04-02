package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Notifier;

public class ExampleNotifier implements Notifier {
  /**
   * <p>
   * An example implementation of a {@link Notifier}.
   * </p>
   * 
   * <p>
   * This constructor will send a given message to any and all listeners,
   * without any direct connection. In this example, the listener will be a
   * {@link ExampleListener}.
   * </p>
   * 
   * @param message
   *          The message to be sent to the listeners.
   */
  public ExampleNotifier(final String message) {
    ExampleApplication.blackboard().fire(new ExampleEvent(message), this);
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
  }
}
