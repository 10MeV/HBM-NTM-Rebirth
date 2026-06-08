package com.hbm.ntm.util;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;

public final class HbmConcurrentBitSet {
    private final AtomicLongArray words;
    private final int size;
    private final LongAdder bitCount = new LongAdder();

    public HbmConcurrentBitSet(int size) {
        this.size = size;
        this.words = new AtomicLongArray((size + 63) >>> 6);
    }

    public void set(int bit) {
        if (bit < 0 || bit >= size) {
            return;
        }
        int wordIndex = bit >>> 6;
        long mask = 1L << (bit & 63);
        while (true) {
            long oldWord = words.get(wordIndex);
            long newWord = oldWord | mask;
            if (oldWord == newWord) {
                return;
            }
            if (words.compareAndSet(wordIndex, oldWord, newWord)) {
                bitCount.increment();
                return;
            }
        }
    }

    public void clear(int bit) {
        if (bit < 0 || bit >= size) {
            return;
        }
        int wordIndex = bit >>> 6;
        long mask = ~(1L << (bit & 63));
        while (true) {
            long oldWord = words.get(wordIndex);
            long newWord = oldWord & mask;
            if (oldWord == newWord) {
                return;
            }
            if (words.compareAndSet(wordIndex, oldWord, newWord)) {
                bitCount.decrement();
                return;
            }
        }
    }

    public int nextSetBit(int from) {
        if (from < 0) {
            from = 0;
        }
        int wordIndex = from >>> 6;
        if (wordIndex >= words.length()) {
            return -1;
        }
        long word = words.get(wordIndex) & (~0L << (from & 63));
        while (true) {
            if (word != 0L) {
                int index = (wordIndex << 6) + Long.numberOfTrailingZeros(word);
                return index < size ? index : -1;
            }
            wordIndex++;
            if (wordIndex >= words.length()) {
                return -1;
            }
            word = words.get(wordIndex);
        }
    }

    public boolean isEmpty() {
        return bitCount.sum() == 0L;
    }

    public long cardinality() {
        return bitCount.sum();
    }
}
