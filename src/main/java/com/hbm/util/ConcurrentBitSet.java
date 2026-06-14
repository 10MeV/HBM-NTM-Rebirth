package com.hbm.util;

/**
 * Legacy 1.7.10 package bridge for the concurrent bit set helper.
 */
@Deprecated(forRemoval = false)
public class ConcurrentBitSet extends com.hbm.ntm.util.ConcurrentBitSet {
    public ConcurrentBitSet(int size) {
        super(size);
    }
}
