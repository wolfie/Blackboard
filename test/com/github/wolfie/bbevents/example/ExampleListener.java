package com.github.wolfie.bbevents.example;

import com.github.wolfie.bbevents.Listener;
import com.github.wolfie.bbevents.ListenerMethod;

public class ExampleListener implements Listener {

  @ListenerMethod
  public void listenerMethod(final ExampleEvent event) {
    System.out.println(toString() + " noticed that " + event.getNotifier()
        + " sent the following message: " + event.getMessage());
  }
}
