package com.hbm.ntm.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

public class HbmFluidTank {
    public static final int HIGHEST_VALID_PRESSURE = 5;

    private FluidType type;
    private int fill;
    private int maxFill;
    private int pressure;

    public HbmFluidTank(FluidType type, int maxFill) {
        this.type = type == null ? HbmFluids.NONE : type;
        this.maxFill = Math.max(0, maxFill);
    }

    public HbmFluidTank withPressure(int pressure) {
        int clamped = clampPressure(pressure);
        if (this.pressure != clamped) {
            setFill(0);
        }
        this.pressure = clamped;
        return this;
    }

    public HbmFluidTank conform(HbmFluidStack stack) {
        setTankType(stack.type());
        withPressure(stack.pressure());
        return this;
    }

    public void resetTank() {
        type = HbmFluids.NONE;
        fill = 0;
        pressure = 0;
    }

    public boolean canAccept(FluidType type, int pressure) {
        FluidType incoming = type == null ? HbmFluids.NONE : type;
        return incoming != HbmFluids.NONE
                && this.pressure == clampPressure(pressure)
                && (this.type == HbmFluids.NONE || this.type == incoming);
    }

    public int fill(FluidType type, int amount, int pressure, boolean simulate) {
        if (amount <= 0 || !canAccept(type, pressure)) {
            return 0;
        }
        int accepted = Math.min(amount, maxFill - fill);
        if (!simulate && accepted > 0) {
            if (this.type == HbmFluids.NONE) {
                this.type = type;
            }
            this.fill += accepted;
        }
        return accepted;
    }

    public int drain(int amount, boolean simulate) {
        if (amount <= 0 || fill <= 0) {
            return 0;
        }
        int drained = Math.min(amount, fill);
        if (!simulate) {
            fill -= drained;
        }
        return drained;
    }

    public HbmFluidReleaseEffects.ReleaseReport release(Level level, BlockPos pos, int amount, FluidReleaseType releaseType, boolean simulate) {
        if (amount <= 0 || isEmpty()) {
            return HbmFluidReleaseEffects.previewRelease(type, 0, releaseType);
        }
        int released = Math.min(amount, fill);
        HbmFluidReleaseEffects.ReleaseReport report = simulate
                ? HbmFluidReleaseEffects.previewRelease(type, released, releaseType)
                : HbmFluidReleaseEffects.applyRelease(level, pos, this, released, releaseType);
        if (!simulate) {
            fill -= released;
        }
        return report;
    }

    public HbmFluidReleaseEffects.ReleaseReport releaseAll(Level level, BlockPos pos, FluidReleaseType releaseType, boolean simulate) {
        return release(level, pos, fill, releaseType, simulate);
    }

    public boolean isEmpty() {
        return type == HbmFluids.NONE || fill <= 0;
    }

    public int getSpace() {
        return Math.max(0, maxFill - fill);
    }

    public int getSpaceFor(FluidType type) {
        FluidType incoming = type == null ? HbmFluids.NONE : type;
        if (incoming == HbmFluids.NONE || (this.type != HbmFluids.NONE && this.type != incoming)) {
            return 0;
        }
        return getSpace();
    }

    public HbmFluidStack getFluidStack() {
        return new HbmFluidStack(type, fill, pressure);
    }

    public void setTankType(FluidType type) {
        FluidType newType = type == null ? HbmFluids.NONE : type;
        if (this.type != newType) {
            this.type = newType;
            setFill(0);
        }
    }

    public void setFill(int fill) {
        this.fill = Mth.clamp(fill, 0, maxFill);
    }

    public int changeTankSize(int maxFill) {
        this.maxFill = Math.max(0, maxFill);
        if (fill > this.maxFill) {
            int overflow = fill - this.maxFill;
            fill = this.maxFill;
            return overflow;
        }
        return 0;
    }

    public void writeToNbt(CompoundTag tag, String key) {
        tag.putInt(key, fill);
        tag.putInt(key + "_max", maxFill);
        tag.putString(key + "_type", type.getName());
        tag.putInt(key + "_type_id", type.getId());
        tag.putShort(key + "_p", (short) pressure);
    }

    public void readFromNbt(CompoundTag tag, String key) {
        fill = tag.getInt(key);
        int savedMax = tag.getInt(key + "_max");
        if (savedMax > 0) {
            maxFill = savedMax;
        }
        fill = Mth.clamp(fill, 0, maxFill);
        type = HbmFluids.fromName(tag.getString(key + "_type"));
        if (type == HbmFluids.NONE && tag.contains(key + "_type_id")) {
            type = HbmFluids.fromId(tag.getInt(key + "_type_id"));
        }
        if (type == HbmFluids.NONE && tag.contains(key + "_type")) {
            type = HbmFluids.fromId(tag.getInt(key + "_type"));
        }
        pressure = clampPressure(tag.getShort(key + "_p"));
    }

    public FluidType getTankType() {
        return type;
    }

    public int getFill() {
        return fill;
    }

    public int getMaxFill() {
        return maxFill;
    }

    public int getPressure() {
        return pressure;
    }

    public static int clampPressure(int pressure) {
        return Mth.clamp(pressure, 0, HIGHEST_VALID_PRESSURE);
    }
}
