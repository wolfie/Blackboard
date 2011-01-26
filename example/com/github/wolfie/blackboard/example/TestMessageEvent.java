package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

/**
 * The event that is passed from {@link ExampleNotifier} to
 * {@link com.github.wolfie.blackboard.example.TestMessageEvent.TestMessageListener
 * TestMessageListener} .
 * <p/>
 * This class is simply a event wrapper for a string message.
 */
public class TestMessageEvent implements Event {

  /**
   * This interface represents a typical listener.
   * <p/>
   * The class name describes clearly what it is listening for and the method
   * name describes naturally what the listener is trying to do.
   */
  public interface TestMessageListener extends Listener {
    public void receiveTestMessage(final TestMessageEvent event);
  }

  private final String message;

  /**
   * Creates a new {@link TestMessageEvent} with a given message that will be
   * passed from notifier to listener.
   * 
   * @param message
   *          The message to pass
   */
  public TestMessageEvent(final String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + message;
  }
}
