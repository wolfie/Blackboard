package com.github.wolfie.bbevents.example;

import com.github.wolfie.bbevents.Blackboard;

public class ExampleApplication {

  public static Blackboard BLACKBOARD = new Blackboard();

  public static void main(final String[] args) {
    BLACKBOARD.register(ExampleListener.class, ExampleEvent.class);

    final ExampleListener listener1 = new ExampleListener();
    BLACKBOARD.addListener(listener1);

    final ExampleListener listener2 = new ExampleListener();
    BLACKBOARD.addListener(listener2);

    new ExampleNotifier("Hello listeners");
    new ExampleNotifier("How are you doing?");
  }
}
