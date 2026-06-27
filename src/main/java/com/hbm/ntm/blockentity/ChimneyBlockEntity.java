package com.hbm.ntm.blockentity;

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
import com.hbm.ntm.particle.ParticleUtil;
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
        implements HbmFluidConnector, HbmFluidReceiver, HbmLegacyLoadedTile {
    private static final String TAG_ON_TICKS = "onTicks";
    private static final String TAG_ASH_TICK = "ashTick";
    private static final String TAG_SOOT_TICK = "sootTick";
    private static final List<FluidPort> SMOKE_PORTS = HbmFluidPortLayouts.cardinal(2);
    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private int onTicks;
    private long ashTick;
    private long sootTick;

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
        chimney.flushCapturedAsh();
        chimney.networkPackNT(150);
        if (chimney.onTicks > 0) {
            chimney.onTicks--;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ChimneyBlockEntity chimney) {
        if (!level.isClientSide || chimney.onTicks <= 0 || level.getGameTime() % 2L != 0L) {
            return;
        }
        if (state.is(ModBlocks.CHIMNEY_INDUSTRIAL.get())) {
            ParticleUtil.spawnIndustrialChimneySmoke(level, pos);
        } else {
            ParticleUtil.spawnBrickChimneySmoke(level, pos);
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
        ashTick = addClamped(ashTick, amount);
        if (getBlockState().is(ModBlocks.CHIMNEY_INDUSTRIAL.get())) {
            sootTick = addClamped(sootTick, amount);
        }
        SmokeExhaustPollution.pollute(level, worldPosition, type, amount, pollutionMultiplier());
        return 0L;
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        return SmokeExhaustPollution.isSmoke(type) ? 1_000_000L : 0L;
    }

    @Override
    public AABB getRenderBoundingBox() {
        int height = getBlockState().is(ModBlocks.CHIMNEY_INDUSTRIAL.get()) ? 23 : 13;
        return new AABB(worldPosition.getX() - 1.0D, worldPosition.getY(), worldPosition.getZ() - 1.0D,
                worldPosition.getX() + 2.0D, worldPosition.getY() + height, worldPosition.getZ() + 2.0D);
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
        tag.putInt(TAG_ON_TICKS, onTicks);
        tag.putLong(TAG_ASH_TICK, ashTick);
        tag.putLong(TAG_SOOT_TICK, sootTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        onTicks = Math.max(0, tag.getInt(TAG_ON_TICKS));
        ashTick = Math.max(0L, tag.getLong(TAG_ASH_TICK));
        sootTick = Math.max(0L, tag.getLong(TAG_SOOT_TICK));
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

    private void flushCapturedAsh() {
        if (level == null || (ashTick <= 0L && sootTick <= 0L)) {
            return;
        }
        if (level.getBlockEntity(worldPosition.below()) instanceof AshpitBlockEntity ashpit) {
            if (ashTick > 0L) {
                ashpit.addFlyAsh(ashTick);
            }
            if (sootTick > 0L) {
                ashpit.addSoot(sootTick);
            }
        }
        ashTick = 0L;
        sootTick = 0L;
        setChanged();
    }

    private static long addClamped(long current, long amount) {
        if (amount <= 0L) {
            return current;
        }
        long result = current + amount;
        return result < 0L ? Long.MAX_VALUE : result;
    }
}
