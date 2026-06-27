package com.hbm.inventory.fluid.tank;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModItems;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * Legacy package facade for 1.7.10 fluid tanks.
 */
@Deprecated(forRemoval = false)
public class FluidTank extends HbmFluidTank implements Cloneable {
    public static final FluidTank[] EMPTY_ARRAY = new FluidTank[0];
    public static final List<FluidLoadingHandler> loadingHandlers = new ArrayList<>();
    public static final Set<Item> noDualUnload = new HashSet<>();

    static {
        loadingHandlers.add(new FluidLoaderStandard());
        loadingHandlers.add(new FluidLoaderFillableItem());
        loadingHandlers.add(new FluidLoaderInfinite());
    }

    protected FluidType type;
    protected int fluid;
    protected int maxFluid;
    protected int pressure;

    public FluidTank(FluidType type, int maxFill) {
        super(type, maxFill);
        syncLegacyFields();
    }

    @Override
    public FluidTank withPressure(int pressure) {
        syncModernFromLegacyFields();
        super.withPressure(pressure);
        syncLegacyFields();
        return this;
    }

    @Override
    public FluidTank conform(com.hbm.ntm.fluid.HbmFluidStack stack) {
        syncModernFromLegacyFields();
        super.conform(stack);
        syncLegacyFields();
        return this;
    }

    public FluidTank conform(com.hbm.inventory.FluidStack stack) {
        if (stack == null) {
            resetTank();
            return this;
        }
        return conform(stack.toModern());
    }

    @Override
    public void resetTank() {
        super.resetTank();
        syncLegacyFields();
    }

    @Override
    public int fill(FluidType type, int amount, int pressure, boolean simulate) {
        syncModernFromLegacyFields();
        int moved = super.fill(type, amount, pressure, simulate);
        syncLegacyFields();
        return moved;
    }

    @Override
    public int drain(int amount, boolean simulate) {
        syncModernFromLegacyFields();
        int moved = super.drain(amount, simulate);
        syncLegacyFields();
        return moved;
    }

    @Override
    public void setTankType(FluidType type) {
        FluidType newType = type == null ? HbmFluids.NONE : type;
        if (this.type != newType) {
            this.type = newType;
            this.fluid = 0;
        }
        syncModernFromLegacyFields();
    }

    @Override
    public void setFill(int fill) {
        this.fluid = fill;
        super.setFill(fill);
    }

    @Override
    public int changeTankSize(int maxFill) {
        this.maxFluid = maxFill;
        int overflow = 0;
        if (this.fluid > this.maxFluid) {
            overflow = this.fluid - this.maxFluid;
            this.fluid = this.maxFluid;
        }
        syncModernFromLegacyFields();
        return overflow;
    }

    @Override
    public boolean canAccept(FluidType type, int pressure) {
        syncModernFromLegacyFields();
        return super.canAccept(type, pressure);
    }

    @Override
    public boolean isEmpty() {
        return getTankType() == HbmFluids.NONE || getFill() <= 0;
    }

    @Override
    public int getSpace() {
        return Math.max(0, getMaxFill() - getFill());
    }

    @Override
    public int getSpaceFor(FluidType type) {
        FluidType incoming = type == null ? HbmFluids.NONE : type;
        if (incoming == HbmFluids.NONE || (this.type != HbmFluids.NONE && this.type != incoming)) {
            return 0;
        }
        return getSpace();
    }

    @Override
    public HbmFluidStack getFluidStack() {
        return new HbmFluidStack(getTankType(), getFill(), getPressure());
    }

    public com.hbm.inventory.FluidStack getLegacyFluidStack() {
        return new com.hbm.inventory.FluidStack(getTankType(), getFill(), getPressure());
    }

    @Override
    public FluidType getTankType() {
        return type == null ? HbmFluids.NONE : type;
    }

    @Override
    public int getFill() {
        return fluid;
    }

    @Override
    public int getMaxFill() {
        return maxFluid;
    }

    @Override
    public int getPressure() {
        return pressure;
    }

    @Override
    public TankState snapshot() {
        return new TankState(getTankType(), getFill(), getMaxFill(), getPressure());
    }

