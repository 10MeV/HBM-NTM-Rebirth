package com.hbm.interfaces;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Legacy 1.7.10 package bridge for the spaghetti-code marker.
 */
@Deprecated(forRemoval = false)
@Retention(RetentionPolicy.CLASS)
public @interface Spaghetti {
    String value();
}
