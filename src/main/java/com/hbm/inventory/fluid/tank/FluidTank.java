package com.hbm.inventory.fluid.tank;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;

/**
 * Legacy package facade for 1.7.10 fluid tanks.
 */
@Deprecated(forRemoval = false)
public class FluidTank extends HbmFluidTank implements Cloneable {
    public static final FluidTank[] EMPTY_ARRAY = new FluidTank[0];

    public FluidTank(FluidType type, int maxFill) {
        super(type, maxFill);
    }

    @Override
    public FluidTank withPressure(int pressure) {
        super.withPressure(pressure);
        return this;
    }

    @Override
    public FluidTank conform(com.hbm.ntm.fluid.HbmFluidStack stack) {
        super.conform(stack);
        return this;
    }

    public void writeToNBT(CompoundTag tag, String key) {
        tag.putInt(key, getFill());
        tag.putInt(key + "_max", getMaxFill());
        tag.putInt(key + "_type", getTankType().getId());
        tag.putShort(key + "_p", (short) getPressure());
    }

    public void readFromNBT(CompoundTag tag, String key) {
        readFromNbt(tag, key);
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
}
