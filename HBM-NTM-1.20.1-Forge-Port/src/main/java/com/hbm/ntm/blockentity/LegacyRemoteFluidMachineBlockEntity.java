package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Common 1.7.10-style tank and remote fluid port runtime for visible oil
 * machines whose full GUI/recipe systems are still being migrated.
 */
public abstract class LegacyRemoteFluidMachineBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidTransceiver {
    private final List<HbmFluidTank> receivingTanks;
    private final List<HbmFluidTank> sendingTanks;
    private final boolean rejectsDownConnections;

    protected LegacyRemoteFluidMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            long maxPower, List<HbmFluidTank> allTanks, List<HbmFluidTank> receivingTanks,
            List<HbmFluidTank> sendingTanks, boolean rejectsDownConnections) {
        super(type, pos, state, new HbmEnergyStorage(maxPower, maxPower, 0L), allTanks);
        this.receivingTanks = List.copyOf(receivingTanks);
        this.sendingTanks = List.copyOf(sendingTanks);
        this.rejectsDownConnections = rejectsDownConnections;
    }

    public static <T extends LegacyRemoteFluidMachineBlockEntity> void serverTick(Level level, BlockPos pos,
            BlockState state, T blockEntity) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, blockEntity);
        blockEntity.refreshEnergyPorts();
        boolean changed = blockEntity.tickLegacyMachine(level, pos, state);
        blockEntity.refreshFluidPorts();
        if (changed || level.getGameTime() % 20L == 0L) {
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    protected boolean tickLegacyMachine(Level level, BlockPos pos, BlockState state) {
        return false;
    }

    protected void refreshFluidPorts() {
        HbmFluidPortMachine.refreshTransceiverPorts(level, worldPosition, getFluidPorts(),
                receivingTanks, sendingTanks, this);
    }

    protected void refreshEnergyPorts() {
        if (level != null && !level.isClientSide && getMaxPower() > 0L) {
            HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, getEnergyPorts(), energy);
        }
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return receivingTanks;
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return sendingTanks;
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
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return receivingTanks;
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return sendingTanks;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null ? HbmFluidSideMode.BOTH : HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return getMaxPower() > 0L ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return type != null && type != HbmFluids.NONE && side != null
                && (!rejectsDownConnections || side != Direction.DOWN)
                && acceptsOrProvides(type);
    }

    private boolean acceptsOrProvides(FluidType type) {
        for (HbmFluidTank tank : getAllTanks()) {
            if (tank.getTankType() == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    protected Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    protected Direction rotatedFacing() {
        return LegacyMultiblockOffsets.legacyUpSide(facing());
    }

    protected static HbmFluidTank tank(FluidType type, int capacity) {
        HbmFluidTank tank = new HbmFluidTank(type, capacity);
        tank.conform(new HbmFluidStack(type, 0));
        return tank;
    }

    protected static HbmFluidTank tank(FluidType type, int capacity, int pressure) {
        HbmFluidTank tank = tank(type, capacity);
        tank.withPressure(pressure);
        return tank;
    }

    protected static void configureTank(HbmFluidTank tank, FluidType type) {
        FluidType next = type == null ? HbmFluids.NONE : type;
        if (tank.getTankType() != next) {
            tank.setTankType(next);
        }
    }

    protected static boolean hasSpace(HbmFluidTank tank, int amount) {
        return amount <= 0 || tank.getFill() + amount <= tank.getMaxFill();
    }

    protected static boolean addFluid(HbmFluidTank tank, FluidType type, int amount) {
        if (amount <= 0 || type == HbmFluids.NONE) {
            return true;
        }
        if (tank.getTankType() != type) {
            tank.setTankType(type);
        }
        if (!hasSpace(tank, amount)) {
            return false;
        }
        tank.setFill(tank.getFill() + amount);
        return true;
    }

    protected List<FluidPort> fixedSurroundingPorts() {
        return List.of(
                FluidPort.of(2, 0, 1, Direction.EAST),
                FluidPort.of(2, 0, -1, Direction.EAST),
                FluidPort.of(-2, 0, 1, Direction.WEST),
                FluidPort.of(-2, 0, -1, Direction.WEST),
                FluidPort.of(1, 0, 2, Direction.SOUTH),
                FluidPort.of(-1, 0, 2, Direction.SOUTH),
                FluidPort.of(1, 0, -2, Direction.NORTH),
                FluidPort.of(-1, 0, -2, Direction.NORTH));
    }

    protected List<FluidPort> portsFromOffsets(List<BlockPos> offsets) {
        List<FluidPort> ports = new ArrayList<>();
        for (BlockPos offset : offsets) {
            Direction direction = dominantHorizontalDirection(offset);
            ports.add(new FluidPort(offset, direction));
        }
        return List.copyOf(ports);
    }

    protected List<EnergyPort> energyPortsFromOffsets(List<BlockPos> offsets) {
        List<EnergyPort> ports = new ArrayList<>();
        for (BlockPos offset : offsets) {
            Direction direction = dominantHorizontalDirection(offset);
            ports.add(new EnergyPort(offset, direction));
        }
        return List.copyOf(ports);
    }

    private static Direction dominantHorizontalDirection(BlockPos offset) {
        if (Math.abs(offset.getX()) >= Math.abs(offset.getZ())) {
            return offset.getX() >= 0 ? Direction.EAST : Direction.WEST;
        }
        return offset.getZ() >= 0 ? Direction.SOUTH : Direction.NORTH;
    }
}
