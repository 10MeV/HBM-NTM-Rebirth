package com.hbm.ntm.util;

import net.minecraft.world.inventory.DataSlot;

public final class HbmMenuDataSlots {
    private HbmMenuDataSlots() {
    }

    public static void addLong(DataSlotSink sink, LongGetter serverGetter, LongGetter clientGetter,
            LongSetter setter) {
        sink.add(new DataSlot() {
            @Override
            public int get() {
                return (int) (serverGetter.get() & 0xFFFFL);
            }

            @Override
            public void set(int value) {
                setter.set((clientGetter.get() & ~0xFFFFL) | (value & 0xFFFFL));
            }
        });
        sink.add(new DataSlot() {
            @Override
            public int get() {
                return (int) ((serverGetter.get() >>> 16) & 0xFFFFL);
            }

            @Override
            public void set(int value) {
                setter.set((clientGetter.get() & ~(0xFFFFL << 16)) | ((long) (value & 0xFFFF) << 16));
            }
        });
        sink.add(new DataSlot() {
            @Override
            public int get() {
                return (int) ((serverGetter.get() >>> 32) & 0xFFFFL);
            }

            @Override
            public void set(int value) {
                setter.set((clientGetter.get() & ~(0xFFFFL << 32)) | ((long) (value & 0xFFFF) << 32));
            }
        });
        sink.add(new DataSlot() {
            @Override
            public int get() {
                return (int) ((serverGetter.get() >>> 48) & 0xFFFFL);
            }

            @Override
            public void set(int value) {
                setter.set((clientGetter.get() & ~(0xFFFFL << 48)) | ((long) (value & 0xFFFF) << 48));
            }
        });
    }

    public static void addProgress(DataSlotSink sink, DoubleGetter serverGetter, IntSetter setter) {
        sink.add(new DataSlot() {
            @Override
            public int get() {
                return (int) Math.round(serverGetter.get() * 10_000.0D);
            }

            @Override
            public void set(int value) {
                setter.set(value);
            }
        });
    }

    @FunctionalInterface
    public interface LongGetter {
        long get();
    }

    @FunctionalInterface
    public interface LongSetter {
        void set(long value);
    }

    @FunctionalInterface
    public interface DoubleGetter {
        double get();
    }

    @FunctionalInterface
    public interface IntSetter {
        void set(int value);
    }

    @FunctionalInterface
    public interface DataSlotSink {
        void add(DataSlot slot);
    }
}
