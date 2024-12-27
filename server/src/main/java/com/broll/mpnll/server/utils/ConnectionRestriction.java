package com.broll.mpnll.server.utils;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(value = RUNTIME)
@Target(value = METHOD)
public @interface ConnectionRestriction {

    RestrictionType value() default RestrictionType.IN_LOBBY;
}
