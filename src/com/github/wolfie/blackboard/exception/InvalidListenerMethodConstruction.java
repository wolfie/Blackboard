package com.github.wolfie.blackboard.exception;

import java.lang.reflect.Method;

import com.github.wolfie.blackboard.Listener;

public class InvalidListenerMethodConstruction extends RuntimeException {

  public InvalidListenerMethodConstruction(
      final Class<? extends Listener> listener,
      final Class<? extends Listener> originalListener, final Method method) {
    super(getStringFor(listener, originalListener, method));
  }

  private static String getStringFor(final Class<? extends Listener> listener,
      final Class<? extends Listener> originalListener, final Method method) {

    final String addition;
    if (originalListener != null && listener != originalListener) {
      addition = " (a superclass/superinterface to %s) ";
    } else {
      addition = " ";
    }

    return String.format("Method %s in %s%sis incorrectly constructed. "
        + "Only one parameter, implementing %s", method.getName(),
        listener.toString(), addition, Listener.class.toString());
  }

  /**
   * 
   */
  private static final long serialVersionUID = -591920370187705054L;

}
