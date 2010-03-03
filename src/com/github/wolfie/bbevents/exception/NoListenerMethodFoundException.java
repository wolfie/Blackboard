package com.github.wolfie.bbevents.exception;

import com.github.wolfie.bbevents.Listener;
import com.github.wolfie.bbevents.ListenerMethod;

public class NoListenerMethodFoundException extends RuntimeException {
  private static final long serialVersionUID = 2145403658103285993L;

  public NoListenerMethodFoundException(final Class<? extends Listener> listener) {
    super("No annotated methods found in " + listener
        + ". Make sure you have exactly one method annotated with "
        + ListenerMethod.class.getName());
  }
}
