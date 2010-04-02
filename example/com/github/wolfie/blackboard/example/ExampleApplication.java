package com.github.wolfie.blackboard.example;

import com.github.wolfie.blackboard.Blackboard;

public class ExampleApplication {
  
  /**
   * The singleton instance (for the current thread) of {@link Blackboard}.
   */
  private final Blackboard blackboardInstance = new Blackboard();
  private static ThreadLocal<ExampleApplication> APPLICATION = new ThreadLocal<ExampleApplication>();
  
  public static void main(final String[] args) {
    
    /*
     * The ThreadLocal needs to be set each time you might change Thread. In
     * Java EE applications, you probably need to let your application implement
     * TransactionListener and in transactionStart() re-set the APPLICATION
     * instance
     */
    APPLICATION.set(new ExampleApplication());
    
    blackboard().enableLogging();
    
    // Informs Blackboard that ExampleEvents should be sent to all
    // ExampleListeners
    blackboard().register(ExampleListener.class, ExampleEvent.class);
    
    // listener1 and listener2 are interested in receiving any and all events it
    // has been registered to
    final ExampleListener listener1 = new ExampleListener();
    blackboard().addListener(listener1);
    
    final ExampleListener listener2 = new ExampleListener();
    blackboard().addListener(listener2);
    
    // The these strings will be passed to the previous ExampleListeners without
    // any direct connection. When the application is run, these strings will
    // appear twice in the console, since there are two listeners
    new ExampleNotifier("Hello listeners");
    new ExampleNotifier("How are you doing?");
  }
  
  public static ExampleApplication getCurrent() {
    return APPLICATION.get();
  }
  
  public static Blackboard blackboard() {
    return getCurrent().blackboardInstance;
  }
}
