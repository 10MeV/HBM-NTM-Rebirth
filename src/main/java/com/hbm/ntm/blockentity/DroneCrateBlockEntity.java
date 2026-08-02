package com.hbm.ntm.blockentity;

import com.hbm.ntm.drone.DroneLinkable;
import com.hbm.ntm.entity.item.DeliveryDroneEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.menu.DroneCrateMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.particle.ParticleUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Modern carrier for TileEntityDroneCrate's patrol item/fluid transfer contract. */
public class DroneCrateBlockEntity extends HbmFluidNetworkBlockEntity
        implements MenuProvider, DroneLinkable, HbmStandardFluidTransceiver, HbmLegacyButtonReceiver {
    public static final int ITEM_SLOTS = 18;
    public static final int FLUID_IDENTIFIER_SLOT = 18;
    public static final int CONTROL_TOGGLE_TYPE = 0;
    public static final int CONTROL_TOGGLE_MODE = 1;
    private static final String TAG_ITEMS = "items";
    private static final List<FluidPort> FLUID_PORTS = HbmFluidPortLayouts.allAdjacent();

    private final HbmFluidTank tank;
    /** Legacy crate transfers changed its array in one operation, then marked the tile once. */
    private boolean batchingInventoryChange;
    private final ItemStackHandler items = new ItemStackHandler(19) {
        @Override protected void onContentsChanged(int slot) {
            if (!batchingInventoryChange) {
                setChangedAndSync();
            }
        }
    };
    /** Legacy sided inventory exposed cargo slots 0-17 only; slot 18 is GUI-only fluid identification. */
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new CargoItemHandler(items));
    private BlockPos nextTarget;
    private boolean sendingMode;
    private boolean itemType = true;

    public DroneCrateBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.NONE, 64_000));
    }

    private DroneCrateBlockEntity(BlockPos pos, BlockState state, HbmFluidTank tank) {
        super(ModBlockEntities.DRONE_CRATE.get(), pos, state, List.of(tank));
        this.tank = tank;
    }

    public static void tick(Level level, DroneCrateBlockEntity crate) {
        if (level.isClientSide) return;
        boolean changed = crate.setFluidTankTypeFromIdentifierSlot(crate.items, FLUID_IDENTIFIER_SLOT, crate.tank);
        // TileEntityDroneCrate deliberately is not a normal Fluid MK2 node
        // host.  The 1.7.10 update only touches its six adjacent endpoints on
        // the world-time 20-tick pass: provider-mode crates subscribe as a
        // receiver, while requester-mode crates directly provide from their
        // tank.  Do not route this through HbmFluidNetworkBlockEntity's
        // generic dirty/signature refresh, which assumes a local node.
        if (level.getGameTime() % 20L == 0L && !crate.itemType) {
            if (crate.sendingMode) {
                crate.subscribeFluidReceiverToPorts(crate.tank.getTankType(), crate);
            } else {
                crate.tryProvideFluidToPorts(crate.tank.getTankType(), crate.tank.getPressure(), crate);
            }
        }
        if (crate.nextTarget != null) {
            crate.handleDockedDrones();
            BlockPos point = crate.dronePoint();
            ParticleUtil.spawnDroneLine(level, point.getX() + 0.5D, point.getY() + 0.5D, point.getZ() + 0.5D,
                    crate.nextTarget.getX() - point.getX(), crate.nextTarget.getY() - point.getY(),
                    crate.nextTarget.getZ() - point.getZ(), 0x00FFFF);
        }
        if (changed) crate.setChangedAndSync();
    }

    public ItemStackHandler items() { return items; }
    public HbmFluidTank tank() { return tank; }
    public boolean sendingMode() { return sendingMode; }
    public boolean itemType() { return itemType; }
    public BlockPos nextTarget() { return nextTarget; }

    private void handleDockedDrones() {
        AABB dock = new AABB(worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1);
        for (DeliveryDroneEntity drone : level.getEntitiesOfClass(DeliveryDroneEntity.class, dock,
                entity -> entity.getDeltaMovement().length() < 0.05D)) {
            drone.setTarget(nextTarget.getX() + 0.5D, nextTarget.getY(), nextTarget.getZ() + 0.5D);
            if (itemType) {
                if (sendingMode) loadItems(drone); else unloadItems(drone);
            } else if (sendingMode) loadFluid(drone); else unloadFluid(drone);
        }
    }

    private void loadItems(DeliveryDroneEntity drone) {
        if (drone.appearance() != 0) return;
        boolean loaded = false;
        batchingInventoryChange = true;
        try {
            for (int i = 0; i < ITEM_SLOTS; i++) {
                ItemStack stack = items.getStackInSlot(i);
                if (!stack.isEmpty()) { drone.setCargo(i, stack); items.setStackInSlot(i, ItemStack.EMPTY); loaded = true; }
            }
        } finally {
            batchingInventoryChange = false;
        }
        if (loaded) {
            drone.setAppearance(1);
            playTransferSound();
            setChangedAndSync();
        }
    }

    private void unloadItems(DeliveryDroneEntity drone) {
        if (drone.appearance() != 1) return;
        boolean emptied = true;
        batchingInventoryChange = true;
        try {
            for (int i = 0; i < ITEM_SLOTS; i++) {
                ItemStack cargo = drone.getCargo(i);
                if (items.getStackInSlot(i).isEmpty() && !cargo.isEmpty()) {
                    items.setStackInSlot(i, cargo); drone.setCargo(i, ItemStack.EMPTY);
                } else if (!cargo.isEmpty()) emptied = false;
            }
        } finally {
            batchingInventoryChange = false;
        }
        if (emptied) {
            drone.setAppearance(0);
            playTransferSound();
        }
        setChangedAndSync();
    }

    private void loadFluid(DeliveryDroneEntity drone) {
        if (drone.appearance() != 0 || tank.getFill() <= 0) return;
        FluidStack stack = HbmFluidForgeMappings.toForge(tank.getTankType(), tank.getFill());
        if (stack.isEmpty()) return;
        drone.setFluid(stack);
        tank.drain(tank.getFill(), false);
        drone.setAppearance(2);
        playTransferSound();
        setChangedAndSync();
    }

    private void unloadFluid(DeliveryDroneEntity drone) {
        if (drone.appearance() != 2 || drone.fluid().isEmpty()) return;
        FluidStack carried = drone.fluid();
        FluidType carriedType = HbmFluidForgeMappings.fromForge(carried);
        if (carriedType != tank.getTankType()) return;
        int accepted = tank.fill(carriedType, carried.getAmount(), 0, false);
        if (accepted > 0) {
            carried.shrink(accepted);
            drone.setFluid(carried);
            if (carried.isEmpty()) drone.setAppearance(0);
        }
        // The legacy branch played item.unpack whenever a compatible barrel met the crate,
        // including a full crate which left an overshoot on the drone.
        playTransferSound();
        setChangedAndSync();
    }

    @Override public BlockPos dronePoint() { return worldPosition.above(); }
    @Override public void setNextDroneTarget(BlockPos target) { nextTarget = target; setChangedAndSync(); }

    @Override public List<HbmFluidTank> getReceivingTanks() {
        return sendingMode && !itemType ? List.of(tank) : List.of();
    }
    @Override public List<HbmFluidTank> getSendingTanks() {
        return !sendingMode && !itemType ? List.of(tank) : List.of();
    }
    @Override public long transferFluid(FluidType type, int pressure, long amount) {
        long remainder = HbmStandardFluidTransceiver.super.transferFluid(type, pressure, amount);
        if (remainder != amount) setChangedAndSync();
        return remainder;
    }
    @Override public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (amount > 0) setChangedAndSync();
    }
    @Override protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return sendingMode && !itemType && type == tank.getTankType();
    }
    @Override protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return !sendingMode && !itemType && tank.getFill() > 0 && type == tank.getTankType();
    }
    @Override protected Iterable<FluidPort> getFluidPorts() { return FLUID_PORTS; }
    @Override protected boolean shouldCreateFluidNode() { return false; }
    @Override protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) { return getReceivingTanks(); }
    @Override protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) { return getSendingTanks(); }
    @Override protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) { return HbmFluidSideMode.BOTH; }

    @Override public void handleLegacyButton(ServerPlayer player, int value, int id) {
        if (id == CONTROL_TOGGLE_TYPE) itemType = !itemType;
        else if (id == CONTROL_TOGGLE_MODE) sendingMode = !sendingMode;
        else return;
        markTankTypesDirty(); setChangedAndSync();
    }
    @Override public Component getDisplayName() { return Component.translatable("container.droneCrate"); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DroneCrateMenu(id, inventory, this);
    }
    public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + .5D, worldPosition.getY() + .5D, worldPosition.getZ() + .5D) <= 128D; }
    /** Legacy DroneCrate#breakBlock split every stored stack into random 10..30 item spills. */
    public void dropContents() {
        if (level == null) return;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            HbmItemStackUtil.spillStack(level, worldPosition, items.getStackInSlot(slot));
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        if (nextTarget != null) tag.putLong("next", nextTarget.asLong()); tag.putBoolean("mode", sendingMode); tag.putBoolean("type", itemType); tank.writeToNbt(tag, "t");
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        batchingInventoryChange = true;
        try {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        } finally {
            batchingInventoryChange = false;
        }
        nextTarget = tag.contains("next") ? BlockPos.of(tag.getLong("next")) : null; sendingMode = tag.getBoolean("mode"); itemType = !tag.contains("type") || tag.getBoolean("type");
        if (tag.contains("t")) tank.readFromNbt(tag, "t"); markTankTypesDirty();
    }
    @Override public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        if (nextTarget != null) tag.putLong("next", nextTarget.asLong());
        tag.putBoolean("mode", sendingMode);
        tag.putBoolean("type", itemType);
        return tag;
    }
    @Override public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        nextTarget = tag.contains("next") ? BlockPos.of(tag.getLong("next")) : null;
        sendingMode = tag.getBoolean("mode");
        itemType = !tag.contains("type") || tag.getBoolean("type");
    }
    @Override public CompoundTag getUpdateTag() { return getClientSyncTag(); }
    @Override public void invalidateCaps() { super.invalidateCaps(); itemHandler.invalidate(); }
    @Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) return itemHandler.cast();
        return super.getCapability(capability, side);
    }
    private void setChangedAndSync() { setChanged(); if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS); }
    private void playTransferSound() {
        if (level != null) level.playSound(null, worldPosition, ModSounds.ITEM_UNPACK.get(),
                net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 0.75F);
    }
    private static final class CargoItemHandler implements IItemHandler {
        private final ItemStackHandler delegate;
        private CargoItemHandler(ItemStackHandler delegate) { this.delegate = delegate; }
        @Override public int getSlots() { return ITEM_SLOTS; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return valid(slot) ? delegate.getStackInSlot(slot) : ItemStack.EMPTY; }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return valid(slot) ? delegate.insertItem(slot, stack, simulate) : stack; }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return valid(slot) ? delegate.extractItem(slot, amount, simulate) : ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return valid(slot) ? delegate.getSlotLimit(slot) : 0; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return valid(slot) && delegate.isItemValid(slot, stack); }
        private boolean valid(int slot) { return slot >= 0 && slot < ITEM_SLOTS; }
    }
}
