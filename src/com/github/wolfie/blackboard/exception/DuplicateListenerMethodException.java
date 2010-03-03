package com.github.wolfie.blackboard.exception;

import java.lang.reflect.Method;

import com.github.wolfie.blackboard.Listener;

public class DuplicateListenerMethodException extends RuntimeException {
  private static final long serialVersionUID = 2699631613035288301L;

  public DuplicateListenerMethodException(
      final Class<? extends Listener> listener, final Method duplicateMethod,
      final Method listenerMethod) {
    super("Class " + listener.getName()
        + " has multiple listener method declarations. "
        + listenerMethod.getName()
        + " was already accepted as the method, but "
        + duplicateMethod.getName() + " was found also.");
  }

}
