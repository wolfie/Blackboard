package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.example.TestMessageEvent.TestMessageListener;

/**
 * A class that listens for {@link TestMessageEvent TestMessageEvents} and
 * echoes them out immediately.
 */
public class MessageConsumer implements TestMessageListener {

  private final int id;

  public MessageConsumer(final int id) {
    this.id = id;
  }

  /**
   * This method an example implementation of
   * {@link TestMessageListener#receiveTestMessage(TestMessageEvent)}, defined
   * by the listener interface.
   */
  public void receiveTestMessage(final TestMessageEvent event) {
    final String message = event.getMessage();

    System.out
        .println("[MSG] " + this + " got the message \"" + message + "\"");
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + id;
  }
}
