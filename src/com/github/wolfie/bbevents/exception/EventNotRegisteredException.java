package com.github.wolfie.bbevents.exception;

import com.github.wolfie.bbevents.Event;

public class EventNotRegisteredException extends RuntimeException {
  private static final long serialVersionUID = 3715484742311180580L;

  public EventNotRegisteredException(final Class<? extends Event> event) {
    super(event.getName() + " was not registered.");
  }

}
