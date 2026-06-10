package com.hbm.ntm.util;

/**
 * Legacy-name weighted entry wrapper.
 */
@Deprecated(forRemoval = false)
public class WeightedRandomGeneric<T> extends HbmWeightedRandomGeneric<T> {
    public WeightedRandomGeneric(T item, int weight) {
        super(item, weight);
    }
}
