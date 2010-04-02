package com.github.wolfie.blackboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.github.wolfie.blackboard.annotation.ListenerMethod;
import com.github.wolfie.blackboard.exception.DuplicateListenerMethodException;
import com.github.wolfie.blackboard.exception.DuplicateRegistrationException;
import com.github.wolfie.blackboard.exception.IncompatibleListenerMethodException;
import com.github.wolfie.blackboard.exception.NoListenerMethodFoundException;
import com.github.wolfie.blackboard.exception.NoMatchingRegistrationFoundException;

public class BlackboardTest {
  
  private class IncompatibleEventListener implements Listener {
    @SuppressWarnings("unused")
    @ListenerMethod
    public void method(final Event event) {
    }
  }
  
  private class InvalidMethodArgumentListener implements Listener {
    @SuppressWarnings("unused")
    @ListenerMethod
    public void method(final String event) {
    }
  }
  
  private class IncompatibleMethodCountListener implements Listener {
    @SuppressWarnings("unused")
    @ListenerMethod
    public void method(final TestEvent event1, final TestEvent event2) {
    }
  }
  
  private class NoMethodListener implements Listener {
  }
  
  private class TooManyMethodsListener implements Listener {
    @ListenerMethod
    @SuppressWarnings("unused")
    public void first(final TestEvent event) {
    }
    
    @ListenerMethod
    @SuppressWarnings("unused")
    public void second(final TestEvent event) {
    }
  }
  
  private class TestListener implements Listener {
    
    private boolean isTriggered = false;
    
    @ListenerMethod
    @SuppressWarnings("unused")
    public void listenerMethod(final TestEvent event) {
      isTriggered = true;
    }
    
    public boolean isTriggered() {
      return isTriggered;
    }
  }
  
  private class SecondTestListener implements Listener {
    @ListenerMethod
    @SuppressWarnings("unused")
    public void listenerMethod(final TestEvent event) {
    }
  }
  
  private class TestEvent extends Event {
  }
  
  private class SecondTestEvent extends Event {
  }
  
  private class TestNotifier implements Notifier {
    void notify(final TestEvent event) {
      blackboard.fire(event, this);
    }
  }
  
  private interface MultiListenerOneListener extends Listener {
    @ListenerMethod
    void trigger1(MultiListenerOneEvent e);
  }
  
  private interface MultiListenerTwoListener extends Listener {
    @ListenerMethod
    void trigger2(MultiListenerTwoEvent e);
  }
  
  private class MultiListener implements MultiListenerOneListener,
      MultiListenerTwoListener {
    
    private boolean trigger1 = false;
    private boolean trigger2 = false;
    
    public void trigger1(final MultiListenerOneEvent e) {
      trigger1 = true;
    }
    
    public void trigger2(final MultiListenerTwoEvent e) {
      trigger2 = true;
    }
    
    public boolean is1Triggered() {
      return trigger1;
    }
    
    public boolean is2Triggered() {
      return trigger2;
    }
  }
  
  private class MultiListenerOneEvent extends Event {
  }
  
  private class MultiListenerTwoEvent extends Event {
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
  public void testRegisteringNullListenerAndNullEvent() {
    blackboard.register(null, null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testRegisteringNullListener() {
    blackboard.register(null, TestEvent.class);
  }
  
  @Test(expected = NullPointerException.class)
  public void testRegisteringNullEvent() {
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
  public void testDuplicateRegistration() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.register(TestListener.class, TestEvent.class);
  }
  
  @Test(expected = DuplicateRegistrationException.class)
  public void testDuplicateRegistrationOfListener() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.register(TestListener.class, SecondTestEvent.class);
  }
  
  @Test(expected = DuplicateRegistrationException.class)
  public void testDuplicateRegistrationOfEvent() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.register(SecondTestListener.class, TestEvent.class);
  }
  
  @Test(expected = IncompatibleListenerMethodException.class)
  public void testInvalidListenerArgumentRegistration() {
    blackboard.register(InvalidMethodArgumentListener.class, TestEvent.class);
  }
  
