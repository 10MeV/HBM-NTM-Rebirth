package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmPowerNet;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RedCableGaugeBlockEntity extends HbmEnergyNodeBlockEntity
        implements LegacyLookOverlayProvider, RORValueProvider {
    private static final String TAG_DELTA_TICK = "deltaTick";
    private static final String TAG_DELTA_SECOND = "deltaSecond";
    private static final String TAG_DELTA_LAST_SECOND = "deltaLastSecond";
    private final RORDispatcher ror;

    private long deltaTick;
    private long deltaSecond;
    private long deltaLastSecond;

    public RedCableGaugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_CABLE_GAUGE.get(), pos, state);
        this.ror = RORDispatcher.builder()
                .value("deltatick", () -> Long.toString(deltaTick))
                .value("deltasecond", () -> Long.toString(deltaLastSecond))
                .build();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RedCableGaugeBlockEntity gauge) {
        if (level.isClientSide) {
            return;
        }

        HbmEnergyNode node = gauge.getEnergyNode();
        HbmPowerNet net = node == null ? null : node.getPowerNet();
        gauge.deltaTick = net == null ? 0L : Math.max(0L, net.getEnergyTracker());
        gauge.deltaSecond += gauge.deltaTick;

        if (level.getGameTime() % 20L == 0L) {
            gauge.deltaLastSecond = gauge.deltaSecond;
            gauge.deltaSecond = 0L;
        }
        if (level.getGameTime() % 25L == 0L) {
            gauge.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public long getDeltaTick() {
        return deltaTick;
    }

    public long getDeltaLastSecond() {
        return deltaLastSecond;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.shortRate(deltaTick, "HE/t"),
                LegacyLookOverlayLines.shortRate(deltaLastSecond, "HE/s")));
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
    public String[] getFunctionInfo() {
        return ror.getFunctionInfo();
    }

    @Override
    public String provideRORValue(String name) {
        return ror.provideValue(name);
    }
}
