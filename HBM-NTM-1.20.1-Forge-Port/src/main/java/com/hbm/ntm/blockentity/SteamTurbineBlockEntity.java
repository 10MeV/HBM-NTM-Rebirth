package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.HbmTurbineConversion;
import com.hbm.ntm.fluid.HbmTurbineConversion.TurbineResult;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SteamTurbineBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidReceiver, HbmStandardFluidSender {
    public static final int INPUT_TANK = 0;
    public static final int OUTPUT_TANK = 1;
    private static final long MAX_POWER = 1_000_000L;
    private static final int INPUT_TANK_SIZE = 64_000;
    private static final int OUTPUT_TANK_SIZE = 128_000;
    private static final int MAX_STEAM_PER_TICK = 6_000;
    private static final double EFFICIENCY = 0.85D;
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(0, -1, 0, Direction.DOWN),
            FluidPort.of(0, 1, 0, Direction.UP),
            FluidPort.of(0, 0, -1, Direction.NORTH),
            FluidPort.of(0, 0, 1, Direction.SOUTH),
            FluidPort.of(-1, 0, 0, Direction.WEST),
            FluidPort.of(1, 0, 0, Direction.EAST));

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private int age;
    private int lastInputUsed;
    private int lastOutputProduced;
    private long lastPowerProduced;

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER),
                new HbmFluidTank(HbmFluids.STEAM, INPUT_TANK_SIZE),
                new HbmFluidTank(HbmFluids.SPENTSTEAM, OUTPUT_TANK_SIZE));
    }

    private SteamTurbineBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank inputTank,
            HbmFluidTank outputTank) {
        super(ModBlockEntities.STEAM_TURBINE.get(), pos, state, energy, List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SteamTurbineBlockEntity turbine) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, turbine);
        turbine.age = (turbine.age + 1) % 2;
        turbine.energy.setPower((long) (turbine.energy.getPower() * 0.95D));
        HbmTurbineConversion.prepareOutputTank(turbine.inputTank, turbine.outputTank);

        TurbineResult result = turbine.runTurbine();
        if (result.powerProduced() > 0L) {
            turbine.energy.setPower(Math.min(turbine.energy.getMaxPower(), turbine.energy.getPower() + result.powerProduced()));
        }
        turbine.lastInputUsed = result.inputUsed();
        turbine.lastOutputProduced = result.outputProduced();
        turbine.lastPowerProduced = result.powerProduced();

        HbmEnergyUtil.tryProvideToAllNeighbors(level, pos, turbine.energy);
        if (turbine.outputTank.getTankType() != HbmFluids.NONE && turbine.outputTank.getFill() > 0) {
            turbine.tryProvideFluidToPorts(turbine.outputTank.getTankType(), turbine.outputTank.getPressure(), turbine);
        }
        if (result.converted() || level.getGameTime() % 20L == 0L) {
            turbine.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private TurbineResult runTurbine() {
        return HbmTurbineConversion.run(inputTank, outputTank, EFFICIENCY, MAX_STEAM_PER_TICK, false);
    }

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
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
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
        return side == null ? HbmFluidSideMode.BOTH : HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("age", age);
        tag.putInt("lastInputUsed", lastInputUsed);
        tag.putInt("lastOutputProduced", lastOutputProduced);
        tag.putLong("lastPowerProduced", lastPowerProduced);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        age = Math.floorMod(tag.getInt("age"), 2);
        lastInputUsed = Math.max(0, tag.getInt("lastInputUsed"));
        lastOutputProduced = Math.max(0, tag.getInt("lastOutputProduced"));
        lastPowerProduced = Math.max(0L, tag.getLong("lastPowerProduced"));
    }

    public static boolean isValidTurbineInput(FluidType type) {
        CoolableFluidTrait trait = type == null ? null : type.getTrait(CoolableFluidTrait.class);
        return trait != null && trait.getEfficiency(CoolableFluidTrait.CoolingType.TURBINE) > 0.0D;
    }
}
