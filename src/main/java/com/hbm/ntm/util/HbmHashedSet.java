package com.hbm.ntm.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HbmHashedSet<T> extends AbstractSet<T> {
    private final HashMap<Integer, T> map = new HashMap<>();

    public HbmHashedSet() {
    }

    public HbmHashedSet(Collection<? extends T> values) {
        addAll(values);
    }

    public HashMap<Integer, T> getMap() {
        return map;
    }

    @Override
    public boolean add(T value) {
        boolean contained = contains(value);
        map.put(value.hashCode(), value);
        return !contained;
    }

    @Override
    public boolean contains(Object value) {
        return value != null && map.containsKey(value.hashCode());
    }

    @Override
    public boolean remove(Object value) {
        if (value == null) {
            return false;
        }
        return map.remove(value.hashCode()) != null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Iterator<T> iterator() {
        Iterator<Map.Entry<Integer, T>> iterator = map.entrySet().iterator();
        return new Iterator<>() {
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
        };
    }

    @Override
    public int size() {
        return map.size();
    }
}
