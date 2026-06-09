package com.hbm.ntm.neutron;

import net.minecraft.nbt.CompoundTag;

public class RBMKCoolerState {
    public static final String TAG_COLD_FILL = "coldFill";
    public static final String TAG_COLD_MAX = "coldMax";
    public static final String TAG_HOT_FILL = "hotFill";
    public static final String TAG_HOT_MAX = "hotMax";
    public static final String TAG_TIMER = "timer";
    public static final int DEFAULT_TANK_CAPACITY = 4_000;

    private int coldFill;
    private int coldMax = DEFAULT_TANK_CAPACITY;
    private int hotFill;
    private int hotMax = DEFAULT_TANK_CAPACITY;
    private int timer;

    public int coldFill() {
        return coldFill;
    }

    public void setColdFill(int coldFill) {
        this.coldFill = clamp(coldFill, 0, coldMax);
    }

    public int coldMax() {
        return coldMax;
    }

    public void setColdMax(int coldMax) {
        this.coldMax = Math.max(1, coldMax);
        setColdFill(coldFill);
    }

    public int hotFill() {
        return hotFill;
    }

    public void setHotFill(int hotFill) {
        this.hotFill = clamp(hotFill, 0, hotMax);
    }

    public int hotMax() {
        return hotMax;
    }

    public void setHotMax(int hotMax) {
        this.hotMax = Math.max(1, hotMax);
        setHotFill(hotFill);
    }

    public int timer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = Math.max(0, timer);
    }

    public void load(CompoundTag tag) {
        setColdMax(tag.contains(TAG_COLD_MAX) ? tag.getInt(TAG_COLD_MAX) : DEFAULT_TANK_CAPACITY);
        setHotMax(tag.contains(TAG_HOT_MAX) ? tag.getInt(TAG_HOT_MAX) : DEFAULT_TANK_CAPACITY);
        setColdFill(tag.getInt(TAG_COLD_FILL));
        setHotFill(tag.getInt(TAG_HOT_FILL));
        setTimer(tag.getInt(TAG_TIMER));
    }

    public void save(CompoundTag tag) {
        tag.putInt(TAG_COLD_FILL, coldFill);
        tag.putInt(TAG_COLD_MAX, coldMax);
        tag.putInt(TAG_HOT_FILL, hotFill);
        tag.putInt(TAG_HOT_MAX, hotMax);
        tag.putInt(TAG_TIMER, timer);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
