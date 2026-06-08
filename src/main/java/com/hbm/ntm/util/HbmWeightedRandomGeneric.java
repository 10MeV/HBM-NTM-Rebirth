package com.hbm.ntm.util;

public class HbmWeightedRandomGeneric<T> implements HbmWeightedRandomUtil.WeightedEntry {
    public final T item;
    public final int itemWeight;

    public HbmWeightedRandomGeneric(T item, int weight) {
        this.item = item;
        this.itemWeight = weight;
    }

    public T get() {
        return item;
    }

    @Override
    public int itemWeight() {
        return itemWeight;
    }
}
