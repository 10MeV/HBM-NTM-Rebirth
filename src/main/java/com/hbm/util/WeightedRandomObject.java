package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for weighted object entries.
 */
@Deprecated(forRemoval = false)
public class WeightedRandomObject extends com.hbm.ntm.util.WeightedRandomObject {
    public WeightedRandomObject(Object item, int weight) {
        super(item, weight);
    }
}
