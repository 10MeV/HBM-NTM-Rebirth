package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.HbmTurbineConversion;
import com.hbm.ntm.fluid.HbmTurbineConversion.TurbineResult;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class LegacySteamTurbineBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidReceiver, HbmStandardFluidSender {
    public static final int INPUT_TANK = 0;
    public static final int OUTPUT_TANK = 1;

    protected final HbmFluidTank inputTank;
    protected final HbmFluidTank outputTank;
    private int lastInputUsed;
    private int lastOutputProduced;
    private long lastPowerProduced;
    private boolean operational;

    protected LegacySteamTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, long maxPower,
            int inputTankSize, int outputTankSize) {
        this(type, pos, state, new HbmEnergyStorage(maxPower, 0L, Math.max(1L, maxPower)),
                new HbmFluidTank(HbmFluids.STEAM, inputTankSize),
                new HbmFluidTank(HbmFluids.SPENTSTEAM, outputTankSize));
    }

    private LegacySteamTurbineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            HbmEnergyStorage energy, HbmFluidTank inputTank, HbmFluidTank outputTank) {
        super(type, pos, state, energy, List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    protected static <T extends LegacySteamTurbineBlockEntity> void tickTurbine(Level level, BlockPos pos,
            BlockState state, T turbine) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, turbine);
        turbine.normalizeConfigState();
        HbmTurbineConversion.prepareOutputTank(turbine.inputTank, turbine.outputTank);

        turbine.beforeTurbineTick();
        TurbineResult result = turbine.runConversion();
        turbine.updateLastResult(result);
        turbine.applyGeneratedPower(result.powerProduced());
        turbine.afterTurbineTick();

        if (turbine.energy.getPower() > 0L) {
            turbine.tryProvideEnergy(level, pos);
        }
        if (turbine.outputTank.getTankType() != HbmFluids.NONE && turbine.outputTank.getFill() > 0) {
            turbine.tryProvideFluidToPorts(turbine.outputTank.getTankType(), turbine.outputTank.getPressure(), turbine);
        }
        if (result.converted() || level.getGameTime() % 20L == 0L) {
            turbine.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    protected TurbineResult runConversion() {
        return HbmTurbineConversion.runPercentOfAvailable(inputTank, outputTank, getEfficiency(),
                getConsumptionPercent(), false);
    }

    protected final void updateLastResult(TurbineResult result) {
        operational = result.converted();
        lastInputUsed = result.inputUsed();
        lastOutputProduced = result.outputProduced();
        lastPowerProduced = result.powerProduced();
    }

    protected void applyGeneratedPower(long power) {
        if (power > 0L) {
            energy.setPower(Math.min(energy.getMaxPower(), energy.getPower() + power));
        }
    }

    protected void afterTurbineTick() {
    }

    protected void beforeTurbineTick() {
    }

    protected void normalizeConfigState() {
    }

    protected void normalizeTankCapacity(int inputCapacity, int outputCapacity) {
        inputTank.changeTankSize(inputCapacity);
        outputTank.changeTankSize(outputCapacity);
    }

    protected void tryProvideEnergy(Level level, BlockPos pos) {
        Iterable<EnergyPort> ports = getEnergyPorts();
        if (ports.iterator().hasNext()) {
            HbmEnergyUtil.tryProvideToPorts(level, pos, ports, energy);
        } else {
            HbmEnergyUtil.tryProvideToAllNeighbors(level, pos, energy);
        }
    }

    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of();
    }

    protected abstract double getEfficiency();

    protected abstract double getConsumptionPercent();

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    public int getLastInputUsed() {
        return lastInputUsed;
    }

    public int getLastOutputProduced() {
        return lastOutputProduced;
    }

    public long getLastPowerProduced() {
        return lastPowerProduced;
    }

    public boolean isOperational() {
        return operational;
    }

    @Nullable
    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return null;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.tank(true, inputTank),
                LegacyLookOverlayLines.tank(false, outputTank),
                LegacyLookOverlayLines.energyOut(lastPowerProduced)));
    }

    protected Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    protected Direction rotatedFacing() {
        return facing().getClockWise();
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(outputTank);
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
        return type != HbmFluids.NONE && type == inputTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type != HbmFluids.NONE && outputTank.getFill() > 0 && type == outputTank.getTankType();
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(inputTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(outputTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("lastInputUsed", lastInputUsed);
        tag.putInt("lastOutputProduced", lastOutputProduced);
        tag.putLong("lastPowerProduced", lastPowerProduced);
        tag.putBoolean("operational", operational);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        normalizeConfigState();
        lastInputUsed = Math.max(0, tag.getInt("lastInputUsed"));
        lastOutputProduced = Math.max(0, tag.getInt("lastOutputProduced"));
        lastPowerProduced = Math.max(0L, tag.getLong("lastPowerProduced"));
        operational = tag.getBoolean("operational");
    }

    protected static FluidPort fluidPort(Direction direction, int x, int y, int z) {
        return FluidPort.of(x, y, z, direction);
    }

    protected static EnergyPort energyPort(Direction direction, int x, int y, int z) {
        return EnergyPort.of(x, y, z, direction);
    }
}
