package com.hbm.ntm.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Legacy-name hash-keyed set.
 */
@Deprecated(forRemoval = false)
public class HashedSet<T> extends HbmHashedSet<T> {
    public HashedSet() {
    }

    public HashedSet(Collection<? extends T> values) {
        super(values);
    }

    public static class HashedIterator<T> implements Iterator<T> {
        private final Iterator<Map.Entry<Integer, T>> iterator;

        public HashedIterator(HashedSet<T> set) {
            this.iterator = set.getMap().entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return iterator.next().getValue();
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
