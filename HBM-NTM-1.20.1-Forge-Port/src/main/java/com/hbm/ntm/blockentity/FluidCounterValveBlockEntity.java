package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidValveBlock;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidCounterValveBlockEntity extends FluidValveBlockEntity
        implements RORValueProvider, RORInteractive {
    private static final String TAG_COUNTER = "counter";
    private long counter;

    public FluidCounterValveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_COUNTER_VALVE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidCounterValveBlockEntity counterValve) {
        if (level.isClientSide) {
            return;
        }
        counterValve.refreshFluidNode();
        if (counterValve.getFluidType() != HbmFluids.NONE) {
            HbmFluidNet net = counterValve.getFluidNet();
            if (net != null) {
                counterValve.counter = Math.max(0L, counterValve.counter + net.getFluidTracker());
            }
        }
        if (level.getGameTime() % 25L == 0L) {
            counterValve.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public long getCounter() {
        return counter;
    }

    public void resetCounter() {
        counter = 0L;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_COUNTER, counter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        counter = Math.max(0L, tag.getLong(TAG_COUNTER));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[] {
                RORInfo.PREFIX_VALUE + "value",
                RORInfo.PREFIX_VALUE + "state",
                RORInfo.PREFIX_FUNCTION + "reset",
                RORInfo.PREFIX_FUNCTION + "setState" + RORInteractive.NAME_SEPARATOR + "state"
        };
    }

    @Override
    public String provideRORValue(String name) {
        if ((RORInfo.PREFIX_VALUE + "value").equals(name)) {
            return Long.toString(counter);
        }
        if ((RORInfo.PREFIX_VALUE + "state").equals(name)) {
            return isOpen() ? "1" : "0";
        }
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        if ((RORInfo.PREFIX_FUNCTION + "reset").equals(name)) {
            resetCounter();
            return null;
        }
        if ((RORInfo.PREFIX_FUNCTION + "setState").equals(name) && params.length > 0) {
            if (level != null && getBlockState().getBlock() instanceof FluidValveBlock valve) {
                int state = RORInteractive.parseInt(params[0], 0, 1);
                valve.setOpen(level, worldPosition, getBlockState(), state == 1, true);
            }
        }
        return null;
    }

}
