package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DeuteriumExtractorBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidTransceiver {
    private static final long MAX_POWER = 10_000L;
    private static final long POWER_PER_OPERATION = MAX_POWER / 20L;
    private static final int WATER_CAPACITY = 1_000;
    private static final int HEAVY_WATER_CAPACITY = 100;
    private static final List<FluidPort> ALL_FLUID_PORTS = Arrays.stream(Direction.values())
            .map(direction -> FluidPort.of(direction.getStepX(), direction.getStepY(), direction.getStepZ(),
                    direction))
            .toList();
    private static final List<EnergyPort> ALL_ENERGY_PORTS = ALL_FLUID_PORTS.stream()
            .map(port -> new EnergyPort(port.offset(), port.direction()))
            .toList();

    private final HbmFluidTank waterTank;
    private final HbmFluidTank heavyWaterTank;

    public DeuteriumExtractorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                new HbmFluidTank(HbmFluids.WATER, WATER_CAPACITY),
                new HbmFluidTank(HbmFluids.HEAVYWATER, HEAVY_WATER_CAPACITY));
    }

    private DeuteriumExtractorBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy,
            HbmFluidTank waterTank, HbmFluidTank heavyWaterTank) {
        super(ModBlockEntities.DEUTERIUM_EXTRACTOR.get(), pos, state, energy, List.of(waterTank, heavyWaterTank));
        this.waterTank = waterTank;
        this.heavyWaterTank = heavyWaterTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeuteriumExtractorBlockEntity extractor) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, extractor);
        extractor.waterTank.setTankType(HbmFluids.WATER);
        extractor.heavyWaterTank.setTankType(HbmFluids.HEAVYWATER);

        long oldPower = extractor.getPower();
        int oldWater = extractor.waterTank.getFill();
        int oldHeavyWater = extractor.heavyWaterTank.getFill();

        extractor.tryProcess();
        if (extractor.heavyWaterTank.getFill() > 0) {
            extractor.tryProvideFluidToPorts(extractor.heavyWaterTank.getTankType(),
                    extractor.heavyWaterTank.getPressure(), extractor);
        }

        boolean changed = oldPower != extractor.getPower()
                || oldWater != extractor.waterTank.getFill()
                || oldHeavyWater != extractor.heavyWaterTank.getFill();
        extractor.networkPackNT(50);
        if (changed) {
            extractor.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void tryProcess() {
        if (getPower() < POWER_PER_OPERATION
                || waterTank.getFill() < 100
                || heavyWaterTank.getFill() >= heavyWaterTank.getMaxFill()) {
            return;
        }
        int convert = Math.min(heavyWaterTank.getMaxFill(), waterTank.getFill()) / 50;
        convert = Math.min(convert, heavyWaterTank.getMaxFill() - heavyWaterTank.getFill());
        if (convert <= 0) {
            return;
        }
        waterTank.setFill(waterTank.getFill() - convert * 50);
        heavyWaterTank.setFill(heavyWaterTank.getFill() + convert);
        setPower(getPower() - POWER_PER_OPERATION);
        onFluidContentsChanged();
    }

    public HbmFluidTank getWaterTank() {
        return waterTank;
    }

    public HbmFluidTank getHeavyWaterTank() {
        return heavyWaterTank;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        ChatFormatting powerColor = getPower() < POWER_PER_OPERATION ? ChatFormatting.RED : ChatFormatting.GREEN;
        return LegacyLookOverlay.forBlock(this, List.of(
                Component.literal("Power: " + LegacyLookOverlayLines.shortNumber(getPower()) + "HE")
                        .withStyle(powerColor),
                LegacyLookOverlayLines.compactTank(true, waterTank),
                LegacyLookOverlayLines.compactTank(false, heavyWaterTank)));
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(waterTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(heavyWaterTank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidTransceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == HbmFluids.WATER;
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == HbmFluids.HEAVYWATER && heavyWaterTank.getFill() > 0;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return ALL_FLUID_PORTS;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return ALL_ENERGY_PORTS;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(waterTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(heavyWaterTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("power", getPower());
        waterTank.writeToNbt(tag, "water");
        heavyWaterTank.writeToNbt(tag, "heavyWater");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("power")) {
            setPower(tag.getLong("power"));
        }
        if (tag.contains("water") || tag.contains("water_type") || tag.contains("water_type_id")) {
            waterTank.readFromNbt(tag, "water");
        }
        if (tag.contains("heavyWater") || tag.contains("heavyWater_type") || tag.contains("heavyWater_type_id")) {
            heavyWaterTank.readFromNbt(tag, "heavyWater");
        }
        waterTank.setTankType(HbmFluids.WATER);
        heavyWaterTank.setTankType(HbmFluids.HEAVYWATER);
    }
}
