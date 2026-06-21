package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
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

public class FluidDuctGaugeBlockEntity extends FluidPipeBlockEntity implements RORValueProvider, HbmLegacyBufPacketReceiver {
    private static final String TAG_DELTA_TICK = "deltaTick";
    private static final String TAG_DELTA_SECOND = "deltaSecond";
    private static final String TAG_DELTA_LAST_SECOND = "deltaLastSecond";
    private final RORDispatcher ror;

    private long deltaTick;
    private long deltaSecond;
    private long deltaLastSecond;

    public FluidDuctGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_DUCT_GAUGE.get(), pos, state);
        this.ror = RORDispatcher.builder()
                .value("deltatick", () -> Long.toString(deltaTick))
                .value("deltasecond", () -> Long.toString(deltaLastSecond))
                .build();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidDuctGaugeBlockEntity gauge) {
        if (level.isClientSide) {
            return;
        }
        if (gauge.getFluidType() != HbmFluids.NONE) {
            HbmFluidNet net = gauge.getFluidNet();
            gauge.deltaTick = net == null ? 0L : Math.max(0L, net.getFluidTracker());
            gauge.deltaSecond += gauge.deltaTick;
        } else {
            gauge.deltaTick = 0L;
        }

        if (level.getGameTime() % 20L == 0L) {
            gauge.deltaLastSecond = gauge.deltaSecond;
            gauge.deltaSecond = 0L;
        }
        if (level.getGameTime() % 25L == 0L) {
            gauge.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        gauge.networkPackNT(25);
    }

    public long getDeltaTick() {
        return deltaTick;
    }

    public long getDeltaLastSecond() {
        return deltaLastSecond;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, java.util.List.of(
                LegacyLookOverlayLines.fluidName(getFluidType()),
                LegacyLookOverlayLines.rate(deltaTick, "mB/t"),
                LegacyLookOverlayLines.rate(deltaLastSecond, "mB/s")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_DELTA_TICK, deltaTick);
        tag.putLong(TAG_DELTA_SECOND, deltaSecond);
        tag.putLong(TAG_DELTA_LAST_SECOND, deltaLastSecond);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        deltaTick = Math.max(0L, tag.getLong(TAG_DELTA_TICK));
        deltaSecond = Math.max(0L, tag.getLong(TAG_DELTA_SECOND));
        deltaLastSecond = Math.max(0L, tag.getLong(TAG_DELTA_LAST_SECOND));
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
    public void serialize(FriendlyByteBuf data) {
        data.writeLong(deltaTick);
        data.writeLong(deltaLastSecond);
    }

    @Override
    public void deserialize(FriendlyByteBuf data) {
        deltaTick = Math.max(data.readLong(), 0L);
        deltaLastSecond = Math.max(data.readLong(), 0L);
    }

    @Override
    public String[] getFunctionInfo() {
        return ror.getFunctionInfo();
    }

    @Override
    public String provideRORValue(String name) {
        return ror.provideValue(name);
    }
}