  @Test(expected = IncompatibleListenerMethodException.class)
  public void testIncompatibleEventRegistration() {
    blackboard.register(IncompatibleEventListener.class, TestEvent.class);
  }
  
  @Test(expected = IncompatibleListenerMethodException.class)
  public void testIncompatibleMethodCountRegistration() {
    blackboard.register(IncompatibleMethodCountListener.class, TestEvent.class);
  }
  
  @Test(expected = NoMatchingRegistrationFoundException.class)
  public void testAddListenerWithoutRegistration() {
    blackboard.addListener(new TestListener());
  }
  
  @Test
  public void testTwoListenersInOneClass() {
    blackboard.register(MultiListenerOneListener.class,
        MultiListenerOneEvent.class);
    blackboard.register(MultiListenerTwoListener.class,
        MultiListenerTwoEvent.class);
    
    final MultiListener multiListener = new MultiListener();
    blackboard.addListener(multiListener);
    
    final Notifier notifier = new Notifier() {
    };
    
    blackboard.fire(new MultiListenerOneEvent(), notifier);
    blackboard.fire(new MultiListenerTwoEvent(), notifier);
    
    assertTrue("First event wasn't caught", multiListener.is1Triggered());
    assertTrue("Second event wasn't caught", multiListener.is2Triggered());
  }
  
  @Test
  public void testListenerHasNoEventToBeginWith() {
    blackboard.register(TestListener.class, TestEvent.class);
    
    final TestListener testListener = new TestListener();
    assertFalse(testListener.isTriggered());
  }
  
  @Test
  public void testListenerDoesntHearWithoutAdding() {
    blackboard.register(TestListener.class, TestEvent.class);
    
    final TestListener listener = new TestListener();
    
    new TestNotifier().notify(new TestEvent());
    
    assertFalse(listener.isTriggered());
  }
  
  @Test
  public void testListenerHearsWithAdding() {
    blackboard.register(TestListener.class, TestEvent.class);
    
    final TestListener listener = new TestListener();
    blackboard.addListener(listener);
    
    new TestNotifier().notify(new TestEvent());
    
    assertTrue(listener.isTriggered());
  }
  
  @Test(expected = NullPointerException.class)
  public void testRemoveNullListener() {
    blackboard.removeListener(null);
  }
  
  @Test
  public void testRemoveNonExistingListener() {
    blackboard.removeListener(new TestListener());
  }
  
  @Test
  public void testRemoveListenerImplementingOneInterface() {
    blackboard.register(TestListener.class, TestEvent.class);
    
    final TestListener listener = new TestListener();
    blackboard.addListener(listener);
    assertTrue("Removing listener failed", blackboard.removeListener(listener));
    
    new TestNotifier().notify(new TestEvent());
    
    assertFalse("Event was triggered, when it shouldn't", listener
        .isTriggered());
  }
  
  @Test
  public void testRemoveListenerImplementingTwoInterfaces() {
    blackboard.register(MultiListenerOneListener.class,
        MultiListenerOneEvent.class);
    blackboard.register(MultiListenerTwoListener.class,
        MultiListenerTwoEvent.class);
    
    final MultiListener listener = new MultiListener();
    blackboard.addListener(listener);
    assertTrue("Removing listener failed", blackboard.removeListener(listener));
    
    final Notifier notifier = new Notifier() {
    };
    
    blackboard.fire(new MultiListenerOneEvent(), notifier);
    blackboard.fire(new MultiListenerTwoEvent(), notifier);
    
    assertFalse("First event was triggered, when it shouldn't", listener
        .is1Triggered());
    assertFalse("Second event was triggered, when it shouldn't", listener
        .is2Triggered());
  }
  
  @Test(expected = NullPointerException.class)
  public void testFiringNullEventWithNullNotifier() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.fire(null, null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testFiringNullEvent() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.fire(null, new TestNotifier());
  }
  
  @Test(expected = NullPointerException.class)
  public void testFiringWithNullNotifier() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.fire(new TestEvent(), null);
  }
}
