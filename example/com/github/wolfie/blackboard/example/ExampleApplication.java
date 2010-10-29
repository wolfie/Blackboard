package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Blackboard;
import com.github.wolfie.blackboard.example.TestMessageEvent.TestMessageListener;

public class ExampleApplication {

  private static final Blackboard BLACKBOARD = new Blackboard();

  public static void main(final String[] args) {
    BLACKBOARD.enableLogging();

    /*
     * Informs Blackboard that all TestMessageEvents should be sent to all
     * objects that implement TestMessageListener.
     * 
     * Keeping all registrations in one central place will make the code easier
     * to read, and mitigates application bugs regarding late registration.
     */
    BLACKBOARD.register(TestMessageListener.class, TestMessageEvent.class);

    /*
     * Adds the two objects as listeners into Blackboard. Blackboard will now
     * inform these objects whenever an event is triggered that is registered to
     * their interface.
     * 
     * The MessageConsumer implements TestMessageListener, which listens to
     * TestMessageEvents. When the event is fired, the MessageConsumer prints
     * out the message in the event.
     * 
     * Note that order is not guaranteed by Blackboard. I.e. the order in which
     * the listeners are triggered is probably not the same in which the
     * listeners were added.
     */
    BLACKBOARD.addListener(new MessageConsumer(1));
    BLACKBOARD.addListener(new MessageConsumer(2));

    /*
     * These strings will be passed to the two MessageConsumers just added to
     * blackboard, without any apparent direct connection.
     * 
     * Each of these strings will appear twice in the console, since there are
     * two listeners interested in them.
     */
    sendString("Hello listeners");
    sendString("How are you doing?");
  }

  private static void sendString(final String message) {
    BLACKBOARD.fire(new TestMessageEvent(message));
  }
}
