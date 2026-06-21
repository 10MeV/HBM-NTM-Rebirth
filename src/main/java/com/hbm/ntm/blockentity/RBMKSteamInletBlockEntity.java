package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.RBMKIOPlanner;
import com.hbm.ntm.neutron.RBMKThermalState;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RBMKSteamInletBlockEntity extends HbmFluidNetworkBlockEntity implements HbmStandardFluidReceiver {
    private static final String LEGACY_TANK_KEY = "tank";
    private static final List<FluidPort> FLUID_PORTS = HbmFluidPortLayouts.allAdjacent();

    private final HbmFluidTank waterTank;

    public RBMKSteamInletBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.WATER, RBMKIOPlanner.INLET_WATER_CAPACITY));
    }

    private RBMKSteamInletBlockEntity(BlockPos pos, BlockState state, HbmFluidTank waterTank) {
        super(ModBlockEntities.RBMK_STEAM_INLET.get(), pos, state, List.of(waterTank));
        this.waterTank = waterTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKSteamInletBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        blockEntity.waterTank.setTankType(HbmFluids.WATER);
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, blockEntity);

        boolean changed = blockEntity.transferWaterToAdjacentColumns(level, pos);
        if (changed || level.getGameTime() % 20L == 0L) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        blockEntity.networkPackNT(25);
    }

    public HbmFluidTank waterTank() {
        return waterTank;
    }

    private boolean transferWaterToAdjacentColumns(Level level, BlockPos pos) {
        List<RBMKIOPlanner.ReaSimColumnFluidState> states = adjacentColumnStates(level, pos);
        RBMKIOPlanner.InletTransferPlan plan = RBMKIOPlanner.planInletTransfer(
                waterTank.getFill(), states, NeutronHandler.rbmkRuntimeSettings(level).reasimBoilers());
        int moved = 0;
        for (RBMKIOPlanner.ColumnTransfer transfer : plan.toColumns()) {
            RBMKColumnBlockEntity column = columnAt(level, transfer.columnPos());
            if (column != null) {
                moved += column.receiveReaSimWater(transfer.amount());
            }
        }
        if (moved <= 0) {
            return false;
        }
        waterTank.drain(moved, false);
        onFluidContentsChanged();
        return true;
    }

    private static List<RBMKIOPlanner.ReaSimColumnFluidState> adjacentColumnStates(Level level, BlockPos pos) {
        List<RBMKIOPlanner.ReaSimColumnFluidState> columns = new ArrayList<>();
        for (BlockPos neighbor : RBMKIOPlanner.horizontalNeighborPositions(pos)) {
            RBMKColumnBlockEntity column = columnAt(level, neighbor);
            if (column != null) {
                columns.add(new RBMKIOPlanner.ReaSimColumnFluidState(column.getBlockPos(),
                        column.reasimWater(), RBMKThermalState.MAX_WATER, column.reasimSteam()));
            }
        }
        return columns;
    }

    @Nullable
    private static RBMKColumnBlockEntity columnAt(Level level, BlockPos pos) {
        return RBMKColumnBlockEntity.resolveOperationalColumn(level, pos);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(waterTank);
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
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == HbmFluids.WATER;
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
        return List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        waterTank.writeToNbt(tag, LEGACY_TANK_KEY);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(LEGACY_TANK_KEY)) {
            waterTank.readFromNbt(tag, LEGACY_TANK_KEY);
        }
        waterTank.setTankType(HbmFluids.WATER);
    }
}
