package com.github.wolfie.blackboard;

import java.util.Random;

import com.github.wolfie.blackboard.annotation.ListenerMethod;

public class ConcurrentTest {

  private interface TestListener extends Listener {
    @ListenerMethod
    void trigger(TestEvent event);
  }

  private static class TestEvent implements Event {
  }

  private static class TestListenerImpl implements TestListener {
    public void trigger(final TestEvent event) {
    }
  }

  private static class Runner extends Thread {
    @Override
    public void run() {
      try {
        Thread.sleep(100 + R.nextInt(10));

        for (int i = 0; i < 1000; i++) {
          BLACKBOARD.fire(new TestEvent());
        }
      } catch (final InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  private static final Blackboard BLACKBOARD = new Blackboard();
  private static Random R = new Random();

  public static void main(final String[] args) {

    BLACKBOARD.register(TestListener.class, TestEvent.class);
    BLACKBOARD.addListener(new TestListenerImpl());
    BLACKBOARD.addListener(new TestListenerImpl());
    BLACKBOARD.addListener(new TestListenerImpl());
    BLACKBOARD.addListener(new TestListenerImpl());
    BLACKBOARD.addListener(new TestListenerImpl());

    for (int i = 0; i < 10; i++) {
      new Runner().start();
    }
  }
}
