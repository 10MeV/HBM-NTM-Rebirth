package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SolarBoilerBlockEntity extends HbmFluidNetworkBlockEntity
        implements HbmStandardFluidReceiver, HbmStandardFluidSender {
    private static final int WATER_CAPACITY = 100;
    private static final int STEAM_CAPACITY = 10_000;
    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(0, 3, 0, Direction.UP),
            FluidPort.of(0, -1, 0, Direction.DOWN));

    private final HbmFluidTank waterTank;
    private final HbmFluidTank steamTank;
    private int display;
    private int heat;
    private final Set<BlockPos> primaryBeamTargets = new LinkedHashSet<>();
    private final Set<BlockPos> secondaryBeamTargets = new LinkedHashSet<>();

    public SolarBoilerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.WATER, WATER_CAPACITY),
                new HbmFluidTank(HbmFluids.STEAM, STEAM_CAPACITY));
    }

    private SolarBoilerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank waterTank, HbmFluidTank steamTank) {
        super(ModBlockEntities.SOLAR_BOILER.get(), pos, state, List.of(waterTank, steamTank));
        this.waterTank = waterTank;
        this.steamTank = steamTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarBoilerBlockEntity boiler) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, boiler);
        boiler.waterTank.setTankType(HbmFluids.WATER);
        boiler.steamTank.setTankType(HbmFluids.STEAM);

        int process = Math.max(0, boiler.heat / 50);
        boiler.display = process;
        process = Math.min(process, boiler.waterTank.getFill());
        process = Math.min(process, boiler.steamTank.getSpace() / 100);
        if (process > 0) {
            boiler.waterTank.drain(process, false);
            boiler.steamTank.fill(HbmFluids.STEAM, process * 100, 0, false);
            boiler.onFluidContentsChanged();
        }
        if (boiler.steamTank.getFill() > 0) {
            boiler.tryProvideFluidToPorts(HbmFluids.STEAM, boiler.steamTank.getPressure(), boiler);
        }
        boiler.heat = 0;

        if (process > 0 || level.getGameTime() % 20L == 0L) {
            boiler.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        boiler.networkPackNT(15);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SolarBoilerBlockEntity boiler) {
        if (!level.isClientSide) {
            return;
        }
        boiler.secondaryBeamTargets.clear();
        boiler.secondaryBeamTargets.addAll(boiler.primaryBeamTargets);
        boiler.primaryBeamTargets.clear();
    }

    public HbmFluidTank getWaterTank() {
        return waterTank;
    }

    public HbmFluidTank getSteamTank() {
        return steamTank;
    }

    public int getDisplay() {
        return display;
    }

    public int getHeat() {
        return heat;
    }

    public void registerSolarMirrorBeam(BlockPos mirrorPos) {
        primaryBeamTargets.add(mirrorPos.immutable());
    }

    public Set<BlockPos> getSolarMirrorBeamTargets() {
        return Collections.unmodifiableSet(secondaryBeamTargets);
    }

    @Nullable
    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return null;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<Component> lines = new ArrayList<>();
        lines.add(LegacyLookOverlayLines.compactTank(true, waterTank));
        lines.add(LegacyLookOverlayLines.compactTank(false, steamTank));
        if (display < 1) {
            lines.add(Component.literal("Too cold!")
                    .withStyle((level.getGameTime() / 10L) % 2L == 0L ? ChatFormatting.RED : ChatFormatting.YELLOW));
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    public void addHeat(int heat) {
        this.heat = Math.max(0, this.heat + Math.max(0, heat));
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(waterTank);
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
        return type == waterTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == steamTank.getTankType() && steamTank.getFill() > 0;
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
        return List.of(waterTank);
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
        tag.putInt("display", display);
        tag.putInt("heat", heat);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        display = Math.max(0, tag.getInt("display"));
        heat = Math.max(0, tag.getInt("heat"));
    }
}
