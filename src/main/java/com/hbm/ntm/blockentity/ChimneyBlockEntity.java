package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.config.RadiationConfig;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ChimneyBlockEntity extends BlockEntity
        implements HbmFluidConnector, HbmFluidReceiver, LegacyLookOverlayProvider, HbmLegacyLoadedTile {
    private static final String TAG_ON_TICKS = "onTicks";
    private static final List<FluidPort> SMOKE_PORTS = HbmFluidPortLayouts.cardinal(2);
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private int onTicks;

    public ChimneyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHIMNEY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChimneyBlockEntity chimney) {
        if (level.isClientSide) {
            return;
        }
        if (level.getGameTime() % 20L == 0L) {
            chimney.refreshSubscriptions();
        }
        chimney.networkPackNT(150);
        if (chimney.onTicks > 0) {
            chimney.onTicks--;
        }
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public int getOnTicks() {
        return onTicks;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null
                && side.getAxis().isHorizontal()
                && SmokeExhaustPollution.isSmoke(type);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of();
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (!SmokeExhaustPollution.isSmoke(type) || amount <= 0L) {
            return amount;
        }
        onTicks = 20;
        captureAshpitProducts(amount);
        SmokeExhaustPollution.pollute(level, worldPosition, type, amount, pollutionMultiplier());
        return 0L;
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        return SmokeExhaustPollution.isSmoke(type) ? 1_000_000L : 0L;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.fluidNames(SmokeExhaustPollution.SMOKES));
    }

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }

    private void refreshSubscriptions() {
        for (FluidType type : SmokeExhaustPollution.SMOKES) {
            for (FluidPort port : SMOKE_PORTS) {
                HbmFluidUtil.subscribeReceiverToPort(level, worldPosition, port, type, this);
            }
        }
    }

    private double pollutionMultiplier() {
        return RadiationConfig.chimneyPollutionMultiplier(getBlockState().is(ModBlocks.CHIMNEY_INDUSTRIAL.get()));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        writeLegacyLoadedTileClientTag(tag);
        tag.putInt(TAG_ON_TICKS, onTicks);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        readLegacyLoadedTileClientTag(tag);
        if (tag.contains(TAG_ON_TICKS)) {
            onTicks = Math.max(0, tag.getInt(TAG_ON_TICKS));
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    private void captureAshpitProducts(long amount) {
        if (level == null || amount <= 0L) {
            return;
        }
        if (level.getBlockEntity(worldPosition.below()) instanceof AshpitBlockEntity ashpit) {
            ashpit.addFlyAsh(amount);
            if (getBlockState().is(ModBlocks.CHIMNEY_INDUSTRIAL.get())) {
                ashpit.addSoot(amount);
            }
        }
    }
}
