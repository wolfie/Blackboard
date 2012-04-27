package com.github.wolfie.blackboard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.wolfie.blackboard.annotation.ListenerMethod;
import com.github.wolfie.blackboard.annotation.ListenerPair;
import com.github.wolfie.blackboard.exception.DuplicateListenerMethodException;
import com.github.wolfie.blackboard.exception.DuplicateRegistrationException;
import com.github.wolfie.blackboard.exception.EventNotRegisteredException;
import com.github.wolfie.blackboard.exception.IncompatibleListenerMethodException;
import com.github.wolfie.blackboard.exception.InvalidListenerMethodConstruction;
import com.github.wolfie.blackboard.exception.NoMatchingRegistrationFoundException;
import com.github.wolfie.blackboard.exception.NoSuitableListenerMethodFoundException;

/**
 * <p>
 * A global event handler
 * </p>
 * 
 * <p>
 * Blackboard is a <a href="http://en.wikipedia.org/wiki/Blackboard_system">
 * blackboard system</a>-based event handler. {@link Listener Listeners} can add
 * themselves as listeners for a certain {@link Event}.
 * </p>
 * 
 * <p>
 * To avoid cross-application message leaking, the {@link Blackboard} is an
 * instance, not a static util class. This means, the client code must handle
 * making the instance available to the application globally, if that is
 * desired. The na&iuml;ve way would be to create it as a static instance in the
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
        final Class<? extends Event> event)
        throws DuplicateListenerMethodException {

      if (!listener.isInterface()) {
        throw new IllegalArgumentException(
            "Unexpected non-interface argument: " + listener);
      } else if (event.isInterface()
          || Modifier.isAbstract(event.getModifiers())) {
        throw new IllegalArgumentException(
            "Unexpected interface or abstract class argument: " + event);
      }

      final Method listenerMethod = getListenerMethod(listener, event);

      final Class<?>[] parameterTypes = listenerMethod.getParameterTypes();
      if (parameterTypes.length != 1 || !parameterTypes[0].equals(event)) {
        throw new IncompatibleListenerMethodException(listener, listenerMethod,
            event);
      }

      Log.log(String.format("Registering %s.%s() to %s", listener.getName(),
          listenerMethod.getName(), event.getName()));
      Log.logEmptyLine();

      method = listenerMethod;
      this.listener = listener;
      this.event = event;
    }

    /**
     * Try to find the method to call when a {@link Listener} should be called.
     * 
     * @param listener
     *          the Listener class to be scanned for a method.
     * @param event2
     * @return The found listener method.
     * @throws NoSuitableListenerMethodFoundException
     *           if no suitable listener method was found.
     */
    private Method getListenerMethod(final Class<? extends Listener> listener,
        final Class<? extends Event> event) {
      Method listenerMethod = getListenerMethodByAnnotation(listener, event);
      if (listenerMethod == null) {
        listenerMethod = getListenerMethodByBeingOnlySuitableMethod(listener,
            event);
      }
      if (listenerMethod == null) {
        throw new NoSuitableListenerMethodFoundException(listener, event);
      }
      return listenerMethod;
    }

    /**
     * Try to find the listener method from a {@link Listener} class by
     * annotation.
     * 
     * @param listener
     *          the {@link Listener} class to scan through.
     * @param event
     * @return the evaluated listener method, or <code>null</code> if no
     *         suitable method was found.
     * @see ListenerMethod
     */
    private static Method getListenerMethodByAnnotation(
        final Class<? extends Listener> listener,
        final Class<? extends Event> event) {

      Method listenerMethod = null;

      for (final Method candidateMethod : listener.getMethods()) {
        final ListenerMethod annotation = candidateMethod
            .getAnnotation(ListenerMethod.class);

        if (annotation != null && hasSuitableParameter(candidateMethod, event)) {
          if (listenerMethod == null) {
            listenerMethod = candidateMethod;
          } else {
            throw new DuplicateListenerMethodException(listener,
                candidateMethod, listenerMethod);
          }
        }
      }

      if (listenerMethod != null) {
        Log.log("Found listener method by annotation");
      }

      return listenerMethod;
    }

    private static boolean hasSuitableParameter(final Method candidateMethod,
        final Class<? extends Event> event) {
      final Class<?>[] params = candidateMethod.getParameterTypes();
      return params.length == 1 && event.isAssignableFrom(params[0]);
    }

    /**
     * If there is a method that takes a parameter of exactly the same type as
     * the sent event, and only one of said methods, get it.
     * 
     * @param listener
     *          The {@link Listener} to scan
     * @param event
     *          The {@link Event} class to match the parameter to.
     * @return The single method should be a listener method.
     */
    private static Method getListenerMethodByBeingOnlySuitableMethod(
        final Class<? extends Listener> listener,
        final Class<? extends Event> event) {

      Method listenerCandidate = null;

      for (final Method method : listener.getDeclaredMethods()) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1 && parameterTypes[0].equals(event)) {

          if (listenerCandidate != null) {
            // too many potential matches, so suggest nothing.
            return null;
          }

          listenerCandidate = method;
        }
      }

      if (listenerCandidate != null) {
        Log.log("Found listener method by being the only suitable method in the class");
      }

      return listenerCandidate;
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

  private final Map<Class<? extends Event>, Registration> registrationsByEvent = new HashMap<Class<? extends Event>, Blackboard.Registration>();
  private final Map<Class<? extends Listener>, HashSet<Listener>> listeners;

  /** Try to register listeners and events automatically as much as possible. */
  private boolean magicRegistration = true;

  private final Set<Class<? extends Listener>> checkedListeners = new HashSet<Class<? extends Listener>>();

  public Blackboard() {
    listeners = new ConcurrentHashMap<Class<? extends Listener>, HashSet<Listener>>();
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
   *          The {@link Listener} interface to register with <tt>event</tt>.
   * @param event
   *          The {@link Event} interface to register with <tt>listener</tt>.
   * @throws DuplicateRegistrationException
   *           if <tt>listener</tt> and/or <tt>event</tt> is already previously
   *           registered.
   * @throws DuplicateListenerMethodException
   *           if <tt>listener</tt> has more than one public method annotated
   *           with {@link ListenerMethod}.
   * @throws NoSuitableListenerMethodFoundException
   *           if <tt>listener</tt> has no public methods annotated with
   *           {@link ListenerMethod}.
   * @throws IncompatibleListenerMethodException
   *           if the method annotated with {@link ListenerMethod} doesn't have
   *           exactly one argument, it being of type <tt>event</tt>.
   * @throws IllegalArgumentException
   *           if <tt>listener</tt> is a non-interface class, and/or
   *           <tt>event</tt> is a interface or an abstract class object.
   */
  public void register(final Class<? extends Listener> listener,
      final Class<? extends Event> event) {
    magicRegistration = false;
    _register(listener, event);
  }

  private void _register(final Class<? extends Listener> listener,
      final Class<? extends Event> event) {

    assertNotNull(listener, event);

    checkForInvalidConstruction(listener, listener);
    checkForDuplicateRegistrations(listener, event);

    registrationsByEvent.put(event, new Registration(listener, event));
  }

  private void checkForInvalidConstruction(
      final Class<? extends Listener> listener,
      final Class<? extends Listener> originalListener) {
    for (final Class<? extends Listener> checkedListener : checkedListeners) {
      if (checkedListener.equals(listener)) {
        return;
      }
    }

    for (final Method method : listener.getMethods()) {
      final ListenerMethod annotation = method
          .getAnnotation(ListenerMethod.class);
      if (annotation != null) {
        final Class<?>[] params = method.getParameterTypes();
        if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
          throw new InvalidListenerMethodConstruction(listener,
              originalListener, method);
        }
      }
    }

    // check for subinterfaces
    final Class<?> c = listener.getSuperclass();
    if (c != null && Listener.class.isAssignableFrom(c)) {
      @SuppressWarnings("unchecked")
      final Class<? extends Listener> superListener = (Class<? extends Listener>) c;
      checkForInvalidConstruction(superListener, originalListener);
    }

    for (final Class<?> iface : listener.getInterfaces()) {
      if (Listener.class.isAssignableFrom(iface)) {
        @SuppressWarnings("unchecked")
        final Class<? extends Listener> cIface = (Class<? extends Listener>) iface;
        checkForInvalidConstruction(cIface, originalListener);
      }
    }

    checkedListeners.add(listener);
  }

  /**
   * @throws DuplicateRegistrationException
   *           if the registration already exists.
   */
  private void checkForDuplicateRegistrations(
      final Class<? extends Listener> listener,
      final Class<? extends Event> event) {
    for (final Registration registration : registrationsByEvent.values()) {
      final Class<? extends Event> existingEvent = registration.getEvent();

      if (existingEvent.equals(event)) {
        throw new DuplicateRegistrationException(listener, event,
            registration.getListener(), existingEvent);
      }
    }
  }

  /**
   * <p>
   * Register a {@link Listener} with Blackboard.
   * </p>
   * 
   * <p>
   * The <tt>Listener</tt> will receive all {@link Event Events} of a certain
   * type, according to any prior {@link #register(Class, Class) registrations}.
   * Each <tt>Listener</tt> object needs to be added only once, even if it
   * implements several Listener-interfaces.
   * </p>
   * 
   * <p>
   * <em>Note:</em> By design, no listener order is preserved. This means, the
   * order in which the listeners are added is not guaranteed to be the same as
   * in which the listeners are triggered.
   * </p>
   * 
   * @param listener
   *          The Listener to register.
   */
  public void addListener(final Listener listener) {
    assertNotNull(listener);

    Log.log("Adding " + listener + " for the following listeners:");

    final Class<? extends Listener> listenerClass = listener.getClass();
    Collection<Class<? extends Listener>> registeredListenerClasses = getRegisteredListenerClasses(listenerClass);

    if (registeredListenerClasses.isEmpty()) {
      boolean success = false;

      if (magicRegistration) {
        success = magicRegisterAllListenerInterfacesIn(listener);
      }

      if (!success) {
        throw new NoMatchingRegistrationFoundException(listenerClass);
      } else {
        registeredListenerClasses = getRegisteredListenerClasses(listenerClass);
      }
    }

    for (final Class<? extends Listener> registeredListenerClass : registeredListenerClasses) {
      HashSet<Listener> listenersForClass = listeners
          .get(registeredListenerClass);
      if (listenersForClass == null) {
        listenersForClass = new HashSet<Listener>();
        listeners.put(registeredListenerClass, listenersForClass);
      }

      listenersForClass.add(listener);
      Log.log("  ...listening to " + registeredListenerClass);
    }

    Log.logEmptyLine();
  }

  private boolean magicRegisterAllListenerInterfacesIn(final Listener listener) {
    final Class<? extends Listener> listenerObjectClass = listener.getClass();

    final Set<Class<? extends Listener>> interfaces = getListenerInterfacesRecursively(listenerObjectClass);
    if (interfaces.isEmpty()) {
      interfaces.add(listenerObjectClass);
    }

    boolean success = false;
    for (final Class<? extends Listener> listenerClass : interfaces) {
      final boolean resultIsSuccessful = findAndRegisterByListenerInlineClasses(listenerClass);
      if (resultIsSuccessful) {
        success = true;
      }
    }

    return success;
  }

  private Set<Class<? extends Listener>> getListenerInterfacesRecursively(
      final Class<? extends Listener> listenerObjectClass) {
    final Set<Class<? extends Listener>> interfaces = new HashSet<Class<? extends Listener>>();

    final HashSet<Class<? extends Object>> objectInterfaces = new HashSet<Class<? extends Object>>();

    final List<Class<?>> interfacesAsList = Arrays.asList(listenerObjectClass
        .getInterfaces());
    objectInterfaces.addAll(interfacesAsList);
    objectInterfaces.remove(Listener.class);

    for (final Class<? extends Object> iface : objectInterfaces) {
      if (Listener.class.isAssignableFrom(iface)) {
        @SuppressWarnings("unchecked")
        final Class<? extends Listener> listenerIface = (Class<? extends Listener>) iface;
        interfaces.addAll(getListenerInterfacesRecursively(listenerIface));
        if (interfaces.isEmpty()) {
          interfaces.add(listenerIface);
        }
      }
    }

    return interfaces;
  }

  private Collection<Class<? extends Listener>> getRegisteredListenerClasses(
      final Class<? extends Listener> listenerClass) {

    final Collection<Class<? extends Listener>> listeners = new HashSet<Class<? extends Listener>>();

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

    assertNotNull(listener);

    Log.log("Removing " + listener);

    final Class<? extends Listener> listenerClass = listener.getClass();
    final Collection<Class<? extends Listener>> registeredListenerClasses = getRegisteredListenerClasses(listenerClass);

    boolean success = false;

    for (final Class<? extends Listener> registeredListenerClass : registeredListenerClasses) {
      final Set<Listener> listenersOfClass = listeners
          .get(registeredListenerClass);
      if (listenersOfClass != null) {
        final boolean intermediateSuccess = listenersOfClass.remove(listener);

        Log.log("  ...removing it from " + registeredListenerClass);

        if (!success) {
          success = intermediateSuccess;
        }
      }
    }

    Log.logEmptyLine();

    return success;
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
   * @throws EventNotRegisteredException
   *           if <tt>event</tt>'s type wasn't previously registered with
   *           Blackboard.
   * @see #register(Class, Class)
   */
  public void fire(final Event event) {

    assertNotNull(event);

    Log.log("Firing " + event);

    final Registration registration = registrationsByEvent
        .get(event.getClass());

    if (registration == null) {
      throw new EventNotRegisteredException(event.getClass());
    }

    final Class<? extends Listener> listenerClass = registration.getListener();
    final Method listenerMethod = registration.getMethod();

    final HashSet<Listener> listenersForClass = listeners.get(listenerClass);
    if (listenersForClass == null) {
      return;
    }

    // The Set is cloned to make concurrency better, and avoid concurrent
    // modification exceptions.
    @SuppressWarnings("unchecked")
    final Set<Listener> clonedListenersForClass = (Set<Listener>) listenersForClass
        .clone();

    for (final Listener listener : clonedListenersForClass) {
      try {
        Log.log("  triggering " + listener);

        listenerMethod.invoke(listener, event);
      } catch (final IllegalArgumentException e) {
        e.printStackTrace();
      } catch (final IllegalAccessException e) {
        e.printStackTrace();
      } catch (final InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    Log.logEmptyLine();
  }

  /**
   * Assert that no arguments are <code>null</code>
   * 
   * @param args
   *          the arguments to check.
   * @throws NullPointerException
   *           if any of <tt>args</tt> is <code>null</code>.
   */
  private void assertNotNull(final Object... args) {
    for (int i = 0; i < args.length; i++) {
      if (args[i] == null) {
        throw new NullPointerException("Argument with index " + i
            + " was null.");
      }
    }
  }

  public void enableLogging() {
    Log.setLogging(true);
  }

  public void disableLogging() {
    Log.setLogging(false);
  }

  public void discover() {
    final Exception exception = new Exception();
    exception.fillInStackTrace();
    final StackTraceElement[] stackTrace = exception.getStackTrace();

    if (stackTrace.length < 2) {
      throw new RuntimeException("Can't autodiscover, seems like "
          + "this method was never called from anywhere");
    }

    final StackTraceElement caller = stackTrace[1];
    try {
      discoverFrom(getClass().getClassLoader().loadClass(caller.getClassName()));
    } catch (final ClassNotFoundException e) {
      throw new RuntimeException("Caller class \"" + caller.getClassName()
          + "\" could not be loaded with the current "
          + "ClassLoader. Use discoverFrom() for better luck.");
    }
  }

  public void discoverFrom(final Class<?> referenceClass) {
    Log.log("Starting automatic discovery from " + referenceClass.getName());

    final Class<? extends Object>[] classes = ClassDiscovery.DiscoverClasses(
        referenceClass, null, null);

    for (final Class<? extends Object> clazz : classes) {
      try {
        if (findByAnnotation(clazz)) {
          continue;
          // } else if (findByListenerInlineClasses(clazz)) {
          // continue;
        } else if (findByEventInlineClasses(clazz)) {
          continue;
        }
      } catch (final DuplicateRegistrationException e) {
        // Ignore, we're doing magic!
      }
    }
  }

  private boolean findByAnnotation(final Class<? extends Object> eventCandidate) {

    if (Event.class.isAssignableFrom(eventCandidate)) {
      final ListenerPair listenerPair = eventCandidate
          .getAnnotation(ListenerPair.class);
      if (listenerPair != null) {
        @SuppressWarnings("unchecked")
        final Class<? extends Event> eventClass = (Class<? extends Event>) eventCandidate;
        _register(listenerPair.value(), eventClass);
        return true;
      }
    }

    return false;
  }

  private boolean findAndRegisterByListenerInlineClasses(
      final Class<? extends Object> listenerCandidate) {
    if (Listener.class.isAssignableFrom(listenerCandidate)) {
      final Class<?> declaringClass = listenerCandidate.getDeclaringClass();
      if (declaringClass != null
          && Event.class.isAssignableFrom(declaringClass)) {
        @SuppressWarnings("unchecked")
        final Class<? extends Event> eventClass = (Class<? extends Event>) declaringClass;
        @SuppressWarnings("unchecked")
        final Class<? extends Listener> listenerClass = (Class<? extends Listener>) listenerCandidate;

        _register(listenerClass, eventClass);
        return true;
      }
    }

    return false;
  }

  private boolean findByEventInlineClasses(
      final Class<? extends Object> eventCandidate) {
    if (Event.class.isAssignableFrom(eventCandidate)) {

      for (final Class<? extends Object> innerClass : eventCandidate
          .getDeclaredClasses()) {
        if (innerClass != null && Listener.class.isAssignableFrom(innerClass)) {
          @SuppressWarnings("unchecked")
          final Class<? extends Event> eventClass = (Class<? extends Event>) eventCandidate;
          @SuppressWarnings("unchecked")
          final Class<? extends Listener> listenerClass = (Class<? extends Listener>) innerClass;

          _register(listenerClass, eventClass);
          return true;
        }
      }
    }

    return false;
  }

  public void clear() {
    Log.log("Clearing Blackboard");
    listeners.clear();
    registrationsByEvent.clear();
  }
}
