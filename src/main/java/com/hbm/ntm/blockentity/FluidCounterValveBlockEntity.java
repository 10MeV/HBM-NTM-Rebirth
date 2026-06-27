package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidValveBlock;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RORInfo;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.network.HbmLegacyBufPacketReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FluidCounterValveBlockEntity extends FluidValveBlockEntity
        implements RORValueProvider, RORInteractive, HbmLegacyBufPacketReceiver {
    private static final String TAG_COUNTER = "counter";
    private final RORDispatcher ror;
    private long counter;

    public FluidCounterValveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_COUNTER_VALVE.get(), pos, state);
        this.ror = createRorDispatcher();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidCounterValveBlockEntity counterValve) {
        if (level.isClientSide) {
            return;
        }
        counterValve.refreshFluidNode();
        long oldCounter = counterValve.counter;
        if (counterValve.getFluidType() != HbmFluids.NONE) {
            HbmFluidNet net = counterValve.getFluidNet();
            if (net != null) {
                counterValve.counter = Math.max(0L, counterValve.counter + net.getFluidTracker());
            }
        }
        if (counterValve.counter != oldCounter && level.getGameTime() % 25L == 0L) {
            counterValve.setChanged();
        }
        counterValve.networkPackNT(25);
    }

    public long getCounter() {
        return counter;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.fluidName(getFluidType()),
                LegacyLookOverlayLines.counter(counter)));
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
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeLong(counter);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        counter = Math.max(0L, data.readLong());
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
        return ror.getFunctionInfo();
    }

    @Override
    public String provideRORValue(String name) {
        return ror.provideValue(name);
    }

    @Override
    public String runRORFunction(String name, String[] params) {
        return ror.runFunction(name, params);
    }

    private RORDispatcher createRorDispatcher() {
        return RORDispatcher.builder()
                .value("value", () -> Long.toString(counter))
                .value("state", () -> isOpen() ? "1" : "0")
                .function("reset", params -> {
                    resetCounter();
                    return null;
                })
                .function("setState", this::runRorSetState, "state")
                .build();
    }

    private String runRorSetState(String[] params) {
        if (params.length > 0 && level != null && getBlockState().getBlock() instanceof FluidValveBlock valve) {
            int state = RORInteractive.parseInt(params[0], 0, 1);
            valve.setOpen(level, worldPosition, getBlockState(), state == 1, true);
        }
        return null;
    }

}
