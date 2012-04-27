package com.github.wolfie.blackboard.exception;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class NoSuitableListenerMethodFoundException extends RuntimeException {
  private static final long serialVersionUID = 2145403658103285993L;

  public NoSuitableListenerMethodFoundException(
      final Class<? extends Listener> listener,
      final Class<? extends Event> event) {
    super("No suitable listener method for " + event + " was found in "
        + listener);
  }
}
