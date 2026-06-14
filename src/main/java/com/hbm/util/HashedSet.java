package com.hbm.util;

import java.util.Collection;

/**
 * Legacy 1.7.10 package bridge for hash-keyed sets.
 */
@Deprecated(forRemoval = false)
public class HashedSet<T> extends com.hbm.ntm.util.HashedSet<T> {
    public HashedSet() {
        super();
    }

    public HashedSet(Collection<? extends T> values) {
        super(values);
    }
}
