package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
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
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SteamEngineBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidReceiver, HbmStandardFluidSender {
    private static final long MAX_POWER = 1_000_000L;
    private static final int STEAM_CAPACITY = 2_000;
    private static final int SPENT_STEAM_CAPACITY = 20;
    private static final double EFFICIENCY = 0.85D;

    private final HbmFluidTank steamTank;
    private final HbmFluidTank spentSteamTank;
    private float rotor;
    private float acceleration;
    private long lastPowerProduced;

    public SteamEngineBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER),
                new HbmFluidTank(HbmFluids.STEAM, STEAM_CAPACITY),
                new HbmFluidTank(HbmFluids.SPENTSTEAM, SPENT_STEAM_CAPACITY));
    }

    private SteamEngineBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy,
            HbmFluidTank steamTank, HbmFluidTank spentSteamTank) {
        super(ModBlockEntities.STEAM_ENGINE.get(), pos, state, energy, List.of(steamTank, spentSteamTank));
        this.steamTank = steamTank;
        this.spentSteamTank = spentSteamTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SteamEngineBlockEntity engine) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, engine);
        engine.steamTank.setTankType(HbmFluids.STEAM);
        engine.spentSteamTank.setTankType(HbmFluids.SPENTSTEAM);
        engine.energy.setPower(0L);

        TurbineResult result = engine.runConversion();
        engine.lastPowerProduced = result.powerProduced();
        if (result.powerProduced() > 0L) {
            engine.energy.setPower(Math.min(engine.energy.getMaxPower(), engine.energy.getPower() + result.powerProduced()));
            engine.acceleration = Math.min(engine.acceleration + 0.1F, 40.0F);
        } else {
            engine.acceleration = Math.max(engine.acceleration - 0.1F, 0.0F);
        }
        engine.rotor += engine.acceleration;
        if (engine.rotor >= 360.0F) {
            engine.rotor -= 360.0F;
        }

        if (engine.energy.getPower() > 0L) {
            engine.tryProvideEnergyToPorts();
        }
        if (engine.spentSteamTank.getFill() > 0) {
            engine.tryProvideFluidToPorts(engine.spentSteamTank.getTankType(), engine.spentSteamTank.getPressure(), engine);
        }

        if (result.converted() || level.getGameTime() % 20L == 0L) {
            engine.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private TurbineResult runConversion() {
        return HbmTurbineConversion.run(steamTank, spentSteamTank, EFFICIENCY, Integer.MAX_VALUE, false);
    }

    public HbmFluidTank getSteamTank() {
        return steamTank;
    }

    public HbmFluidTank getSpentSteamTank() {
        return spentSteamTank;
    }

    public float getRotor() {
        return rotor;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public long getLastPowerProduced() {
        return lastPowerProduced;
    }

    @Nullable
    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return null;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.tank(true, steamTank),
                LegacyLookOverlayLines.tank(false, spentSteamTank)));
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(steamTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(spentSteamTank);
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
        return type == steamTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == spentSteamTank.getTankType() && spentSteamTank.getFill() > 0;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                FluidPort.of(rot.getStepX(), 1, rot.getStepZ(), rot),
                FluidPort.of(rot.getStepX() + facing.getStepX(), 1,
                        rot.getStepZ() + facing.getStepZ(), rot),
                FluidPort.of(rot.getStepX() - facing.getStepX(), 1,
                        rot.getStepZ() - facing.getStepZ(), rot));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                EnergyPort.of(rot.getStepX(), 1, rot.getStepZ(), rot),
                EnergyPort.of(rot.getStepX() + facing.getStepX(), 1,
                        rot.getStepZ() + facing.getStepZ(), rot),
                EnergyPort.of(rot.getStepX() - facing.getStepX(), 1,
                        rot.getStepZ() - facing.getStepZ(), rot));
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(steamTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(spentSteamTank);
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
        tag.putFloat("rotor", rotor);
        tag.putFloat("acceleration", acceleration);
        tag.putLong("lastPowerProduced", lastPowerProduced);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rotor = tag.getFloat("rotor");
        acceleration = Math.max(0.0F, tag.getFloat("acceleration"));
        lastPowerProduced = Math.max(0L, tag.getLong("lastPowerProduced"));
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }
}
