package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.Notifier;
import com.github.wolfie.blackboard.annotation.ListenerMethod;

/**
 * This class does nothing on its own. Once an {@link ExampleEvent} is fired by
 * Blackboard, the {@link #listenerMethod(ExampleEvent)} will be triggered,
 * since it is annotated with {@link ListenerMethod @ListenerMethod}.
 * 
 * @author Henrik Paul
 */
public class ExampleListener implements Listener {
  
  /**
   * <p>
   * The method that is triggered when a suitable {@link Notifier} fires an
   * event.
   * </p>
   * 
   * <p>
   * In this example, the notifier is an {@link ExampleNotifier}. The event
   * contains a message that has been passed into the <tt>ExampleNotifier</tt>.
   * </p>
   * 
   * @param event
   *          The {@link Event} that is sent by the notifier. In this example,
   *          it is an {@link ExampleEvent}.
   */
  @ListenerMethod
  public void listenerMethod(final ExampleEvent event) {
    final Notifier notifier = event.getNotifier();
    System.out.println(String.format(
        "[App] %s noticed that %s sent the following message: %s", //
        toString(), //
        notifier.toString(), //
        event.getMessage()) //
        );
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
  }
}
