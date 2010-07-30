package com.github.wolfie.blackboard.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.wolfie.blackboard.Event;
import com.github.wolfie.blackboard.Listener;

/**
 * <p>
 * The annotation that marks that this is the method to call in a
 * {@link Listener}, when a matching {@link Event} has been fired.
 * </p>
 * 
 * <p>
 * One, and only one public method must be annotated per {@link Listener}.
 * Non-public methods are ignored.
 * </p>
 * 
 * @author Henrik Paul
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ListenerMethod {
}
