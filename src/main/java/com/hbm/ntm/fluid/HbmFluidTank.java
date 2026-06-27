package com.hbm.ntm.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
        withPressureReport(pressure);
        return this;
    }

    public TankMutationReport withPressureReport(int pressure) {
        TankState before = snapshot();
        int clamped = clampPressure(pressure);
        if (this.pressure != clamped) {
            setFill(0);
        }
        this.pressure = clamped;
        return TankMutationReport.of("pressure", false, before, snapshot(), pressure, clamped,
                before.pressure() != clamped ? before.fillMb() : 0, 0);
    }

    public HbmFluidTank conform(HbmFluidStack stack) {
        setTankType(stack.type());
        withPressure(stack.pressure());
        return this;
    }

    public void resetTank() {
        resetTankReport();
    }

    public TankMutationReport resetTankReport() {
        TankState before = snapshot();
        type = HbmFluids.NONE;
        fill = 0;
        pressure = 0;
        return TankMutationReport.of("reset", false, before, snapshot(), 0, 0, before.fillMb(), 0);
    }

    public boolean canAccept(FluidType type, int pressure) {
        FluidType incoming = type == null ? HbmFluids.NONE : type;
        return incoming != HbmFluids.NONE
                && this.pressure == clampPressure(pressure)
                && (this.type == HbmFluids.NONE || this.type == incoming);
    }

    public int fill(FluidType type, int amount, int pressure, boolean simulate) {
        return fillReport(type, amount, pressure, simulate).movedMb();
    }

    public TankMutationReport fillReport(FluidType type, int amount, int pressure, boolean simulate) {
        TankState before = snapshot();
        if (amount <= 0 || !canAccept(type, pressure)) {
            return TankMutationReport.of("fill", simulate, before, before, amount, 0, 0, Math.max(0, amount));
        }
        int accepted = Math.min(amount, maxFill - fill);
        if (!simulate && accepted > 0) {
            if (this.type == HbmFluids.NONE) {
                this.type = type;
            }
            this.fill += accepted;
        }
        return TankMutationReport.of("fill", simulate, before, snapshot(), amount, accepted, 0, amount - accepted);
    }

    public int drain(int amount, boolean simulate) {
        return drainReport(amount, simulate).movedMb();
    }

    public TankMutationReport drainReport(int amount, boolean simulate) {
        TankState before = snapshot();
        if (amount <= 0 || fill <= 0) {
            return TankMutationReport.of("drain", simulate, before, before, amount, 0, 0, Math.max(0, amount));
        }
        int drained = Math.min(amount, fill);
        if (!simulate) {
            fill -= drained;
        }
        return TankMutationReport.of("drain", simulate, before, snapshot(), amount, drained, 0, amount - drained);
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
        setTankTypeReport(type);
    }

    public TankMutationReport setTankTypeReport(FluidType type) {
        TankState before = snapshot();
        FluidType newType = type == null ? HbmFluids.NONE : type;
        if (this.type != newType) {
            this.type = newType;
            setFill(0);
        }
        return TankMutationReport.of("type", false, before, snapshot(), 0, 0,
                before.type() != newType ? before.fillMb() : 0, 0);
    }

    public void setFill(int fill) {
        setFillReport(fill);
    }

    public TankMutationReport setFillReport(int fill) {
        TankState before = snapshot();
        this.fill = Mth.clamp(fill, 0, maxFill);
        int rejected = fill > maxFill ? fill - maxFill : 0;
        return TankMutationReport.of("set_fill", false, before, snapshot(), fill,
                Math.abs(this.fill - before.fillMb()), 0, Math.max(0, rejected));
    }

    public int changeTankSize(int maxFill) {
        return changeTankSizeReport(maxFill).overflowMb();
    }

    public TankMutationReport changeTankSizeReport(int maxFill) {
        TankState before = snapshot();
        this.maxFill = Math.max(0, maxFill);
        int overflow = 0;
        if (fill > this.maxFill) {
            overflow = fill - this.maxFill;
            fill = this.maxFill;
        }
        return TankMutationReport.of("capacity", false, before, snapshot(), maxFill, 0, 0, overflow);
    }

    public void writeToNbt(CompoundTag tag, String key) {
        writeToNbtReport(tag, key);
    }

    public TankNbtWriteReport writeToNbtReport(CompoundTag tag, String key) {
        tag.putInt(key, fill);
        tag.putInt(key + "_max", maxFill);
        tag.putInt(key + "_type", type.getId());
        tag.putShort(key + "_p", (short) pressure);
        return new TankNbtWriteReport(key, snapshot());
    }

    public void readFromNbt(CompoundTag tag, String key) {
        readFromNbtReport(tag, key);
    }

    public TankNbtReadReport readFromNbtReport(CompoundTag tag, String key) {
        TankState before = snapshot();
        int savedFill = tag.getInt(key);
        fill = tag.getInt(key);
        int savedMax = tag.getInt(key + "_max");
        if (savedMax > 0) {
            maxFill = savedMax;
        }
        fill = Mth.clamp(fill, 0, maxFill);
        if (tag.contains(key + "_type", Tag.TAG_STRING)) {
            type = HbmFluids.fromNameCompat(tag.getString(key + "_type"));
        } else {
            type = HbmFluids.NONE;
        }
        if (type == HbmFluids.NONE && tag.contains(key + "_type", Tag.TAG_INT)) {
            type = HbmFluids.fromId(tag.getInt(key + "_type"));
        }
        if (type == HbmFluids.NONE && tag.contains(key + "_type_id", Tag.TAG_INT)) {
            type = HbmFluids.fromId(tag.getInt(key + "_type_id"));
        }
        int savedPressure = tag.getShort(key + "_p");
        pressure = clampPressure(savedPressure);
        return new TankNbtReadReport(key, before, snapshot(), savedFill, savedMax, savedPressure,
                fill != savedFill, pressure != savedPressure);
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

    public TankState snapshot() {
        return new TankState(type, fill, maxFill, pressure);
    }

    public record TankState(FluidType type, int fillMb, int capacityMb, int pressure) {
        public TankState {
            type = type == null ? HbmFluids.NONE : type;
            fillMb = Math.max(0, fillMb);
            capacityMb = Math.max(0, capacityMb);
            pressure = HbmFluidTank.clampPressure(pressure);
        }

        public int spaceMb() {
            return Math.max(0, capacityMb - fillMb);
        }

        public boolean isEmpty() {
            return type == HbmFluids.NONE || fillMb <= 0;
        }
    }

    public record TankMutationReport(
            String operation,
            boolean simulated,
            TankState before,
            TankState after,
            int requestedMb,
            int movedMb,
            int clearedMb,
            int rejectedMb,
            boolean changed) {
        public TankMutationReport {
            operation = operation == null ? "unknown" : operation;
            before = before == null ? new TankState(HbmFluids.NONE, 0, 0, 0) : before;
            after = after == null ? before : after;
            requestedMb = Math.max(0, requestedMb);
            movedMb = Math.max(0, movedMb);
            clearedMb = Math.max(0, clearedMb);
            rejectedMb = Math.max(0, rejectedMb);
        }

        private static TankMutationReport of(String operation, boolean simulated, TankState before, TankState after,
                int requestedMb, int movedMb, int clearedMb, int rejectedMb) {
            boolean changed = before != null && after != null
                    && (before.type() != after.type()
                            || before.fillMb() != after.fillMb()
                            || before.capacityMb() != after.capacityMb()
                            || before.pressure() != after.pressure());
            return new TankMutationReport(operation, simulated, before, after, requestedMb, movedMb, clearedMb,
                    rejectedMb, changed);
        }

        public int overflowMb() {
            return rejectedMb;
        }
    }

    public record TankNbtWriteReport(String key, TankState writtenState) {
        public TankNbtWriteReport {
            key = key == null ? "" : key;
            writtenState = writtenState == null ? new TankState(HbmFluids.NONE, 0, 0, 0) : writtenState;
        }
    }

    public record TankNbtReadReport(
            String key,
            TankState before,
            TankState after,
            int savedFillMb,
            int savedCapacityMb,
            int savedPressure,
            boolean clampedFill,
            boolean clampedPressure) {
        public TankNbtReadReport {
            key = key == null ? "" : key;
            before = before == null ? new TankState(HbmFluids.NONE, 0, 0, 0) : before;
            after = after == null ? before : after;
            savedFillMb = Math.max(0, savedFillMb);
            savedCapacityMb = Math.max(0, savedCapacityMb);
        }

        public boolean changed() {
            return before.type() != after.type()
                    || before.fillMb() != after.fillMb()
                    || before.capacityMb() != after.capacityMb()
                    || before.pressure() != after.pressure();
        }
    }
}
