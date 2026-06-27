package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.fluid.LegacyFluidTankPacket;
import com.hbm.ntm.neutron.NeutronHandler;
import com.hbm.ntm.neutron.RBMKIOPlanner;
import com.hbm.ntm.neutron.RBMKThermalState;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RBMKSteamOutletBlockEntity extends HbmFluidNetworkBlockEntity implements HbmStandardFluidSender {
    private static final String LEGACY_TANK_KEY = "tank";
    private static final List<FluidPort> FLUID_PORTS = HbmFluidPortLayouts.allAdjacent();

    private final HbmFluidTank steamTank;

    public RBMKSteamOutletBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.SUPERHOTSTEAM, RBMKIOPlanner.OUTLET_STEAM_CAPACITY));
    }

    private RBMKSteamOutletBlockEntity(BlockPos pos, BlockState state, HbmFluidTank steamTank) {
        super(ModBlockEntities.RBMK_STEAM_OUTLET.get(), pos, state, List.of(steamTank));
        this.steamTank = steamTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RBMKSteamOutletBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }
        blockEntity.steamTank.setTankType(HbmFluids.SUPERHOTSTEAM);
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, blockEntity);

        boolean changed = blockEntity.pullSteamFromAdjacentColumns(level, pos);
        if (blockEntity.steamTank.getFill() > 0) {
            blockEntity.tryProvideFluidToPorts(HbmFluids.SUPERHOTSTEAM, blockEntity.steamTank.getPressure(), blockEntity);
        }
        if (changed) {
            blockEntity.setChanged();
        }
        blockEntity.networkPackNT(25);
    }

    public HbmFluidTank steamTank() {
        return steamTank;
    }

    private boolean pullSteamFromAdjacentColumns(Level level, BlockPos pos) {
        List<RBMKIOPlanner.ReaSimColumnFluidState> states = adjacentColumnStates(level, pos);
        RBMKIOPlanner.OutletTransferPlan plan = RBMKIOPlanner.planOutletTransfer(
                steamTank.getFill(), states, NeutronHandler.rbmkRuntimeSettings(level).reasimBoilers());
        int moved = 0;
        for (RBMKIOPlanner.ColumnTransfer transfer : plan.fromColumns()) {
            RBMKColumnBlockEntity column = columnAt(level, transfer.columnPos());
            if (column != null) {
                moved += column.extractReaSimSteam(transfer.amount());
            }
        }
        if (moved <= 0) {
            return false;
        }
        steamTank.fill(HbmFluids.SUPERHOTSTEAM, moved, steamTank.getPressure(), false);
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
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(steamTank);
    }

    @Override
    public boolean supportsFluidSettingsCopy() {
        return false;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == HbmFluids.SUPERHOTSTEAM && steamTank.getFill() > 0;
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
        return List.of();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(steamTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.OUTPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        steamTank.writeToNbt(tag, LEGACY_TANK_KEY);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(LEGACY_TANK_KEY)) {
            steamTank.readFromNbt(tag, LEGACY_TANK_KEY);
        }
        steamTank.setTankType(HbmFluids.SUPERHOTSTEAM);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        LegacyFluidTankPacket.write(data, steamTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        LegacyFluidTankPacket.read(data, steamTank);
    }
}
