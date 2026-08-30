package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.ntl.PneumaticConnector;
import com.hbm.ntm.menu.PneumaticStorageImporterMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNetwork;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNode;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticStackCache;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/** 1.7.10 TileEntityPneumoStorageImporter on the shared pneumatic cache. */
public class PneumaticStorageImporterBlockEntity extends BlockEntity implements MenuProvider, PneumaticConnector {
    public static final int SLOT_COUNT = 9;
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_DELAY = "delay";
    private final int[] delay = new int[SLOT_COUNT];
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!getStackInSlot(slot).isEmpty()) {
                delay[slot] = Math.max(delay[slot], 1);
            }
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> internalItems = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> externalInputItems = LazyOptional.of(ExternalInputHandler::new);
    private PneumaticNode node;
    private PneumaticStackCache cache;

    public PneumaticStorageImporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PNEUMATIC_STORAGE_IMPORTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PneumaticStorageImporterBlockEntity importer) {
        importer.refreshNetwork();
        if (importer.cache == null || importer.cache.hasExpired()) {
            importer.cache = new PneumaticStackCache(pos);
        }
        PneumaticNetwork network = importer.getPneumaticNet();
        if (network != null) {
            network.addStackCache(importer.cache);
        }
        if (importer.cache == null || importer.cache.hasExpired()) {
            return;
        }
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (importer.delay[slot] > 0) {
                importer.delay[slot]--;
                continue;
            }
            ItemStack stack = importer.items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            long remaining = importer.cache.addItemsAndReturnQuantity(stack, stack.getCount());
            if (remaining == stack.getCount()) {
                importer.delay[slot] = 100;
            } else {
                importer.items.extractItem(slot, (int) (stack.getCount() - remaining), false);
            }
        }
    }

    public ItemStackHandler getItems() { return items; }
    public int[] getDelay() { return delay.clone(); }
    public PneumaticStackCache getCache() { return cache; }
    public PneumaticNetwork getPneumaticNet() { return node == null ? null : node.getPneumaticNet(); }

    @Override public boolean canConnectPneumatic(Direction side) { return side != null; }
    @Override public Component getDisplayName() { return Component.translatableWithFallback("container.pneumoStorageImporter", "PSN Importer"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PneumaticStorageImporterMenu(id, inventory, this);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ITEMS, items.serializeNBT());
        tag.putIntArray(TAG_DELAY, delay);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_ITEMS)) items.deserializeNBT(tag.getCompound(TAG_ITEMS));
        int[] savedDelay = tag.getIntArray(TAG_DELAY);
        System.arraycopy(savedDelay, 0, delay, 0, Math.min(delay.length, savedDelay.length));
    }

    @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return (side == null ? internalItems : externalInputItems).cast();
        }
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); internalItems.invalidate(); externalInputItems.invalidate(); }
    @Override public void setRemoved() { removeNetwork(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { removeNetwork(); super.onChunkUnloaded(); }

    private void refreshNetwork() {
        if (level == null || level.isClientSide) return;
        Set<Direction> connections = PneumaticUtil.allConnections();
        if (node != null && !node.isExpired() && !node.getConnections().equals(connections)) removeNetwork();
        if (node == null || node.isExpired()) node = PneumaticNodespace.createNode(level, new PneumaticNode(worldPosition, connections));
    }
    private void removeNetwork() {
        if (level != null && !level.isClientSide) PneumaticNodespace.destroyNode(level, worldPosition);
        node = null;
        if (cache != null) { cache.dissolveCache(); cache = null; }
    }

    /** Old ISidedInventory exposed all nine slots for insertion but never allowed extraction. */
    private final class ExternalInputHandler implements IItemHandler {
        @Override public int getSlots() { return SLOT_COUNT; }
        @Override public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return slot >= 0 && slot < SLOT_COUNT ? items.insertItem(slot, stack, simulate) : stack;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return slot >= 0 && slot < SLOT_COUNT ? items.getSlotLimit(slot) : 0; }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return slot >= 0 && slot < SLOT_COUNT && items.isItemValid(slot, stack);
        }
    }
}
