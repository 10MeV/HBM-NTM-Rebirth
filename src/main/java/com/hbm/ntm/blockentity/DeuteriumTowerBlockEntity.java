package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.registry.ModBlockEntities;
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

public class DeuteriumTowerBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidTransceiver {
    private static final long MAX_POWER = 100_000L;
    private static final long POWER_PER_OPERATION = MAX_POWER / 20L;
    private static final int WATER_CAPACITY = 50_000;
    private static final int HEAVY_WATER_CAPACITY = 5_000;

    private final HbmFluidTank waterTank;
    private final HbmFluidTank heavyWaterTank;

    public DeuteriumTowerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                new HbmFluidTank(HbmFluids.WATER, WATER_CAPACITY),
                new HbmFluidTank(HbmFluids.HEAVYWATER, HEAVY_WATER_CAPACITY));
    }

    private DeuteriumTowerBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy,
            HbmFluidTank waterTank, HbmFluidTank heavyWaterTank) {
        super(ModBlockEntities.DEUTERIUM_TOWER.get(), pos, state, energy, List.of(waterTank, heavyWaterTank));
        this.waterTank = waterTank;
        this.heavyWaterTank = heavyWaterTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeuteriumTowerBlockEntity tower) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, tower);
        tower.waterTank.setTankType(HbmFluids.WATER);
        tower.heavyWaterTank.setTankType(HbmFluids.HEAVYWATER);

        long oldPower = tower.getPower();
        int oldWater = tower.waterTank.getFill();
        int oldHeavyWater = tower.heavyWaterTank.getFill();

        tower.tryProcess();
        if (tower.heavyWaterTank.getFill() > 0) {
            tower.tryProvideFluidToPorts(tower.heavyWaterTank.getTankType(), tower.heavyWaterTank.getPressure(), tower);
        }

        boolean changed = oldPower != tower.getPower()
                || oldWater != tower.waterTank.getFill()
                || oldHeavyWater != tower.heavyWaterTank.getFill();
        tower.networkPackNT(50);
        if (changed) {
            tower.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void tryProcess() {
        if (!hasPower() || !hasEnoughWater() || heavyWaterTank.getFill() >= heavyWaterTank.getMaxFill()) {
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

    private boolean hasPower() {
        return getPower() >= POWER_PER_OPERATION;
    }

    private boolean hasEnoughWater() {
        return waterTank.getFill() >= 100;
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
        return towerFluidPorts();
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return towerFluidPorts().stream()
                .map(port -> new EnergyPort(port.offset(), port.direction()))
                .toList();
    }

    private List<FluidPort> towerFluidPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyDownSide(facing);
        return List.of(
                FluidPort.of(-facing.getStepX() * 2, 0, -facing.getStepZ() * 2, facing.getOpposite()),
                FluidPort.of(-facing.getStepX() * 2 + rot.getStepX(), 0,
                        -facing.getStepZ() * 2 + rot.getStepZ(), facing.getOpposite()),
                FluidPort.of(facing.getStepX(), 0, facing.getStepZ(), facing),
                FluidPort.of(facing.getStepX() + rot.getStepX(), 0,
                        facing.getStepZ() + rot.getStepZ(), facing),
                FluidPort.of(-rot.getStepX(), 0, -rot.getStepZ(), rot.getOpposite()),
                FluidPort.of(-facing.getStepX() - rot.getStepX(), 0,
                        -facing.getStepZ() - rot.getStepZ(), rot.getOpposite()),
                FluidPort.of(rot.getStepX() * 2, 0, rot.getStepZ() * 2, rot),
                FluidPort.of(-facing.getStepX() + rot.getStepX() * 2, 0,
                        -facing.getStepZ() + rot.getStepZ() * 2, rot));
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
        return side == null || side.getAxis().isHorizontal() ? HbmFluidSideMode.BOTH : HbmFluidSideMode.NONE;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == null || side.getAxis().isHorizontal() ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
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

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
    }
}
