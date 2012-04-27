package com.github.wolfie.blackboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.github.wolfie.blackboard.annotation.ListenerMethod;
import com.github.wolfie.blackboard.exception.DuplicateListenerMethodException;
import com.github.wolfie.blackboard.exception.DuplicateRegistrationException;
import com.github.wolfie.blackboard.exception.InvalidListenerMethodConstruction;
import com.github.wolfie.blackboard.exception.NoSuitableListenerMethodFoundException;

public class BlackboardTest {

  private abstract class AbstractTestEvent implements Event {
  }

  private interface ListenerForAbstractEvent extends Listener {
    @ListenerMethod
    void method(AbstractTestEvent event);
  }

  private interface IncompatibleEventListener extends Listener {
    @ListenerMethod
    void method(final Event event);
  }

  private interface InvalidMethodArgumentListener extends Listener {
    @ListenerMethod
    void method(final String event);
  }

  private interface IncompatibleMethodCountListener extends Listener {
    @ListenerMethod
    void method(final TestEvent event1, final TestEvent event2);
  }

  private interface NoMethodListener extends Listener {
  }

  private interface TooManyMethodsListener extends Listener {
    @ListenerMethod
    public void first(final TestEvent event);

    @ListenerMethod
    public void second(final TestEvent event);
  }

  private interface TestListener extends Listener {
    @ListenerMethod
    void listenerMethod(TestEvent event);
  }

  private static class TestListenerImpl implements TestListener {
    private boolean isTriggered = false;

    @ListenerMethod
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

  private class TestEvent implements Event {
  }

  private class SecondTestEvent implements Event {
  }

  private class TestNotifier {
    void notify(final TestEvent event) {
      blackboard.fire(event);
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

  private static class MultiListener implements MultiListenerOneListener,
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

  private static class NonInterfaceListener implements Listener {
    @SuppressWarnings("unused")
    @ListenerMethod
    public void listenerMethd(final TestEvent event) {
    }
  }

  private class MultiListenerOneEvent implements Event {
  }

  private class MultiListenerTwoEvent implements Event {
  }

  public interface MultiEventPerListener extends Listener {
    @ListenerMethod
    public void eventOne(EventOne event);

    @ListenerMethod
    public void eventTwo(EventTwo event);
  }

  public static class MultiEventPerListenerImpl implements
      MultiEventPerListener {
    private boolean one = false;
    private boolean two = false;

    public void eventOne(final EventOne event) {
      one = true;
    }

    public void eventTwo(final EventTwo event) {
      two = true;
    }

    public boolean eventOneCalled() {
      return one;
    }

    public boolean eventTwoCalled() {
      return two;
    }
  }

  public static class EventOne implements Event {
  }

  public static class EventTwo implements Event {
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

  public void testRegisteringListenerClass() {
    blackboard.register(NonInterfaceListener.class, TestEvent.class);
  }

  @Test(expected = NoSuitableListenerMethodFoundException.class)
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
  public void testDuplicateRegistrationOfEvent() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.register(SecondTestListener.class, TestEvent.class);
  }

  @Test(expected = InvalidListenerMethodConstruction.class)
  public void testInvalidListenerArgumentRegistration() {
    blackboard.register(InvalidMethodArgumentListener.class, TestEvent.class);
  }

  @Test(expected = NoSuitableListenerMethodFoundException.class)
  public void testIncompatibleEventRegistration() {
    blackboard.register(IncompatibleEventListener.class, TestEvent.class);
  }

  @Test(expected = InvalidListenerMethodConstruction.class)
  public void testIncompatibleMethodCountRegistration() {
    blackboard.register(IncompatibleMethodCountListener.class, TestEvent.class);
  }

  @Test
  public void testTwoListenersInOneClass() {
    blackboard.register(MultiListenerOneListener.class,
        MultiListenerOneEvent.class);
    blackboard.register(MultiListenerTwoListener.class,
        MultiListenerTwoEvent.class);

    final MultiListener multiListener = new MultiListener();
    blackboard.addListener(multiListener);

    blackboard.fire(new MultiListenerOneEvent());
    blackboard.fire(new MultiListenerTwoEvent());

    assertTrue("First event wasn't caught", multiListener.is1Triggered());
    assertTrue("Second event wasn't caught", multiListener.is2Triggered());
  }

  @Test
  public void testListenerHasNoEventToBeginWith() {
    blackboard.register(TestListener.class, TestEvent.class);

    final TestListenerImpl testListener = new TestListenerImpl();
    assertFalse(testListener.isTriggered());
  }

  @Test
  public void testListenerDoesntHearWithoutAdding() {
    blackboard.register(TestListener.class, TestEvent.class);

    final TestListenerImpl listener = new TestListenerImpl();

    new TestNotifier().notify(new TestEvent());

    assertFalse(listener.isTriggered());
  }

  @Test
  public void testListenerHearsWithAdding() {
    blackboard.register(TestListener.class, TestEvent.class);

    final TestListenerImpl listener = new TestListenerImpl();
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
    blackboard.removeListener(new TestListenerImpl());
  }

  @Test
  public void testRemoveListenerImplementingOneInterface() {
    blackboard.register(TestListener.class, TestEvent.class);

    final TestListenerImpl listener = new TestListenerImpl();
    blackboard.addListener(listener);
    assertTrue("Removing listener failed", blackboard.removeListener(listener));

    new TestNotifier().notify(new TestEvent());

    assertFalse("Event was triggered, when it shouldn't",
        listener.isTriggered());
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

    blackboard.fire(new MultiListenerOneEvent());
    blackboard.fire(new MultiListenerTwoEvent());

    assertFalse("First event was triggered, when it shouldn't",
        listener.is1Triggered());
    assertFalse("Second event was triggered, when it shouldn't",
        listener.is2Triggered());
  }

  @Test(expected = NullPointerException.class)
  public void testFiringNullEvent() {
    blackboard.register(TestListener.class, TestEvent.class);
    blackboard.fire(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRegisteringAbstractEvent() {
    blackboard
        .register(ListenerForAbstractEvent.class, AbstractTestEvent.class);
  }

  @Test
  public void testMultiEventPerListenerWithRegistration() {
    blackboard.register(MultiEventPerListener.class, EventOne.class);
    blackboard.register(MultiEventPerListener.class, EventTwo.class);

    _callMultiEventPerListener();
  }

  @Test
  public void testMultiEventPerListenerWithoutRegistration() {
    _callMultiEventPerListener();
  }

  private void _callMultiEventPerListener() {
    final MultiEventPerListenerImpl obj = new MultiEventPerListenerImpl();
    blackboard.addListener(obj);

    blackboard.fire(new EventOne());
    assertTrue("Event one wasn't called", obj.eventOneCalled());

    blackboard.fire(new EventTwo());
    assertTrue("Event two wasn't called", obj.eventTwoCalled());
  }
}
