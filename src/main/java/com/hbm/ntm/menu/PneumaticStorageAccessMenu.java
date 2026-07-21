package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.PneumaticStorageAccessBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticStackCache;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PneumaticStorageAccessMenu extends AbstractContainerMenu {
    public static final int CACHE_SLOT_COUNT = 48;
    public static final String CACHE_AMOUNT_TAG = "hbmPneumaticCacheAmount";
    public static final String CACHE_STACKS_TAG = "hbmPneumaticCacheStacks";
    private final PneumaticStorageAccessBlockEntity blockEntity;
    private final SimpleContainer listing = new SimpleContainer(CACHE_SLOT_COUNT);

    public PneumaticStorageAccessMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public PneumaticStorageAccessMenu(int containerId, Inventory inventory, PneumaticStorageAccessBlockEntity blockEntity) {
        super(ModMenuTypes.PNEUMATIC_STORAGE_ACCESS.get(), containerId);
        this.blockEntity = blockEntity;
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 8; column++) {
                int slot = column + row * 8;
                addSlot(new ReadOnlySlot(listing, slot, 8 + column * 18, 17 + row * 18));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 169, 227);
        refreshListing();
    }

    public PneumaticStorageAccessBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5D, blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 225.0D;
    }

    @Override
    public void broadcastChanges() {
        refreshListing();
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private void refreshListing() {
        for (int slot = 0; slot < CACHE_SLOT_COUNT; slot++) {
            listing.setItem(slot, ItemStack.EMPTY);
        }
        PneumaticStackCache cache = blockEntity.getCache();
        if (cache == null || cache.hasExpired()) {
            return;
        }
        List<PneumaticStackCache.CacheSlot> entries = new ArrayList<>(cache.getCacheSlots().values());
        entries.removeIf(entry -> entry.getStackSize() <= 0L || entry.getDisplayStack() == null
                || entry.getDisplayStack().isEmpty());
        entries.sort(Comparator.comparingLong(PneumaticStackCache.CacheSlot::getStackSize).reversed()
                .thenComparing(entry -> entry.getDisplayStack().getItem().toString())
                .thenComparingInt(entry -> entry.getDisplayStack().getDamageValue()));
        for (int slot = 0; slot < Math.min(CACHE_SLOT_COUNT, entries.size()); slot++) {
            ItemStack display = entries.get(slot).getDisplayStack().copy();
            display.setCount(1);
            display.getOrCreateTag().putLong(CACHE_AMOUNT_TAG, entries.get(slot).getStackSize());
            display.getTag().putInt(CACHE_STACKS_TAG, entries.get(slot).getMonitors().size());
            listing.setItem(slot, display);
        }
    }

    private static PneumaticStorageAccessBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PneumaticStorageAccessBlockEntity access) {
            return access;
        }
        throw new IllegalStateException("Expected pneumatic storage access at " + pos);
    }

    private static class ReadOnlySlot extends Slot {
        private ReadOnlySlot(SimpleContainer inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
