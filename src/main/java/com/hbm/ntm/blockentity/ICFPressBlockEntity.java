package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.item.ICFPelletItem;
import com.hbm.ntm.item.ICFPelletItem.FuelType;
import com.hbm.ntm.menu.ICFPressMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ICFPressBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, HbmStandardFluidTransceiver {
    public static final int SLOT_EMPTY = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_MUON = 2;
    public static final int SLOT_MUON_OUT = 3;
    public static final int SLOT_FUEL_1 = 4;
    public static final int SLOT_FUEL_2 = 5;
    public static final int SLOT_FLUID_ID_1 = 6;
    public static final int SLOT_FLUID_ID_2 = 7;
    public static final int SLOT_COUNT = 8;
    public static final int TANK_CAPACITY = 16_000;
    public static final int MAX_MUON = 16;
    private static final int[] TOP_BOTTOM_SLOTS = {SLOT_EMPTY, SLOT_OUTPUT, SLOT_MUON, SLOT_MUON_OUT, SLOT_FUEL_1};
    private static final int[] SIDE_SLOTS = {SLOT_EMPTY, SLOT_OUTPUT, SLOT_MUON, SLOT_MUON_OUT, SLOT_FUEL_2};
    private static final int[] UNSIDED_SLOTS = {
            SLOT_EMPTY, SLOT_OUTPUT, SLOT_MUON, SLOT_MUON_OUT, SLOT_FUEL_1, SLOT_FUEL_2};

    private final HbmFluidTank deuteriumTank;
    private final HbmFluidTank tritiumTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_EMPTY -> stack.is(ModItems.ICF_PELLET_EMPTY.get());
                case SLOT_MUON -> stack.is(ModItems.PARTICLE_MUON.get());
                case SLOT_FUEL_1, SLOT_FUEL_2 -> !stack.isEmpty();
                case SLOT_FLUID_ID_1, SLOT_FLUID_ID_2 -> stack.getItem() instanceof IFluidIdentifierItem;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> topBottomItemHandler =
            LazyOptional.of(() -> new AccessibleItemHandler(TOP_BOTTOM_SLOTS));
    private final LazyOptional<IItemHandler> sideItemHandler =
            LazyOptional.of(() -> new AccessibleItemHandler(SIDE_SLOTS));
    private final LazyOptional<IItemHandler> nullSideItemHandler =
            LazyOptional.of(() -> new AccessibleItemHandler(UNSIDED_SLOTS));
    private int muon;

    public ICFPressBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.DEUTERIUM, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.TRITIUM, TANK_CAPACITY));
    }

    private ICFPressBlockEntity(BlockPos pos, BlockState state, HbmFluidTank deuteriumTank,
            HbmFluidTank tritiumTank) {
        super(ModBlockEntities.ICF_PRESS.get(), pos, state, List.of(deuteriumTank, tritiumTank));
        this.deuteriumTank = deuteriumTank;
        this.tritiumTank = tritiumTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ICFPressBlockEntity press) {
        boolean changed = press.updateIdentifierTankTypes();
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, press);
        changed |= press.tickServer(level);
        press.networkPackNT(15);
        if (changed || level.getGameTime() % 20L == 0L) {
            press.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getDeuteriumTank() {
        return deuteriumTank;
    }

    public HbmFluidTank getTritiumTank() {
        return tritiumTank;
    }

    public int getMuon() {
        return muon;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineICFPress", "ICF Fuel Press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ICFPressMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(deuteriumTank, tritiumTank);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(deuteriumTank, tritiumTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of();
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
    protected Iterable<FluidPort> getFluidPorts() {
        return List.of(
                FluidPort.of(0, 0, -1, Direction.NORTH),
                FluidPort.of(0, 0, 1, Direction.SOUTH),
                FluidPort.of(-1, 0, 0, Direction.WEST),
                FluidPort.of(1, 0, 0, Direction.EAST),
                FluidPort.of(0, 1, 0, Direction.UP),
                FluidPort.of(0, -1, 0, Direction.DOWN));
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == deuteriumTank.getTankType() || type == tritiumTank.getTankType();
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(deuteriumTank, tritiumTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        deuteriumTank.writeToNbt(tag, "t0");
        tritiumTank.writeToNbt(tag, "t1");
        tag.putByte("muon", (byte) muon);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        if (hasTankTag(tag, "t0")) {
            deuteriumTank.readFromNbt(tag, "t0");
        }
        if (hasTankTag(tag, "t1")) {
            tritiumTank.readFromNbt(tag, "t1");
        }
        muon = tag.getByte("muon");
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        topBottomItemHandler.invalidate();
        sideItemHandler.invalidate();
        nullSideItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.UP || side == Direction.DOWN) {
                return topBottomItemHandler.cast();
            }
            return side == null ? nullSideItemHandler.cast() : sideItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putByte("muon", (byte) muon);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains("muon")) {
            muon = tag.getByte("muon");
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeByte((byte) muon);
        writeTank(data, deuteriumTank);
        writeTank(data, tritiumTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        muon = data.readByte();
        readTank(data, deuteriumTank);
        readTank(data, tritiumTank);
    }

    private boolean tickServer(Level level) {
        boolean changed = loadMuon();
        changed |= press();
        return changed;
    }

    private boolean updateIdentifierTankTypes() {
        boolean changed = setFluidTankTypeFromIdentifierSlotReport(items, SLOT_FLUID_ID_1, SLOT_FLUID_ID_1,
                deuteriumTank, 0, false).changed();
        changed |= setFluidTankTypeFromIdentifierSlotReport(items, SLOT_FLUID_ID_2, SLOT_FLUID_ID_2,
                tritiumTank, 0, false).changed();
        return changed;
    }

    private boolean loadMuon() {
        ItemStack input = items.getStackInSlot(SLOT_MUON);
        if (muon > 0 || !input.is(ModItems.PARTICLE_MUON.get())) {
            return false;
        }
        ItemStack container = input.getCraftingRemainingItem();
        ItemStack current = items.getStackInSlot(SLOT_MUON_OUT);
        if (!container.isEmpty()) {
            if (current.isEmpty()) {
                ItemStack stored = container.copy();
                stored.setCount(1);
                items.setStackInSlot(SLOT_MUON_OUT, stored);
            } else if (current.is(container.getItem()) && current.getDamageValue() == container.getDamageValue()
                    && current.getCount() < current.getMaxStackSize()) {
                current.grow(1);
            } else {
                return false;
            }
        }
        muon = MAX_MUON;
        items.extractItem(SLOT_MUON, 1, false);
        return true;
    }

    private boolean press() {
        if (!items.getStackInSlot(SLOT_EMPTY).is(ModItems.ICF_PELLET_EMPTY.get())
                || !items.getStackInSlot(SLOT_OUTPUT).isEmpty()) {
            return false;
        }
        FuelChoice first = chooseFuel(deuteriumTank, items.getStackInSlot(SLOT_FUEL_1));
        FuelChoice second = chooseFuel(tritiumTank, items.getStackInSlot(SLOT_FUEL_2));
        if (first == null || second == null || first.type == second.type) {
            return false;
        }
        items.setStackInSlot(SLOT_OUTPUT, ICFPelletItem.setup(first.type, second.type, muon > 0));
        items.extractItem(SLOT_EMPTY, 1, false);
        consumeFuel(first, deuteriumTank, SLOT_FUEL_1);
        consumeFuel(second, tritiumTank, SLOT_FUEL_2);
        if (muon > 0) {
            muon--;
        }
        return true;
    }

    private FuelChoice chooseFuel(HbmFluidTank tank, ItemStack solid) {
        FuelType fluidFuel = tank.getFill() >= 1000 ? ICFPelletItem.fuelFromFluid(tank.getTankType()) : null;
        if (fluidFuel != null) {
            return new FuelChoice(fluidFuel, true);
        }
        FuelType solidFuel = ICFPelletItem.fuelFromItem(solid);
        return solidFuel == null ? null : new FuelChoice(solidFuel, false);
    }

    private void consumeFuel(FuelChoice choice, HbmFluidTank tank, int solidSlot) {
        if (choice.fluid) {
            tank.setFill(tank.getFill() - 1000);
        } else {
            items.extractItem(solidSlot, 1, false);
        }
    }

    private record FuelChoice(FuelType type, boolean fluid) {
    }

    private static void writeTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.write(data, tank);
    }

    private static void readTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.read(data, tank);
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private class AccessibleItemHandler implements IItemHandler {
        private final int[] accessibleSlots;

        private AccessibleItemHandler(int[] accessibleSlots) {
            this.accessibleSlots = accessibleSlots;
        }

        @Override public int getSlots() { return accessibleSlots.length; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? ItemStack.EMPTY : items.getStackInSlot(mapped);
        }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = map(slot);
            return mapped >= 0 && isAutomationItemValid(mapped, stack)
                    ? items.insertItem(mapped, stack, simulate)
                    : stack;
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = map(slot);
            return mapped >= 0 && (mapped == SLOT_OUTPUT || mapped == SLOT_MUON_OUT)
                    ? items.extractItem(mapped, amount, simulate)
                    : ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int slot) {
            int mapped = map(slot);
            return mapped < 0 ? 0 : items.getSlotLimit(mapped);
        }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = map(slot);
            return mapped >= 0 && isAutomationItemValid(mapped, stack);
        }

        private int map(int slot) {
            return slot >= 0 && slot < accessibleSlots.length ? accessibleSlots[slot] : -1;
        }

        private boolean isAutomationItemValid(int mapped, ItemStack stack) {
            return switch (mapped) {
                case SLOT_EMPTY -> stack.is(ModItems.ICF_PELLET_EMPTY.get());
                case SLOT_MUON -> stack.is(ModItems.PARTICLE_MUON.get());
                case SLOT_FUEL_1, SLOT_FUEL_2 -> !stack.isEmpty();
                default -> false;
            };
        }
    }
}
