package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.config.SteamTurbineConfig;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmTurbineConversion;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait;
import com.hbm.ntm.fluid.trait.CoolableFluidTrait.CoolingType;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialSteamTurbineBlockEntity extends LegacySteamTurbineBlockEntity implements RORValueProvider {
    private static final String[] SPIN_BLOCKS = new String[] {"", "|", "/", "\\"};
    private static final long MAX_STORED_POWER = 100_000_000L;
    private static final double CONSUMPTION_PERCENT = 0.2D;
    private static final double FLYWHEEL_MAX_ENERGY = 50_000_000D;

    private double spin;
    private long lastPowerTarget;
    private long flywheelEnergy;
    private long maxPowerTarget;
    private final RORDispatcher ror;

    public IndustrialSteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INDUSTRIAL_STEAM_TURBINE.get(), pos, state, MAX_STORED_POWER,
                SteamTurbineConfig.industrialInputTankSize(), SteamTurbineConfig.industrialOutputTankSize());
        this.ror = RORDispatcher.builder()
                .value("output", () -> Long.toString(lastPowerTarget))
                .value("flywheel", () -> Integer.toString((int) (spin * 100.0D)))
                .build();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IndustrialSteamTurbineBlockEntity turbine) {
        tickTurbine(level, pos, state, turbine);
    }

    @Override
    protected double getEfficiency() {
        return SteamTurbineConfig.industrialEfficiency();
    }

    @Override
    protected double getConsumptionPercent() {
        return CONSUMPTION_PERCENT;
    }

    @Override
    protected void applyGeneratedPower(long power) {
        maxPowerTarget = HbmTurbineConversion.previewMaxPowerForPercent(inputTank, getEfficiency(),
                CONSUMPTION_PERCENT);
        if (power > 0L) {
            flywheelEnergy = Math.min(Long.MAX_VALUE - power, flywheelEnergy) + power;
        }
    }

    @Override
    protected void normalizeConfigState() {
        normalizeTankCapacity(SteamTurbineConfig.industrialInputTankSize(),
                SteamTurbineConfig.industrialOutputTankSize());
    }

    @Override
    protected void afterTurbineTick() {
        spin = Math.max(0.0D, flywheelEnergy / FLYWHEEL_MAX_ENERGY);
        lastPowerTarget = Math.max(0L, (long) (spin * maxPowerTarget));
        flywheelEnergy = Math.max(0L, flywheelEnergy - lastPowerTarget);
        energy.setPower(Math.min(energy.getMaxPower(), lastPowerTarget));
    }

    public double getSpin() {
        return spin;
    }

    public long getLastPowerTarget() {
        return lastPowerTarget;
    }

    public long getFlywheelEnergy() {
        return flywheelEnergy;
    }

    public long getMaxPowerTarget() {
        return maxPowerTarget;
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
        int spinner = flywheelEnergy <= 0L ? 0 : (int) ((level.getGameTime() / 4L) % 4L);
        int spinPercent = (int) Math.round(spin * 100.0D);
        FluidType inputType = inputTank.getTankType();
        CoolableFluidTrait trait = inputType.getTrait(CoolableFluidTrait.class);
        FluidType outputType = trait != null && trait.getEfficiency(CoolingType.TURBINE) > 0.0D
                ? trait.getCoolsTo()
                : HbmFluids.NONE;
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.groupedCompactTank(true, inputTank),
                LegacyLookOverlayLines.groupedCompactTank(false, outputType, outputTank.getFill(),
                        outputTank.getMaxFill()),
                LegacyLookOverlayLines.industrialTurbineEnergyOut(lastPowerTarget, SPIN_BLOCKS[spinner],
                        spinPercent, spin)));
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = rotatedFacing();
        return List.of(
                fluidPort(rot, facing.getStepX() * 3 + rot.getStepX() * 2, 0,
                        facing.getStepZ() * 3 + rot.getStepZ() * 2),
                fluidPort(rot.getOpposite(), facing.getStepX() * 3 - rot.getStepX() * 2, 0,
                        facing.getStepZ() * 3 - rot.getStepZ() * 2),
                fluidPort(rot, -facing.getStepX() + rot.getStepX() * 2, 0,
                        -facing.getStepZ() + rot.getStepZ() * 2),
                fluidPort(rot.getOpposite(), -facing.getStepX() - rot.getStepX() * 2, 0,
                        -facing.getStepZ() - rot.getStepZ() * 2),
                fluidPort(Direction.UP, facing.getStepX() * 3, 3, facing.getStepZ() * 3),
                fluidPort(Direction.UP, -facing.getStepX(), 3, -facing.getStepZ()));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        return List.of(energyPort(facing.getOpposite(), -facing.getStepX() * 4, 1, -facing.getStepZ() * 4));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putDouble("spin", spin);
        tag.putLong("lastPowerTarget", lastPowerTarget);
        tag.putLong("flywheelEnergy", flywheelEnergy);
        tag.putLong("maxPowerTarget", maxPowerTarget);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        spin = Math.max(0.0D, tag.getDouble("spin"));
        lastPowerTarget = Math.max(0L, tag.getLong("lastPowerTarget"));
        flywheelEnergy = Math.max(0L, tag.getLong("flywheelEnergy"));
        maxPowerTarget = Math.max(0L, tag.getLong("maxPowerTarget"));
    }
}
