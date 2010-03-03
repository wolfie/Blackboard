package com.github.wolfie.blackboard;

import org.junit.Before;
import org.junit.Test;

import com.github.wolfie.blackboard.annotation.ListenerMethod;
import com.github.wolfie.blackboard.exception.DuplicateListenerMethodException;
import com.github.wolfie.blackboard.exception.DuplicateRegistrationException;
import com.github.wolfie.blackboard.exception.IncompatibleListenerMethodException;
import com.github.wolfie.blackboard.exception.NoListenerMethodFoundException;

public class BlackboardTest {
  
  public class IncompatibleEventListener implements Listener {
    @ListenerMethod
    public void method(final String event) {
    }
  }
  
  public class IncompatibleMethodCountListener implements Listener {
    @ListenerMethod
    public void method(final TestEvent event1, final TestEvent event2) {
    }
  }
  
  private class NoMethodListener implements Listener {
  }
  
  private class TooManyMethodsListener implements Listener {
    @ListenerMethod
    public void first(final TestEvent event) {
    }
    
    @ListenerMethod
    public void second(final TestEvent event) {
    }
  }
  
  private class TestListener implements Listener {
    @ListenerMethod
    public void listenerMethod(final TestEvent event) {
    }
  }
  
  private class SecondTestListener implements Listener {
    @ListenerMethod
    public void listenerMethod(final TestEvent event) {
    }
  }
  
  private class TestEvent extends Event {
  }
  
  private class SecondTestEvent extends Event {
  }
  
  private Blackboard blackboard;
  
  @Before
  public void setUp() {
    blackboard = new Blackboard();
  }
  
  @Test
  public void testRegistration() {
    blackboard.register(TestListener.class, TestEvent.class);
  }
  
  @Test(expected = NullPointerException.class)
  public void testNullRegistrationBoth() {
    blackboard.register(null, null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testNullRegistrationFirst() {
    blackboard.register(null, TestEvent.class);
  }
  
  @Test(expected = NullPointerException.class)
  public void testNullRegistrationSecond() {
    blackboard.register(TestListener.class, null);
  }
  
  @Test(expected = NoListenerMethodFoundException.class)
  public void testRegistrationWithNoMethodListener() {
    blackboard.register(NoMethodListener.class, TestEvent.class);
  }
  
  @Test(expected = DuplicateListenerMethodException.class)
  public void testRegistrationWithDuplicateMethodsListener() {
    blackboard.register(TooManyMethodsListener.class, TestEvent.class);
  }
  
  @Test(expected = DuplicateRegistrationException.class)
  public void testDuplicateRegistrationSame() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.register(TestListener.class, TestEvent.class);
  }
  
  @Test(expected = DuplicateRegistrationException.class)
  public void testDuplicateRegistrationFirst() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.register(TestListener.class, SecondTestEvent.class);
  }
  
  @Test(expected = DuplicateRegistrationException.class)
  public void testDuplicateRegistrationSecond() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.register(SecondTestListener.class, TestEvent.class);
  }
  
  @Test(expected = IncompatibleListenerMethodException.class)
  public void testIncompatibleArgumentRegistration() {
    blackboard.register(IncompatibleEventListener.class, TestEvent.class);
  }
  
  @Test(expected = IncompatibleListenerMethodException.class)
  public void testIncompatibleMethodCountRegistration() {
    blackboard.register(IncompatibleMethodCountListener.class, TestEvent.class);
  }
}
