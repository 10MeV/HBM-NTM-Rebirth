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
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BoilerBlockEntity extends HbmFluidNetworkBlockEntity implements HbmStandardFluidReceiver, HbmStandardFluidSender, HeatSource, RORValueProvider {
    public static final int FEED_TANK = 0;
    public static final int STEAM_TANK = 1;
    private static final int FEED_TANK_CAPACITY = 16_000;
    private static final int STEAM_TANK_CAPACITY = FEED_TANK_CAPACITY * 100;

    private final HbmFluidTank feedTank;
    private final HbmFluidTank steamTank;
    private final RORDispatcher ror;
    private int heat;

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        this(
                pos,
                state,
                new HbmFluidTank(HbmFluids.WATER, FEED_TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.STEAM, STEAM_TANK_CAPACITY)
        );
    }

    private BoilerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank feedTank, HbmFluidTank steamTank) {
        super(ModBlockEntities.BOILER.get(), pos, state, List.of(feedTank, steamTank));
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
        this.heat = Math.min(BoilerConfig.maxHeat(), Math.max(0, this.heat + Math.max(0, heat)));
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
        blockEntity.normalizeConfigState();
        blockEntity.prepareOutputTank();
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, blockEntity);
        blockEntity.pullHeatFromBelow(level, pos);
        HbmFluidThermalExchange.ThermalResult result = blockEntity.tryBoil(blockEntity.heat, false);
        if (blockEntity.steamTank.getTankType() != HbmFluids.NONE && blockEntity.steamTank.getFill() > 0) {
            blockEntity.tryProvideFluidToPorts(blockEntity.steamTank.getTankType(), blockEntity.steamTank.getPressure(), blockEntity);
        }
        if (result.converted() || level.getGameTime() % 20L == 0L) {
            blockEntity.setChanged();
        }
    }

    private void normalizeConfigState() {
        heat = Math.min(BoilerConfig.maxHeat(), Math.max(0, heat));
    }

    private void pullHeatFromBelow(Level level, BlockPos pos) {
        BlockEntity sourceBlockEntity = level.getBlockEntity(pos.below());
        if (sourceBlockEntity instanceof HeatSource source) {
            int diff = source.getHeatStored() - heat;
            if (diff > 0) {
                int transferred = (int) Math.ceil(diff * BoilerConfig.diffusion());
                transferred = Math.min(transferred, BoilerConfig.maxHeat() - heat);
                if (transferred > 0) {
                    source.useUpHeat(transferred);
                    heat = Math.min(BoilerConfig.maxHeat(), heat + transferred);
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
        Direction facing = getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        return List.of(
                FluidPort.of(facing.getStepX() * 2, 0, facing.getStepZ() * 2, facing),
                FluidPort.of(-facing.getStepX() * 2, 0, -facing.getStepZ() * 2, facing.getOpposite()),
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
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        heat = Math.min(BoilerConfig.maxHeat(), Math.max(0, tag.getInt("heat")));
    }
}
