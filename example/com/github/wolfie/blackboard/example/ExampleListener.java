package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Listener;
import com.github.wolfie.blackboard.Notifier;
import com.github.wolfie.blackboard.annotation.ListenerMethod;

public class ExampleListener implements Listener {
  
  @ListenerMethod
  public void listenerMethod(final ExampleEvent event) {
    final Notifier notifier = event.getNotifier();
    System.out.println(String.format(
        "%s noticed that %s sent the following message: %s", //
        toString(), //
        notifier.toString(), //
        event.getMessage()) //
        );
  }
}
