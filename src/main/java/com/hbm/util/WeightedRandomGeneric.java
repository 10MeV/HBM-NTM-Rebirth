package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for weighted entries.
 */
@Deprecated(forRemoval = false)
public class WeightedRandomGeneric<T> extends com.hbm.ntm.util.WeightedRandomGeneric<T> {
    public WeightedRandomGeneric(T item, int weight) {
        super(item, weight);
    }
}
