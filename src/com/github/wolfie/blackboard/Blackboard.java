package com.github.wolfie.blackboard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.wolfie.blackboard.annotation.ListenerMethod;
import com.github.wolfie.blackboard.exception.DuplicateListenerMethodException;
import com.github.wolfie.blackboard.exception.DuplicateRegistrationException;
import com.github.wolfie.blackboard.exception.EventNotRegisteredException;
import com.github.wolfie.blackboard.exception.IncompatibleListenerMethodException;
import com.github.wolfie.blackboard.exception.NoListenerMethodFoundException;
import com.github.wolfie.blackboard.exception.NoMatchingRegistrationFoundException;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * <p>
 * A global event handler
 * </p>
 * 
 * <p>
 * Blackboard is a <a href="http://en.wikipedia.org/wiki/Blackboard_system">
 * blackboard system</a>-based event handler. {@link Listener Listeners} can add
 * themselves as listeners for a certain {@link Event}, that are fired by
 * {@link Notifier Notifiers}.
 * </p>
 * 
 * <p>
 * To avoid cross-application message leaking, the {@link Blackboard} is an
 * instance, not a static util class. This means, the client code must handle
 * making the instance available to the application globally, if that is
 * desired. The n&iuml;ve way would be to create it as a static instance in the
 * application, but that is not thread safe (which, in some cases, might be
 * okay).
 * </p>
 * 
 * <p>
 * Using the ThreadLocal pattern<sup><a
 * href="http://vaadin.com/wiki/-/wiki/Main/ThreadLocal%20Pattern">[1]</a>,<a
 * href="http://en.wikipedia.org/wiki/Thread-local_storage">[2]</a></sup> is
 * highly encouraged in multithreaded applications.
 * </p>
 * 
 * <p>
 * Any method in this class may throw a {@link NullPointerException} upon passed
 * <code>null</code> arguments.
 * </p>
 * 
 * @author Henrik Paul
 */
public class Blackboard {

  private static class Registration {
    private final Class<? extends Listener> listener;
    private final Class<? extends Event> event;
    private final Method method;

    public Registration(final Class<? extends Listener> listener,
        final Class<? extends Event> event) {

      Method listenerMethod = null;
      for (final Method candidateMethod : listener.getMethods()) {
        final ListenerMethod annotation = candidateMethod
            .getAnnotation(ListenerMethod.class);

        if (annotation != null) {
          if (listenerMethod == null) {
            listenerMethod = candidateMethod;
          } else {
            throw new DuplicateListenerMethodException(listener,
                candidateMethod, listenerMethod);
          }
        }
      }

      if (listenerMethod != null) {
        final Class<?>[] parameterTypes = listenerMethod.getParameterTypes();
        if (parameterTypes.length != 1 || !parameterTypes[0].equals(event)) {
          throw new IncompatibleListenerMethodException(listener,
              listenerMethod, event);
        }
      } else {
        throw new NoListenerMethodFoundException(listener);
      }

      method = listenerMethod;
      this.listener = listener;
      this.event = event;
    }

    public Class<? extends Listener> getListener() {
      return listener;
    }

    public Class<? extends Event> getEvent() {
      return event;
    }

    public Method getMethod() {
      return method;
    }
  }

  private final Map<Class<? extends Event>, Registration> registrationsByEvent = Maps
      .newHashMap();
  private final Map<Class<? extends Listener>, Set<Listener>> listeners = new MapMaker()
      .weakKeys().softValues().makeMap();

  public Blackboard() {
  }

