package com.hbm.ntm.neutron;

import net.minecraft.nbt.CompoundTag;

public class RBMKControlState {
    public static final String TAG_LEVEL = "level";
    public static final String TAG_TARGET_LEVEL = "targetLevel";
    public static final double SPEED = 0.00277D;
    public static final long POWER_CONSUMPTION = 5_000L;
    public static final long MAX_POWER = POWER_CONSUMPTION * 10L;

    private double lastLevel;
    private double level;
    private double targetLevel;
    private boolean hasPower;
    private long power;

    public double lastLevel() {
        return lastLevel;
    }

    public double level() {
        return level;
    }

    public void setLevel(double level) {
        this.level = clamp01(level);
    }

    public double targetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(double targetLevel) {
        this.targetLevel = clamp01(targetLevel);
    }

    public boolean hasPower() {
        return hasPower;
    }

    public long power() {
        return power;
    }

    public void setPower(long power) {
        this.power = Math.max(0L, Math.min(MAX_POWER, power));
    }

    public void tick(boolean poweredControlRod, double controlSpeedModifier) {
        hasPower = true;
        if (poweredControlRod && power < POWER_CONSUMPTION) {
            hasPower = false;
        }

        lastLevel = level;
        if (hasPower) {
            double step = SPEED * Math.max(0.0D, controlSpeedModifier);
            if (level < targetLevel) {
                level = Math.min(targetLevel, level + step);
            }
            if (level > targetLevel) {
                level = Math.max(targetLevel, level - step);
            }
            if (poweredControlRod && level != lastLevel) {
                power = Math.max(0L, power - POWER_CONSUMPTION);
            }
        }
    }

    public void tick(boolean poweredControlRod, RBMKRuntimeSettings settings) {
        tick(poweredControlRod, settings.controlSpeedModifier());
    }

    public void load(CompoundTag tag) {
        setLevel(tag.getDouble(TAG_LEVEL));
        setTargetLevel(tag.getDouble(TAG_TARGET_LEVEL));
    }

    public void save(CompoundTag tag) {
        tag.putDouble(TAG_LEVEL, level);
        tag.putDouble(TAG_TARGET_LEVEL, targetLevel);
    }

    private static double clamp01(double value) {
        if (value < 0.0D || Double.isNaN(value)) {
            return 0.0D;
        }
        if (value > 1.0D) {
            return 1.0D;
        }
        return value;
    }
}
