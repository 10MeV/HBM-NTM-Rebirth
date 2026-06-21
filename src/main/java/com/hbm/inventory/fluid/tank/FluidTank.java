package com.hbm.inventory.fluid.tank;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidTank;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;

/**
 * Legacy package facade for 1.7.10 fluid tanks.
 */
@Deprecated(forRemoval = false)
public class FluidTank extends HbmFluidTank implements Cloneable {
    public static final FluidTank[] EMPTY_ARRAY = new FluidTank[0];
    private static final FluidLoadingHandler[] LOADING_HANDLERS = {
            new FluidLoaderStandard(),
            new FluidLoaderFillableItem(),
            new FluidLoaderInfinite()
    };

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
        super.withPressure(pressure);
        syncLegacyFields();
        return this;
    }

    @Override
    public FluidTank conform(com.hbm.ntm.fluid.HbmFluidStack stack) {
        super.conform(stack);
        syncLegacyFields();
        return this;
    }

    @Override
    public void resetTank() {
        super.resetTank();
        syncLegacyFields();
    }

    @Override
    public int fill(FluidType type, int amount, int pressure, boolean simulate) {
        int moved = super.fill(type, amount, pressure, simulate);
        syncLegacyFields();
        return moved;
    }

    @Override
    public int drain(int amount, boolean simulate) {
        int moved = super.drain(amount, simulate);
        syncLegacyFields();
        return moved;
    }

    @Override
    public void setTankType(FluidType type) {
        super.setTankType(type);
        syncLegacyFields();
    }

    @Override
    public void setFill(int fill) {
        super.setFill(fill);
        syncLegacyFields();
    }

    @Override
    public int changeTankSize(int maxFill) {
        int overflow = super.changeTankSize(maxFill);
        syncLegacyFields();
        return overflow;
    }

    public boolean loadTank(int in, int out, net.minecraft.world.item.ItemStack[] slots) {
        if (slots == null || in < 0 || in >= slots.length || slots[in] == null || slots[in].isEmpty()) {
            return false;
        }
        int previous = getFill();
        for (FluidLoadingHandler handler : LOADING_HANDLERS) {
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
        int previous = getFill();
        for (FluidLoadingHandler handler : LOADING_HANDLERS) {
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
        readFromNbt(tag, key);
        syncLegacyFields();
    }

    public void serialize(ByteBuf buf) {
        buf.writeInt(getFill());
        buf.writeInt(getMaxFill());
        buf.writeInt(getTankType().getId());
        buf.writeShort((short) getPressure());
    }

    public void deserialize(ByteBuf buf) {
        int fill = buf.readInt();
        int capacity = buf.readInt();
        FluidType type = com.hbm.inventory.fluid.Fluids.fromID(buf.readInt());
        int pressure = buf.readShort();
        changeTankSize(capacity);
        setTankType(type);
        withPressure(pressure);
        setFill(fill);
        syncLegacyFields();
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

    private void syncLegacyFields() {
        this.type = getTankType();
        this.fluid = getFill();
        this.maxFluid = getMaxFill();
        this.pressure = getPressure();
    }
}
