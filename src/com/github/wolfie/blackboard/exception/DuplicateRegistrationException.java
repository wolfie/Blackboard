package com.github.wolfie.blackboard.exception;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

public class DuplicateRegistrationException extends RuntimeException {
  private static final long serialVersionUID = 1710988524955675742L;
  
  public DuplicateRegistrationException(
      final Class<? extends Listener> listener,
      final Class<? extends Event> event,
      final Class<? extends Listener> existingListener,
      final Class<? extends Event> existingEvent) {
    super(
        String
            .format(
                "Duplicate listener/event registration. Tried to register %s and %s, collides with %s and/or %s",
                listener, event, existingListener, existingEvent));
  }
  
}
