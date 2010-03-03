package com.github.wolfie.bbevents.exception;

import java.lang.reflect.Method;

import com.github.wolfie.bbevents.Event;
import com.github.wolfie.bbevents.Listener;

public class IncompatibleListenerMethodException extends RuntimeException {
  private static final long serialVersionUID = 4404069292505802332L;

  public IncompatibleListenerMethodException(final Class<? extends Listener> listener,
      final Method listenerMethod, final Class<? extends Event> event) {
    super("Listener method " + listenerMethod.getName() + " in class "
        + listener.getName() + " should have exactly one parameter of type "
        + event.getName());
  }
}
