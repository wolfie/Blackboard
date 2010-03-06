package com.github.wolfie.blackboard.exception;

import com.github.wolfie.blackboard.Listener;

public class NoMatchingRegistrationFoundException extends RuntimeException {
  private static final long serialVersionUID = 1639067602385701335L;
  
  public NoMatchingRegistrationFoundException(
      final Class<? extends Listener> class1) {
    super(class1.getName() + " or any of its superclasses or "
        + "interfaces were not previously registered as Listener.");
  }
}
