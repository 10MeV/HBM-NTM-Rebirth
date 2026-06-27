package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.api.tile.HeatSource;
import com.hbm.ntm.config.BoilerConfig;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidThermalExchange;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingStep;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.world.saveddata.TomImpactSavedData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BoilerBlockEntity extends HbmFluidNetworkBlockEntity implements HbmStandardFluidReceiver, HbmStandardFluidSender, HeatSource, RORValueProvider {
    public static final int FEED_TANK = 0;
    public static final int STEAM_TANK = 1;

    private final BoilerProfile profile;
    private final HbmFluidTank feedTank;
    private final HbmFluidTank steamTank;
    private final RORDispatcher ror;
    private int heat;
    private boolean active;
    private boolean hasExploded;
    private Object audioLoop;

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        this(
                pos,
                state,
                profileFromState(state)
        );
    }

    private BoilerBlockEntity(BlockPos pos, BlockState state, BoilerProfile profile) {
        this(pos, state, profile, new HbmFluidTank(HbmFluids.WATER, profile.feedCapacity),
                new HbmFluidTank(HbmFluids.STEAM, profile.feedCapacity * 100));
    }

    private BoilerBlockEntity(BlockPos pos, BlockState state, BoilerProfile profile,
            HbmFluidTank feedTank, HbmFluidTank steamTank) {
        super(ModBlockEntities.BOILER.get(), pos, state, List.of(feedTank, steamTank));
        this.profile = profile;
        this.feedTank = feedTank;
        this.steamTank = steamTank;
        this.ror = RORDispatcher.builder()
                .value("input", () -> Integer.toString(this.feedTank.getFill()))
                .value("output", () -> Integer.toString(this.steamTank.getFill()))
                .build();
        this.feedTank.conform(new HbmFluidStack(HbmFluids.WATER, 0));
        this.steamTank.conform(new HbmFluidStack(HbmFluids.STEAM, 0));
    }

    public HbmFluidTank getFeedTank() {
        return feedTank;
    }

    public HbmFluidTank getSteamTank() {
        return steamTank;
    }

    public int getHeat() {
        return heat;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasExploded() {
        return hasExploded;
    }

    public int getSteamFill() {
        return steamTank.getFill();
    }

    public int getSteamMaxFill() {
        return steamTank.getMaxFill();
    }

    public void setFeedTankType(FluidType type) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        HeatableFluidTrait trait = type.getTrait(HeatableFluidTrait.class);
        if (trait == null || trait.getEfficiency(HeatingType.BOILER) <= 0.0D) {
            return;
        }
        feedTank.setTankType(type);
        prepareOutputTank();
        onFluidContentsChanged();
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
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
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        if (hasExploded) {
            return null;
        }
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.heatTu(heat),
                LegacyLookOverlayLines.tank(true, feedTank),
                LegacyLookOverlayLines.tank(false, steamTank)));
    }

    @Override
    public int getHeatStored() {
        return heat;
    }

    @Override
    public void useUpHeat(int heat) {
        this.heat = Math.max(0, this.heat - Math.max(0, heat));
    }

    public void addHeat(int heat) {
        this.heat = Math.min(maxHeat(), Math.max(0, this.heat + Math.max(0, heat)));
    }

    public HbmFluidThermalExchange.ThermalResult previewBoiling() {
        prepareOutputTank();
        return HbmFluidThermalExchange.heat(feedTank, steamTank, HeatingType.BOILER, heat, true);
    }

    public HbmFluidThermalExchange.ThermalResult tryBoil(int availableHeat, boolean simulate) {
        if (!simulate) {
            prepareOutputTank();
        }
        HbmFluidThermalExchange.ThermalResult result = HbmFluidThermalExchange.heat(feedTank, steamTank, HeatingType.BOILER, availableHeat, simulate);
        if (!simulate && result.converted()) {
            heat = Math.max(0, heat - result.heatUsed());
            onFluidContentsChanged();
        }
        return result;
    }

    private void prepareOutputTank() {
        HeatableFluidTrait trait = feedTank.getTankType().getTrait(HeatableFluidTrait.class);
        HeatingStep step = trait == null || trait.getEfficiency(HeatingType.BOILER) <= 0.0D ? null : trait.getFirstStep();
        if (step == null || step.amountRequired() <= 0 || step.amountProduced() <= 0) {
            steamTank.setTankType(HbmFluids.NONE);
            return;
        }
        steamTank.setTankType(step.producedType());
        steamTank.changeTankSize(feedTank.getMaxFill() * step.amountProduced() / step.amountRequired());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoilerBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        if (blockEntity.hasExploded) {
            blockEntity.active = false;
            blockEntity.networkPackNT(25);
            return;
        }
        blockEntity.normalizeConfigState();
        blockEntity.prepareOutputTank();
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, blockEntity);
        blockEntity.pullHeatFromBelow(level, pos);
        blockEntity.pullHeatFromTomFire(level, pos);
        HbmFluidThermalExchange.ThermalResult result = blockEntity.tryBoil(blockEntity.heat, false);
        blockEntity.active = result.converted();
        if (blockEntity.active && level.random.nextInt(400) == 0) {
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D,
                    ModSounds.BLOCK_BOILER_GROAN.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        if (!blockEntity.profile.industrial && !result.converted() && blockEntity.steamTank.getMaxFill() > 0
                && blockEntity.steamTank.getFill() >= blockEntity.steamTank.getMaxFill()
                && BoilerConfig.canExplode()) {
            blockEntity.burst(level, pos);
            blockEntity.networkPackNT(25);
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            return;
        }
        if (blockEntity.steamTank.getTankType() != HbmFluids.NONE && blockEntity.steamTank.getFill() > 0) {
            blockEntity.tryProvideFluidToPorts(blockEntity.steamTank.getTankType(), blockEntity.steamTank.getPressure(), blockEntity);
        }
        if (result.converted()) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        blockEntity.networkPackNT(25);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BoilerBlockEntity blockEntity) {
        if (!level.isClientSide) {
            return;
        }
        blockEntity.audioLoop = LegacyMachineAudioBridge.updateLoop(blockEntity.audioLoop, blockEntity,
                "hbm:block.boiler", blockEntity.active && !blockEntity.hasExploded, 10.0D, 20.0F,
                0.125F, 1.0F);
    }

    private void normalizeConfigState() {
        heat = Math.min(maxHeat(), Math.max(0, heat));
        feedTank.changeTankSize(profile.feedCapacity);
    }

    private void pullHeatFromBelow(Level level, BlockPos pos) {
        BlockEntity sourceBlockEntity = level.getBlockEntity(pos.below());
        if (sourceBlockEntity instanceof HeatSource source) {
            int diff = source.getHeatStored() - heat;
            if (diff > 0) {
                int transferred = (int) Math.ceil(diff * BoilerConfig.diffusion());
                transferred = Math.min(transferred, maxHeat() - heat);
                if (transferred > 0) {
                    source.useUpHeat(transferred);
                    heat = Math.min(maxHeat(), heat + transferred);
                    return;
                }
            }
            if (diff == 0) {
                return;
            }
        }
        if (heat > 0) {
            heat = Math.max(heat - Math.max(heat / 1000, 1), 0);
        }
    }

    private void pullHeatFromTomFire(Level level, BlockPos pos) {
        if (level.getBrightness(LightLayer.SKY, pos) <= 7) {
            return;
        }
        TomImpactSavedData.forLevel(level).ifPresent(data -> {
            if (data.fire() > 1.0e-5F) {
                heat += (int) ((maxHeat() - heat) * 0.000005D);
                heat = Math.min(maxHeat(), Math.max(0, heat));
            }
        });
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(feedTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(steamTank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == feedTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type != HbmFluids.NONE && type == steamTank.getTankType();
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        if (profile.industrial) {
            return List.of(
                    FluidPort.of(2, 0, 0, Direction.EAST),
                    FluidPort.of(-2, 0, 0, Direction.WEST),
                    FluidPort.of(0, 0, 2, Direction.SOUTH),
                    FluidPort.of(0, 0, -2, Direction.NORTH),
                    FluidPort.of(0, 5, 0, Direction.UP));
        }
        Direction rot = getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING).getClockWise()
                : Direction.WEST;
        return List.of(
                FluidPort.of(rot.getStepX() * 2, 0, rot.getStepZ() * 2, rot),
                FluidPort.of(-rot.getStepX() * 2, 0, -rot.getStepZ() * 2, rot.getOpposite()),
                FluidPort.of(0, 4, 0, Direction.UP));
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(feedTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(steamTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("heat", heat);
        tag.putBoolean("exploded", hasExploded);
        feedTank.writeToNbt(tag, "water");
        steamTank.writeToNbt(tag, "steam");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        normalizeConfigState();
        if (tag.contains("water")) {
            feedTank.readFromNbt(tag, "water");
        }
        if (tag.contains("steam")) {
            steamTank.readFromNbt(tag, "steam");
        }
        prepareOutputTank();
        heat = Math.min(maxHeat(), Math.max(0, tag.getInt("heat")));
        hasExploded = tag.getBoolean("exploded");
        readRuntimeSync(tag);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putBoolean("isOn", active);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        readRuntimeSync(tag);
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

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    private void readRuntimeSync(CompoundTag tag) {
        if (tag.contains("isOn")) {
            active = tag.getBoolean("isOn");
        }
    }

    private int maxHeat() {
        return BoilerConfig.maxHeat(profile.industrial);
    }

    private void burst(Level level, BlockPos pos) {
        hasExploded = true;
        active = false;
        heat = 0;
        MultiblockHelper.removeOffsets(level, pos, basicBoilerBurstOffsets());
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D,
                5.0F, false, Level.ExplosionInteraction.BLOCK);
    }

    private static List<BlockPos> basicBoilerBurstOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        offsets.add(new BlockPos(0, 1, 0));
        for (int x = -1; x <= 1; x++) {
            for (int y = 2; y <= 3; y++) {
                for (int z = -1; z <= 1; z++) {
                    offsets.add(new BlockPos(x, y, z));
                }
            }
        }
        return offsets;
    }

    private static BoilerProfile profileFromState(BlockState state) {
        return state.is(ModBlocks.MACHINE_INDUSTRIAL_BOILER.get())
                ? BoilerProfile.INDUSTRIAL
                : BoilerProfile.BASIC;
    }

    private enum BoilerProfile {
        BASIC(false, 16_000),
        INDUSTRIAL(true, 64_000);

        private final boolean industrial;
        private final int feedCapacity;

        BoilerProfile(boolean industrial, int feedCapacity) {
            this.industrial = industrial;
            this.feedCapacity = feedCapacity;
        }
    }
}