  /**
   * <p>
   * Register a unique listener/event combination with Blackboard.
   * </p>
   * 
   * <p>
   * Whenever an {@link Event} of type <tt>event</tt> is fired, all
   * {@link Listener Listeners} of type <tt>Listener</tt> are triggered.
   * </p>
   * 
   * @param listener
   *          The listener type to register with <tt>event</tt>.
   * @param event
   *          The event type to register with <tt>listener</tt>.
   * @throws DuplicateRegistrationException
   *           if <tt>listener</tt> and/or <tt>event</tt> is already previously
   *           registered.
   * @throws DuplicateListenerMethodException
   *           if <tt>listener</tt> has more than one public method annotated
   *           with {@link ListenerMethod}.
   * @throws NoListenerMethodFoundException
   *           if <tt>listener</tt> has no public methods annotated with
   *           {@link ListenerMethod}.
   * @throws IncompatibleListenerMethodException
   *           if the method annotated with {@link ListenerMethod} doesn't have
   *           exactly one argument, it being of type <tt>event</tt>.
   */
  public void register(final Class<? extends Listener> listener,
      final Class<? extends Event> event) {

    if (listener == null || event == null) {
      throw new NullPointerException("Arguments may not be null");
    }

    for (final Registration registration : registrationsByEvent.values()) {
      if (registration.getListener().equals(listener)
          || registration.getEvent().equals(event)) {
        throw new DuplicateRegistrationException(listener, event, registration
            .getListener(), registration.getEvent());
      }
    }
    registrationsByEvent.put(event, new Registration(listener, event));
  }

  /**
   * <p>
   * Register a {@link Listener} with Blackboard.
   * </p>
   * 
   * <p>
   * The <tt>Listener</tt> will receive all {@link Event Events} of a certain
   * type, according to any prior {@link #register(Class, Class) registrations}.
   * </p>
   * 
   * @param listener
   *          The Listener to register.
   */
  public void addListener(final Listener listener) {
    final Collection<Class<? extends Listener>> listenerClasses = getRegisteredListenerClass(listener
        .getClass());

    if (listenerClasses.isEmpty()) {
      throw new NoMatchingRegistrationFoundException(listener.getClass());
    }

    for (final Class<? extends Listener> listenerClass : listenerClasses) {
      Set<Listener> listenersForClass = listeners.get(listenerClass);
      if (listenersForClass == null) {
        listenersForClass = new HashSet<Listener>();
        listeners.put(listenerClass, listenersForClass);
      }

      listenersForClass.add(listener);
    }
  }

  private Collection<Class<? extends Listener>> getRegisteredListenerClass(
      final Class<? extends Listener> listenerClass) {

    final Collection<Class<? extends Listener>> listeners = Sets.newHashSet();

    for (final Registration registration : registrationsByEvent.values()) {
      final Class<? extends Listener> registeredListenerClass = registration
          .getListener();

      if (registeredListenerClass.isAssignableFrom(listenerClass)) {
        listeners.add(registeredListenerClass);
      }
    }

    return listeners;
  }

  /**
   * Remove a {@link Listener} from Blackboard.
   * 
   * @param listener
   *          The Listener to remove.
   * @return <code>true</code> iff <tt>listener</tt> was found and removed.
   */
  public boolean removeListener(final Listener listener) {
    final Set<Listener> listenersOfClass = listeners.get(listener.getClass());
    if (listenersOfClass != null) {
      return listenersOfClass.remove(listener);
    } else {
      return false;
    }
  }

  /**
   * <p>
   * Fire an {@link Event}
   * </p>
   * 
   * <p>
   * All {@link Listener Listeners} registered to listen to the given Event will
   * be notified.
   * </p>
   * 
   * @param event
   *          The Event to fire.
   * @param notifier
   *          The {@link Notifier} that fired the <tt>event</tt>. This will be
   *          injected into <tt>event</tt>, and can be accessed via
   *          {@link Event#getNotifier()}.
   * @throws EventNotRegisteredException
   *           if <tt>event</tt>'s type wasn't previously
   *           {@link #register(Class, Class) registered} with Blackboard.
   */
  public void fire(final Event event, final Notifier notifier) {
    final Registration registration = registrationsByEvent
        .get(event.getClass());
    if (registration != null) {
      final Class<? extends Listener> listenerClass = registration
          .getListener();
      final Method listenerMethod = registration.getMethod();

      final Set<Listener> listenersForClass = listeners.get(listenerClass);
      if (listenersForClass != null) {
        for (final Listener listener : listenersForClass) {
          try {
            // inject the notifier into the event.
            event.notifier = notifier;

            listenerMethod.invoke(listener, event);
          } catch (final IllegalArgumentException e) {
            e.printStackTrace();
          } catch (final IllegalAccessException e) {
            e.printStackTrace();
          } catch (final InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }
    }

    else {
      throw new EventNotRegisteredException(event.getClass());
    }
  }
}
