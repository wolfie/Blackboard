package com.github.wolfie.blackboard;

import com.github.wolfie.blackboard.annotation.ListenerMethod;

public class MemLeakTest {
  static class MemTestListener implements Listener {
    @ListenerMethod
    public void method(final MemTestEvent event) {
    }
  }
  
  static class MemTestEvent extends Event {
  }
  
  private static final Blackboard BLACKBOARD = new Blackboard();
  private static final int TEST_MAX = 10000000;
  private static final Runtime RUNTIME = Runtime.getRuntime();
  
  public static void main(final String args[]) {
    BLACKBOARD.register(MemTestListener.class, MemTestEvent.class);
    
    for (int i = 0; i < TEST_MAX; i++) {
      BLACKBOARD.addListener(new MemTestListener());
      if (i % 10000 == 0) {
        final long freeMemory = RUNTIME.freeMemory();
        final long totalMemory = RUNTIME.totalMemory();
        System.out.println(i + ": " + ((totalMemory - freeMemory) / 1024)
            + "KB");
      }
    }
    
    System.out.println(TEST_MAX + " iterations passed!");
  }
}
