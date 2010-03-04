package com.github.wolfie.blackboard.exception;

import com.github.wolfie.blackboard.Listener;

public class NoMatchingListenerRegistered extends RuntimeException {
  private static final long serialVersionUID = -8143162070880780841L;

  public NoMatchingListenerRegistered(final Class<? extends Listener> listener) {
    super(String
        .format("Cannot register %s since "
            + "no matching Listener was previously registered.", listener
            .getName()));
  }
}
