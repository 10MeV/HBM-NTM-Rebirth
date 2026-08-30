package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.PneumaticStorageAccessBlockEntity;
import com.hbm.ntm.network.HbmMenuActionReceiver;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticStackCache;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Server-authoritative view and interaction surface for a pneumatic stack cache.
 * The old ContainerPneumoStorageAccess never exposed an inventory: its 48 slots were
 * an interactive projection of StackCache.  Keep that boundary here so normal item
 * capabilities cannot accidentally bypass the pneumatic storage monitors.
 */
public class PneumaticStorageAccessMenu extends AbstractContainerMenu implements HbmMenuActionReceiver {
    public static final int CACHE_SLOT_COUNT = 48;
    public static final String CACHE_AMOUNT_TAG = "hbmPneumaticCacheAmount";
    public static final String CACHE_STACKS_TAG = "hbmPneumaticCacheStacks";
    public static final int ACTION_CACHE_LEFT = 0;
    public static final int ACTION_CACHE_RIGHT = 1;
    public static final int ACTION_CACHE_SHIFT = 2;
    public static final int ACTION_SET_PAGE = 3;
    public static final int ACTION_SET_SORT = 4;
    public static final int ACTION_SET_SEARCH = 5;
    public static final int ACTION_TOGGLE_DETAILED_SEARCH = 6;
    private static final int PLAYER_SLOT_START = CACHE_SLOT_COUNT;

    private final PneumaticStorageAccessBlockEntity blockEntity;
    private final Inventory playerInventory;
    private final SimpleContainer listing = new SimpleContainer(CACHE_SLOT_COUNT);
    private int page;
    private int pageLimit;
    private SortMode sortMode = SortMode.AMOUNT;
    private String search = "";
    private boolean detailedSearch;

