package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
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
import com.hbm.ntm.menu.RemoteFluidMachineMenu;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Common 1.7.10-style tank and remote fluid port runtime for visible oil
 * machines whose full GUI/recipe systems are still being migrated.
 */
public abstract class LegacyRemoteFluidMachineBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver, HbmPersistentBlockState {
    private static final String TAG_INVENTORY = "Inventory";

    private final List<HbmFluidTank> receivingTanks;
    private final List<HbmFluidTank> sendingTanks;
    private final boolean rejectsDownConnections;
    @Nullable
    private final ItemStackHandler items;

    protected LegacyRemoteFluidMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            long maxPower, List<HbmFluidTank> allTanks, List<HbmFluidTank> receivingTanks,
            List<HbmFluidTank> sendingTanks, boolean rejectsDownConnections) {
        this(type, pos, state, maxPower, allTanks, receivingTanks, sendingTanks, rejectsDownConnections, 0);
    }

    protected LegacyRemoteFluidMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            long maxPower, List<HbmFluidTank> allTanks, List<HbmFluidTank> receivingTanks,
            List<HbmFluidTank> sendingTanks, boolean rejectsDownConnections, int itemSlotCount) {
        super(type, pos, state, new HbmEnergyStorage(maxPower, maxPower, 0L), allTanks);
        this.receivingTanks = List.copyOf(receivingTanks);
        this.sendingTanks = List.copyOf(sendingTanks);
        this.rejectsDownConnections = rejectsDownConnections;
        this.items = itemSlotCount > 0 ? new ItemStackHandler(itemSlotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return LegacyRemoteFluidMachineBlockEntity.this.isItemValid(slot, stack);
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
            }
        } : null;
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

    protected boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }

    public boolean canSetInputTypeWithIdentifier() {
        return false;
    }

    public boolean setInputTypeFromIdentifier(FluidType type) {
        return false;
    }

    @Nullable
    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return items == null ? List.of() : HbmInventoryMenuHelper.clearToDrops(items);
    }

    public ItemStack createPersistentBlockDrop(Item item) {
        ItemStack stack = new ItemStack(item);
        writePersistentStateToStack(stack);
        return stack;
    }

    public LegacyGuiProfile getLegacyGuiProfile() {
        return LegacyGuiProfile.NONE;
    }

    public boolean hasLegacyGui() {
        return getLegacyGuiProfile() != LegacyGuiProfile.NONE;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return hasLegacyGui() ? new RemoteFluidMachineMenu(containerId, playerInventory, this) : null;
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
        if (side == null) {
            return HbmFluidSideMode.BOTH;
        }
        return rejectsDownConnections && side == Direction.DOWN ? HbmFluidSideMode.NONE : HbmFluidSideMode.BOTH;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return getMaxPower() > 0L ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return type != null && type != HbmFluids.NONE && side != null
                && (!rejectsDownConnections || side != Direction.DOWN);
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

    @Override
    public void writePersistentState(CompoundTag persistent) {
        HbmPersistentBlockState.writeIndexedTanks(persistent, getAllTanks());
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        HbmPersistentBlockState.readIndexedTanks(persistent, getAllTanks());
        invalidateFluidHandlers();
        setChanged();
        if (level != null) {
            refreshFluidPorts();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
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

    protected boolean consumePower(long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (energy.getPower() < amount) {
            return false;
        }
        energy.setPower(energy.getPower() - amount);
        return true;
    }

    protected void chargeFromSlot(int slot) {
        if (items != null && slot >= 0 && slot < items.getSlots()) {
            HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(slot), energy, energy.getReceiverSpeed());
        }
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

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (items != null) {
            tag.put(TAG_INVENTORY, HbmInventoryMenuHelper.saveLegacyItems(items));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (items != null) {
            HbmInventoryMenuHelper.loadLegacyItems(tag.getCompound(TAG_INVENTORY), items);
        }
    }

    public enum LegacyGuiProfile {
        NONE(176, 166, 84, 142, false),
        COKER(176, 204, 122, 180, false),
        HYDROTREATER(176, 238, 156, 214, true),
        CATALYTIC_REFORMER(176, 238, 156, 214, true),
        VACUUM_DISTILL(176, 238, 156, 214, true);

        private final int width;
        private final int height;
        private final int inventoryY;
        private final int hotbarY;
        private final boolean energyBar;

        LegacyGuiProfile(int width, int height, int inventoryY, int hotbarY, boolean energyBar) {
            this.width = width;
            this.height = height;
            this.inventoryY = inventoryY;
            this.hotbarY = hotbarY;
            this.energyBar = energyBar;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public int inventoryY() {
            return inventoryY;
        }

        public int hotbarY() {
            return hotbarY;
        }

        public boolean hasEnergyBar() {
            return energyBar;
        }
    }
}
