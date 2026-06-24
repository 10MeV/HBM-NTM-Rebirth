package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.menu.FluidPumpMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidPumpBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmLegacyControlReceiver {
    private static final int DEFAULT_BUFFER_SIZE = 100;
    private static final int MAX_BUFFER_SIZE = 10_000;
    private static final String TAG_TANK = "t";
    private static final String TAG_PRIORITY = "p";
    private static final String TAG_BUFFER = "buffer";
    private static final String TAG_REDSTONE = "redstone";

    private final HbmFluidTank tank;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private HbmEnergyReceiver.ConnectionPriority priority = HbmEnergyReceiver.ConnectionPriority.NORMAL;
    private boolean redstone;

    public FluidPumpBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, DEFAULT_BUFFER_SIZE));
    }

    private FluidPumpBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.FLUID_PUMP.get(), pos, state, List.of(tank));
        this.tank = tank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidPumpBlockEntity pump) {
        if (level.isClientSide) {
            return;
        }

        boolean changed = pump.normalizeBuffer();
        boolean powered = level.hasNeighborSignal(pos);
        if (pump.redstone != powered) {
            pump.redstone = powered;
            pump.invalidateFluidHandlers();
            level.updateNeighborsAt(pos, state.getBlock());
            changed = true;
        }

        pump.refreshPumpPorts();

        if (changed || level.getGameTime() % 15L == 0L) {
            pump.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        pump.networkPackNT(15);
    }

    public HbmFluidTank getTank() {
        return tank;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getPressure() {
        return tank.getPressure();
    }

    public HbmEnergyReceiver.ConnectionPriority getPriority() {
        return priority;
    }

    public boolean isRedstoneBlocked() {
        return redstone;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        java.util.ArrayList<net.minecraft.network.chat.Component> lines = new java.util.ArrayList<>();
        lines.add(LegacyLookOverlayLines.pumpLine(tank, bufferSize));
        lines.add(LegacyLookOverlayLines.priority(priority));
        if (tank.getFill() > 0) {
            lines.add(LegacyLookOverlayLines.buffered(tank.getFill()));
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    public boolean setIdentifiedType(FluidType type) {
        FluidType next = type == null ? HbmFluids.NONE : type;
        if (tank.getTankType() == next) {
            return false;
        }
        tank.setTankType(next);
        onFluidContentsChanged();
        return true;
    }

    public void setBufferSize(int bufferSize) {
        int clamped = Math.max(0, Math.min(MAX_BUFFER_SIZE, bufferSize));
        if (this.bufferSize != clamped) {
            this.bufferSize = clamped;
            normalizeBuffer();
            onFluidContentsChanged();
        }
    }

    public void setPressure(int pressure) {
        int previous = tank.getPressure();
        tank.withPressure(pressure);
        if (previous != tank.getPressure()) {
            onFluidContentsChanged();
        }
    }

    public void setPriority(HbmEnergyReceiver.ConnectionPriority priority) {
        HbmEnergyReceiver.ConnectionPriority next = priority == null
                ? HbmEnergyReceiver.ConnectionPriority.NORMAL
                : priority;
        if (this.priority != next) {
            this.priority = next;
            setChanged();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FluidPumpMenu(containerId, inventory, this);
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D,
                worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 128.0D;
    }

    @Override
    public void receiveControl(CompoundTag data) {
        if (data.contains("capacity")) {
            setBufferSize(data.getInt("capacity"));
        }
        if (data.contains("pressure")) {
            setPressure(HbmFluidTank.clampPressure(data.getByte("pressure")));
        }
        if (data.contains("priority")) {
            HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
            int ordinal = data.getByte("priority");
            setPriority(ordinal >= 0 && ordinal < values.length
                    ? values[ordinal]
                    : HbmEnergyReceiver.ConnectionPriority.NORMAL);
        }
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return bufferSize < tank.getFill() ? List.of() : List.of(tank);
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return bufferSize;
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return redstone ? List.of() : List.of(tank);
    }

    @Override
    public long getProviderSpeed(FluidType type, int pressure) {
        return bufferSize;
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (amount <= 0L || bufferSize < tank.getFill() || !tank.canAccept(type, pressure)) {
            return amount;
        }
        int accepted = tank.fill(type, (int) Math.min(Integer.MAX_VALUE, amount), pressure, false);
        if (accepted > 0) {
            onFluidContentsChanged();
        }
        return amount - accepted;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        long before = tank.getFill();
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (tank.getFill() != before) {
            onFluidContentsChanged();
        }
    }

    @Override
    public HbmEnergyReceiver.ConnectionPriority getFluidPriority() {
        return priority;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null
                && type != null
                && type != HbmFluids.NONE
                && type == tank.getTankType()
                && (side == inputSide() || side == outputSide());
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        if (side == null) {
            return HbmFluidSideMode.BOTH;
        }
        if (side == inputSide()) {
            return HbmFluidSideMode.INPUT;
        }
        if (!redstone && side == outputSide()) {
            return HbmFluidSideMode.OUTPUT;
        }
        return HbmFluidSideMode.NONE;
    }

    @Override
    protected int getInputPressure(@Nullable Direction side) {
        return tank.getPressure();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tank.writeToNbt(tag, TAG_TANK);
        tag.putByte(TAG_PRIORITY, (byte) priority.ordinal());
        tag.putInt(TAG_BUFFER, bufferSize);
        tag.putBoolean(TAG_REDSTONE, redstone);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (hasTankTag(tag, TAG_TANK)) {
            tank.readFromNbt(tag, TAG_TANK);
        }
        if (tag.contains(TAG_PRIORITY)) {
            HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
            int ordinal = tag.getByte(TAG_PRIORITY);
            priority = ordinal >= 0 && ordinal < values.length
                    ? values[ordinal]
                    : HbmEnergyReceiver.ConnectionPriority.NORMAL;
        }
        if (tag.contains(TAG_BUFFER)) {
            bufferSize = Math.max(0, Math.min(MAX_BUFFER_SIZE, tag.getInt(TAG_BUFFER)));
        }
        redstone = tag.getBoolean(TAG_REDSTONE);
        normalizeBuffer();
        invalidateFluidHandlers();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void refreshPumpPorts() {
        if (level == null || level.isClientSide || tank.getTankType() == HbmFluids.NONE) {
            return;
        }
        HbmFluidPortMachine.refreshReceiverPorts(level, worldPosition, List.of(port(inputSide())),
                getReceivingTanks(), this);
        if (!redstone && tank.getFill() > 0) {
            HbmFluidPortMachine.refreshProviderPorts(level, worldPosition, List.of(port(outputSide())),
                    getSendingTanks(), this);
        }
    }

    private boolean normalizeBuffer() {
        if (bufferSize == tank.getMaxFill()) {
            return false;
        }
        int nextBuffer = Math.max(tank.getFill(), bufferSize);
        tank.changeTankSize(nextBuffer);
        return true;
    }

    private FluidPort port(Direction direction) {
        return new FluidPort(new BlockPos(direction.getStepX(), direction.getStepY(), direction.getStepZ()), direction);
    }

    private Direction inputSide() {
        return LegacyMultiblockOffsets.legacyUpSide(facing());
    }

    private Direction outputSide() {
        return inputSide().getOpposite();
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }
}