    public boolean loadTank(int in, int out, net.minecraft.world.item.ItemStack[] slots) {
        if (slots == null || in < 0 || in >= slots.length || slots[in] == null || slots[in].isEmpty()) {
            return false;
        }
        syncModernFromLegacyFields();
        boolean isInfiniteBarrel = slots[in].is(ModItems.FLUID_BARREL_INFINITE.get());
        if (!isInfiniteBarrel && getPressure() != 0) {
            return false;
        }
        int previous = getFill();
        for (FluidLoadingHandler handler : loadingHandlers) {
            if (handler.emptyItem(slots, in, out, this)) {
                break;
            }
        }
        syncLegacyFields();
        return getFill() > previous;
    }

    public boolean unloadTank(int in, int out, net.minecraft.world.item.ItemStack[] slots) {
        if (slots == null || in < 0 || in >= slots.length || slots[in] == null || slots[in].isEmpty()) {
            return false;
        }
        syncModernFromLegacyFields();
        int previous = getFill();
        for (FluidLoadingHandler handler : loadingHandlers) {
            if (handler.fillItem(slots, in, out, this)) {
                break;
            }
        }
        syncLegacyFields();
        return getFill() < previous;
    }

    public boolean setType(int in, net.minecraft.world.item.ItemStack[] slots) {
        return setType(in, in, slots);
    }

    public boolean setType(int in, int out, net.minecraft.world.item.ItemStack[] slots) {
        if (slots == null || in < 0 || in >= slots.length || out < 0 || out >= slots.length) {
            return false;
        }
        boolean changed = HbmFluidItemTransfer.setTankTypeFromIdentifierSlot(
                FluidLoadingHandler.wrap(slots), in, out, this, null, net.minecraft.core.BlockPos.ZERO);
        syncLegacyFields();
        return changed;
    }

    public void writeToNBT(CompoundTag tag, String key) {
        tag.putInt(key, getFill());
        tag.putInt(key + "_max", getMaxFill());
        tag.putInt(key + "_type", getTankType().getId());
        tag.putShort(key + "_p", (short) getPressure());
    }

    public void readFromNBT(CompoundTag tag, String key) {
        fluid = tag.getInt(key);
        int max = tag.getInt(key + "_max");
        if (max > 0) {
            maxFluid = max;
        }
        fluid = Mth.clamp(fluid, 0, max);
        type = com.hbm.inventory.fluid.Fluids.fromNameCompat(tag.getString(key + "_type"));
        if (type == HbmFluids.NONE) {
            type = com.hbm.inventory.fluid.Fluids.fromID(tag.getInt(key + "_type"));
        }
        pressure = HbmFluidTank.clampPressure(tag.getShort(key + "_p"));
        syncModernFromLegacyFields();
    }

    public void serialize(ByteBuf buf) {
        buf.writeInt(getFill());
        buf.writeInt(getMaxFill());
        buf.writeInt(getTankType().getId());
        buf.writeShort((short) getPressure());
    }

    public void deserialize(ByteBuf buf) {
        fluid = buf.readInt();
        maxFluid = buf.readInt();
        type = com.hbm.inventory.fluid.Fluids.fromID(buf.readInt());
        pressure = HbmFluidTank.clampPressure(buf.readShort());
        syncModernFromLegacyFields();
    }

    @Override
    public FluidTank clone() {
        return new FluidTank(getTankType(), getMaxFill())
                .withPressure(getPressure())
                .setFillForClone(getFill());
    }

    private FluidTank setFillForClone(int fill) {
        setFill(fill);
        return this;
    }

    private void syncModernFromLegacyFields() {
        FluidType legacyType = getTankType();
        int legacyMax = getMaxFill();
        int legacyPressure = getPressure();
        int legacyFill = getFill();
        if (super.getMaxFill() != legacyMax) {
            super.changeTankSize(legacyMax);
        }
        if (super.getTankType() != legacyType) {
            super.setTankType(legacyType);
        }
        if (super.getPressure() != HbmFluidTank.clampPressure(legacyPressure)) {
            super.withPressure(legacyPressure);
        }
        if (super.getFill() != legacyFill) {
            super.setFill(legacyFill);
        }
        syncLegacyFields();
    }

    private void syncLegacyFields() {
        this.type = super.getTankType();
        this.fluid = super.getFill();
        this.maxFluid = super.getMaxFill();
        this.pressure = super.getPressure();
    }
}