    public PneumaticStorageAccessMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public PneumaticStorageAccessMenu(int containerId, Inventory inventory, PneumaticStorageAccessBlockEntity blockEntity) {
        super(ModMenuTypes.PNEUMATIC_STORAGE_ACCESS.get(), containerId);
        this.blockEntity = blockEntity;
        this.playerInventory = inventory;
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 8; column++) {
                addSlot(new ReadOnlySlot(listing, column + row * 8, 42 + column * 18, 17 + row * 18));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 42, 169, 227);
        addDataSlot(new DataSlot() {
            @Override public int get() { return page; }
            @Override public void set(int value) { page = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return pageLimit; }
            @Override public void set(int value) { pageLimit = value; }
        });
        refreshListing();
    }

    public PneumaticStorageAccessBlockEntity getBlockEntity() { return blockEntity; }
    public int getPage() { return page; }
    public SortMode getSortMode() { return sortMode; }
    public boolean isDetailedSearch() { return detailedSearch; }
    public String getSearch() { return search; }

    public int getPageLimit() {
        return pageLimit;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == playerInventory.player && player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D, blockEntity.getBlockPos().getZ() + 0.5D) <= 225.0D;
    }

    @Override
    public void broadcastChanges() {
        refreshListing();
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < PLAYER_SLOT_START || index >= slots.size() || player != playerInventory.player) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        PneumaticStackCache cache = usableCache();
        if (cache == null) {
            return ItemStack.EMPTY;
        }
        long remainder = cache.addItemsAndReturnQuantity(stack, stack.getCount());
        int inserted = stack.getCount() - (int) Math.min(Integer.MAX_VALUE, remainder);
        if (inserted <= 0) {
            return ItemStack.EMPTY;
        }
        stack.shrink(inserted);
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        broadcastChanges();
        return original;
    }

    @Override
    public boolean canReceiveMenuAction(ServerPlayer player, int action, int value, CompoundTag data) {
        if (player != playerInventory.player || !stillValid(player)) {
            return false;
        }
        if (action < ACTION_CACHE_LEFT || action > ACTION_TOGGLE_DETAILED_SEARCH) {
            return false;
        }
        if (action <= ACTION_CACHE_SHIFT) {
            return value >= 0 && value < CACHE_SLOT_COUNT && usableCache() != null;
        }
        if (action == ACTION_SET_SEARCH) {
            return data.contains("search") && data.getString("search").length() <= 50;
        }
        return true;
    }

    @Override
    public void handleMenuAction(ServerPlayer player, int action, int value, CompoundTag data) {
        switch (action) {
            case ACTION_CACHE_LEFT -> interactCache(player, value, false, false);
            case ACTION_CACHE_RIGHT -> interactCache(player, value, true, false);
            case ACTION_CACHE_SHIFT -> interactCache(player, value, false, true);
            case ACTION_SET_PAGE -> page = Math.max(0, Math.min(value, getPageLimit()));
            case ACTION_SET_SORT -> {
                if (value >= 0 && value < SortMode.values().length) {
                    sortMode = SortMode.values()[value];
                    page = 0;
                }
            }
            case ACTION_SET_SEARCH -> {
                search = data.getString("search").toLowerCase(Locale.ROOT);
                page = 0;
            }
            case ACTION_TOGGLE_DETAILED_SEARCH -> {
                detailedSearch = !detailedSearch;
                page = 0;
            }
            default -> { }
        }
    }

    private void interactCache(ServerPlayer player, int slotIndex, boolean rightClick, boolean shiftClick) {
        PneumaticStackCache.CacheSlot entry = getEntryAt(slotIndex);
        PneumaticStackCache cache = usableCache();
        if (cache == null) {
            return;
        }
        ItemStack carried = player.containerMenu.getCarried();
        // The old null-identity cache slot is intentionally clickable: dropping an item on an
        // empty visible cell is how a player assigns a free Mono/Clutter storage slot.
        if (entry == null || entry.getDisplayStack() == null) {
            if (!carried.isEmpty() && !shiftClick) {
                int requested = rightClick ? 1 : carried.getCount();
                long remainder = cache.addItemsAndReturnQuantity(carried, requested);
                carried.shrink(requested - (int) Math.min(Integer.MAX_VALUE, remainder));
                player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            }
            return;
        }
        ItemStack source = entry.getDisplayStack().copy();
        if (shiftClick) {
            int requested = (int) Math.min(source.getMaxStackSize(), entry.getStackSize());
            source.setCount(requested);
            if (player.getInventory().add(source)) {
                cache.consumeItemsAndReturnQuantity(entry.getDisplayStack(), requested);
            } else {
                int inserted = requested - source.getCount();
                if (inserted > 0) cache.consumeItemsAndReturnQuantity(entry.getDisplayStack(), inserted);
            }
            return;
        }
        if (!carried.isEmpty() && !ItemStack.isSameItemSameTags(carried, source)) {
            int requested = rightClick ? 1 : carried.getCount();
            long remainder = cache.addItemsAndReturnQuantity(carried, requested);
            carried.shrink(requested - (int) Math.min(Integer.MAX_VALUE, remainder));
            player.containerMenu.setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            return;
        }
        int alreadyHeld = carried.isEmpty() ? 0 : carried.getCount();
        int capacity = source.getMaxStackSize() - alreadyHeld;
        int requested = rightClick ? Math.min(1, capacity) : Math.min(capacity, (int) Math.min(Integer.MAX_VALUE, entry.getStackSize()));
        if (requested <= 0) {
            return;
        }
        long grabbed = cache.consumeItemsAndReturnQuantity(source, requested);
        if (grabbed <= 0) {
            return;
        }
        source.setCount(alreadyHeld + (int) grabbed);
        player.containerMenu.setCarried(source);
    }

    private PneumaticStackCache.CacheSlot getEntryAt(int slotIndex) {
        int entryIndex = page * 8 + slotIndex;
        List<PneumaticStackCache.CacheSlot> entries = getEntries();
        return entryIndex >= 0 && entryIndex < entries.size() ? entries.get(entryIndex) : null;
    }

    private PneumaticStackCache usableCache() {
        PneumaticStackCache cache = blockEntity.getCache();
        return cache == null || cache.hasExpired() ? null : cache;
    }

    private List<PneumaticStackCache.CacheSlot> getEntries() {
        PneumaticStackCache cache = usableCache();
        if (cache == null) return List.of();
        List<PneumaticStackCache.CacheSlot> entries = new ArrayList<>(cache.getCacheSlots().values());
        entries.removeIf(entry -> entry.getStackSize() <= 0L || entry.getDisplayStack() == null || entry.getDisplayStack().isEmpty());
        if (!search.isEmpty()) {
            entries.removeIf(entry -> !matchesSearch(entry.getDisplayStack()));
        }
        entries.sort(sortMode.comparator());
        return entries;
    }

    private boolean matchesSearch(ItemStack stack) {
        if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(search)) return true;
        if (!detailedSearch) return false;
        return stack.getTooltipLines(playerInventory.player, TooltipFlag.Default.NORMAL).stream()
                .anyMatch(line -> line.getString().toLowerCase(Locale.ROOT).contains(search));
    }

    private void refreshListing() {
        List<PneumaticStackCache.CacheSlot> entries = getEntries();
        // GUIPneumoStorageAccess used ceil(stackCount / 8D - 6D) and then forced a
        // minimum of one.  Preserve its one extra reachable page even for a short list.
        pageLimit = Math.max(1, (int) Math.ceil(entries.size() / 8.0D - 6.0D));
        page = Math.max(0, Math.min(page, pageLimit));
        int start = page * 8;
        for (int slot = 0; slot < CACHE_SLOT_COUNT; slot++) {
            int entryIndex = start + slot;
            if (entryIndex >= entries.size()) {
                listing.setItem(slot, ItemStack.EMPTY);
                continue;
            }
            PneumaticStackCache.CacheSlot entry = entries.get(entryIndex);
            ItemStack display = entry.getDisplayStack().copy();
            display.setCount(1);
            display.getOrCreateTag().putLong(CACHE_AMOUNT_TAG, entry.getStackSize());
            display.getTag().putInt(CACHE_STACKS_TAG, entry.getMonitors().size());
            listing.setItem(slot, display);
        }
    }

    public enum SortMode {
        AMOUNT(Comparator.comparingLong(PneumaticStackCache.CacheSlot::getStackSize).reversed()
                .thenComparing(entry -> entry.getDisplayStack().getItem().toString()).thenComparingInt(entry -> entry.getDisplayStack().getDamageValue())),
        ITEM_ID(Comparator.comparing((PneumaticStackCache.CacheSlot entry) -> entry.getDisplayStack().getItem().toString())
                .thenComparingInt(entry -> entry.getDisplayStack().getDamageValue())
                .thenComparing(Comparator.comparingLong(PneumaticStackCache.CacheSlot::getStackSize).reversed())),
        LOCALIZED_NAME(Comparator.comparing((PneumaticStackCache.CacheSlot entry) -> entry.getDisplayStack().getHoverName().getString(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.getDisplayStack().getItem().toString())),
        INTERNAL_NAME(Comparator.comparing((PneumaticStackCache.CacheSlot entry) -> entry.getDisplayStack().getDescriptionId(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.getDisplayStack().getItem().toString()));

        private final Comparator<PneumaticStackCache.CacheSlot> comparator;
        SortMode(Comparator<PneumaticStackCache.CacheSlot> comparator) { this.comparator = comparator; }
        public Comparator<PneumaticStackCache.CacheSlot> comparator() { return comparator; }
    }

    private static PneumaticStorageAccessBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PneumaticStorageAccessBlockEntity access) return access;
        throw new IllegalStateException("Expected pneumatic storage access at " + pos);
    }

    private static class ReadOnlySlot extends Slot {
        private ReadOnlySlot(SimpleContainer inventory, int slot, int x, int y) { super(inventory, slot, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
        @Override public boolean mayPickup(Player player) { return false; }
    }
}
