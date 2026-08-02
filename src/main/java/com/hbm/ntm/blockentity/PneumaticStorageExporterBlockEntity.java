package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.ntl.PneumaticConnector;
import com.hbm.ntm.api.redstoneoverradio.RORInteractive;
import com.hbm.ntm.api.redstoneoverradio.RTTYSystem;
import com.hbm.ntm.menu.PneumaticStorageExporterMenu;
import com.hbm.ntm.network.HbmTileSyncable;
import com.hbm.ntm.network.HbmGuiControlSecurity;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNetwork;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNode;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticStackCache;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
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

/** 1.7.10 TileEntityPneumoStorageExporter. OpenComputers remains deliberately unported. */
public class PneumaticStorageExporterBlockEntity extends BlockEntity implements MenuProvider, PneumaticConnector, HbmTileSyncable, RORInteractive {
    public static final int FILTER_SLOT_COUNT = 9;
    public static final int SLOT_COUNT = 18;
    public static final int SLOT_DELAY = 10;
    public static final int MODE_AS_MUCH_AS_POSSIBLE = 0;
    public static final int MODE_FULL_STACK = 1;
    public static final int MODE_FULL_REQUEST = 2;
    public static final int CONTROL_CONTINUOUS = 0;
    public static final int CONTROL_REQUEST_MODE = 1;
    public static final int CONTROL_ROR_FILTERS = 2;
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_CONTINUOUS = "continuousRequest";
    private static final String TAG_MODE = "requestMode";
    private static final String TAG_ROR_CONFIGURED = "rorConfiguredMode";
    private static final String TAG_ROR_FILTERS = "rorFilters";
    private static final String TAG_REDSTONE = "lastRedstone";
    private static final String TAG_DELAY = "slotDelay";
    private static final String TAG_CONTROL = "control";
    private final int[] slotDelay = new int[FILTER_SLOT_COUNT];
    private final ItemStack[] rorFilters = new ItemStack[FILTER_SLOT_COUNT];
    private boolean continuousRequest;
    private boolean rorConfiguredMode;
    private int requestMode;
    private boolean lastRedstone;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) { @Override protected void onContentsChanged(int slot) { setChanged(); } };
    private final LazyOptional<IItemHandler> allItems = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> outputItems = LazyOptional.of(() -> new OutputHandler());
    private PneumaticNode node;
    private PneumaticStackCache cache;

    public PneumaticStorageExporterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PNEUMATIC_STORAGE_EXPORTER.get(), pos, state);
        java.util.Arrays.fill(rorFilters, ItemStack.EMPTY);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, PneumaticStorageExporterBlockEntity exporter) {
        exporter.refreshNetwork();
        if (exporter.cache == null || exporter.cache.hasExpired()) exporter.cache = new PneumaticStackCache(pos);
        PneumaticNetwork network = exporter.getPneumaticNet();
        if (network != null) network.addStackCache(exporter.cache);
        for (int i = 0; i < FILTER_SLOT_COUNT; i++) if (exporter.slotDelay[i] > 0) exporter.slotDelay[i]--;
        boolean redstone = level.hasNeighborSignal(pos);
        if (exporter.continuousRequest) exporter.doRequest(false);
        else if (redstone && !exporter.lastRedstone) exporter.doRequest(true);
        exporter.lastRedstone = redstone;
    }
    public void doRequest(boolean force) {
        if (requestMode != MODE_FULL_REQUEST) {
            for (int i = 0; i < FILTER_SLOT_COUNT; i++) if (!requestSlot(i, force)) slotDelay[i] = SLOT_DELAY;
            return;
        }
        if (!force) for (int i = 0; i < FILTER_SLOT_COUNT; i++) if (!filter(i).isEmpty() && slotDelay[i] > 0) return;
        for (int i = 0; i < FILTER_SLOT_COUNT; i++) {
            ItemStack filter = filter(i); if (filter.isEmpty()) continue;
            ItemStack output = items.getStackInSlot(i + FILTER_SLOT_COUNT);
            int present = sameNoTag(output, filter) ? output.getCount() : 0;
            if ((!output.isEmpty() && present == 0) || filter.getMaxStackSize() - present < filter.getCount() || availability(filter) < filter.getCount()) { slotDelay[i] = SLOT_DELAY; return; }
        }
        for (int i = 0; i < FILTER_SLOT_COUNT; i++) pull(i, filter(i));
    }
    public boolean requestSlot(int slot, boolean force) {
        if (!force && slotDelay[slot] > 0) return true;
        ItemStack filter = filter(slot);
        if (cache == null || cache.hasExpired() || filter.isEmpty()) return false;
        ItemStack output = items.getStackInSlot(slot + FILTER_SLOT_COUNT);
        int present = sameNoTag(output, filter) ? output.getCount() : 0;
        int capacity = filter.getMaxStackSize() - present;
        if ((!output.isEmpty() && present == 0) || (capacity < filter.getCount() && requestMode != MODE_AS_MUCH_AS_POSSIBLE)) return false;
        long available = availability(filter);
        if ((available < filter.getCount() && requestMode != MODE_AS_MUCH_AS_POSSIBLE) || available <= 0L) return false;
        return pull(slot, filter);
    }
    private boolean pull(int slot, ItemStack filter) {
        if (filter.isEmpty() || cache == null || cache.hasExpired()) return false;
        ItemStack output = items.getStackInSlot(slot + FILTER_SLOT_COUNT);
        int present = sameNoTag(output, filter) ? output.getCount() : 0;
        int amount = (int) Math.min(Math.min(filter.getCount(), availability(filter)), filter.getMaxStackSize() - present);
        if (amount <= 0) return false;
        ItemStack desired = filter.copy(); desired.setCount(amount);
        int consumed = (int) cache.consumeItemsAndReturnQuantity(desired, amount);
        if (consumed <= 0) return false;
        ItemStack result = filter.copy(); result.setCount(present + consumed);
        items.setStackInSlot(slot + FILTER_SLOT_COUNT, result);
        return true;
    }
    private ItemStack filter(int slot) { return rorConfiguredMode ? rorFilters[slot] : items.getStackInSlot(slot); }
    private long availability(ItemStack filter) {
        if (cache == null || cache.hasExpired() || filter.isEmpty()) return 0L;
        PneumaticStackCache.CacheSlot entry = cache.getCacheSlots().get(PneumaticStackCache.StackIdentity.of(filter));
        return entry == null || filter.hasTag() ? 0L : entry.getStackSize();
    }
    private static boolean sameNoTag(ItemStack first, ItemStack second) { return !first.isEmpty() && first.getItem() == second.getItem() && first.getDamageValue() == second.getDamageValue() && !first.hasTag(); }
    public ItemStackHandler getItems() { return items; }
    public boolean isContinuousRequest() { return continuousRequest; }
    public boolean isRorConfiguredMode() { return rorConfiguredMode; }
    public int getRequestMode() { return requestMode; }
    public ItemStack getRorFilter(int slot) { return slot >= 0 && slot < FILTER_SLOT_COUNT ? rorFilters[slot].copy() : ItemStack.EMPTY; }
    public int[] getSlotDelay() { return slotDelay.clone(); }
    public PneumaticStackCache getCache() { return cache; }
    public PneumaticNetwork getPneumaticNet() { return node == null ? null : node.getPneumaticNet(); }
    @Override public boolean canConnectPneumatic(Direction side) { return side != null; }
    @Override public Component getDisplayName() { return Component.translatableWithFallback("container.pneumoStorageExporter", "PSN Exporter"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new PneumaticStorageExporterMenu(id, inventory, this); }
    public static CompoundTag controlTag(int control) { CompoundTag tag = new CompoundTag(); tag.putInt(TAG_CONTROL, control); return tag; }
    @Override public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return HbmGuiControlSecurity.hasLegacyMachineUsePermission(player, this)
                && tag.contains(TAG_CONTROL) && tag.getInt(TAG_CONTROL) >= CONTROL_CONTINUOUS && tag.getInt(TAG_CONTROL) <= CONTROL_ROR_FILTERS;
    }
    @Override public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        switch (tag.getInt(TAG_CONTROL)) {
            case CONTROL_CONTINUOUS -> continuousRequest = !continuousRequest;
            case CONTROL_REQUEST_MODE -> requestMode = (requestMode + 1) % 3;
            case CONTROL_ROR_FILTERS -> rorConfiguredMode = !rorConfiguredMode;
            default -> { return; }
        }
        setChanged();
        syncToTracking();
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ITEMS, items.serializeNBT());
        tag.putBoolean(TAG_CONTINUOUS, continuousRequest);
        tag.putBoolean(TAG_ROR_CONFIGURED, rorConfiguredMode);
        tag.putInt(TAG_MODE, requestMode);
        CompoundTag filters = new CompoundTag();
        for (int i = 0; i < FILTER_SLOT_COUNT; i++) filters.put("filter_" + i, rorFilters[i].save(new CompoundTag()));
        tag.put(TAG_ROR_FILTERS, filters);
        tag.putBoolean(TAG_REDSTONE, lastRedstone);
        tag.putIntArray(TAG_DELAY, slotDelay);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_ITEMS)) items.deserializeNBT(tag.getCompound(TAG_ITEMS));
        continuousRequest = tag.getBoolean(TAG_CONTINUOUS);
        rorConfiguredMode = tag.getBoolean(TAG_ROR_CONFIGURED);
        requestMode = Math.floorMod(tag.getInt(TAG_MODE), 3);
        CompoundTag filters = tag.getCompound(TAG_ROR_FILTERS);
        for (int i = 0; i < FILTER_SLOT_COUNT; i++) rorFilters[i] = filters.contains("filter_" + i) ? ItemStack.of(filters.getCompound("filter_" + i)) : ItemStack.EMPTY;
        lastRedstone = tag.getBoolean(TAG_REDSTONE);
        int[] saved = tag.getIntArray(TAG_DELAY);
        System.arraycopy(saved, 0, slotDelay, 0, Math.min(saved.length, slotDelay.length));
    }
    @Override public CompoundTag getClientSyncTag() { return saveWithoutMetadata(); }
    @Override public void handleClientSyncTag(CompoundTag tag) { load(tag); }

    @Override public String[] getFunctionInfo() {
        return new String[] {
                PREFIX_FUNCTION + "setfilter" + NAME_SEPARATOR + "slot" + PARAM_SEPARATOR + "itemid" + PARAM_SEPARATOR + "itemmeta" + PARAM_SEPARATOR + "amount",
                PREFIX_FUNCTION + "setcontinuous" + NAME_SEPARATOR + "on/off",
                PREFIX_FUNCTION + "request",
                PREFIX_FUNCTION + "requestslot" + NAME_SEPARATOR + "slot",
                PREFIX_FUNCTION + "checkavailability" + NAME_SEPARATOR + "itemid" + PARAM_SEPARATOR + "itemmeta" + PARAM_SEPARATOR + "returnchannel"
        };
    }

    @Override public String runRORFunction(String name, String[] params) {
        if ((PREFIX_FUNCTION + "setfilter").equals(name) && params.length == 4) {
            int slot = RORInteractive.parseInt(params[0], 1, FILTER_SLOT_COUNT) - 1;
            Item item = BuiltInRegistries.ITEM.byId(RORInteractive.parseInt(params[1], 0, Short.MAX_VALUE));
            int meta = RORInteractive.parseInt(params[2], 0, Short.MAX_VALUE);
            int amount = RORInteractive.parseInt(params[3], 1, 64);
            ItemStack filter = item == null ? ItemStack.EMPTY : new ItemStack(item, amount);
            if (!filter.isEmpty()) filter.setDamageValue(meta);
            rorFilters[slot] = filter;
            setChanged();
            syncToTracking();
            return null;
        }
        if ((PREFIX_FUNCTION + "setcontinuous").equals(name) && params.length == 1) {
            if ("on".equals(params[0])) continuousRequest = true;
            if ("off".equals(params[0])) continuousRequest = false;
            setChanged();
            syncToTracking();
            return null;
        }
        if ((PREFIX_FUNCTION + "request").equals(name)) {
            doRequest(true);
            return null;
        }
        if ((PREFIX_FUNCTION + "requestslot").equals(name) && params.length == 1) {
            int slot = RORInteractive.parseInt(params[0], 1, FILTER_SLOT_COUNT) - 1;
            if (!requestSlot(slot, true)) slotDelay[slot] = SLOT_DELAY;
            return null;
        }
        if ((PREFIX_FUNCTION + "checkavailability").equals(name) && params.length == 3) {
            Item item = BuiltInRegistries.ITEM.byId(RORInteractive.parseInt(params[0], 0, Short.MAX_VALUE));
            int meta = RORInteractive.parseInt(params[1], 0, Short.MAX_VALUE);
            ItemStack filter = item == null ? ItemStack.EMPTY : new ItemStack(item);
            if (!filter.isEmpty()) filter.setDamageValue(meta);
            RTTYSystem.broadcast(level, params[2], Long.toString(availability(filter)));
        }
        return null;
    }
    @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) { if (cap == ForgeCapabilities.ITEM_HANDLER) return (side == null ? allItems : outputItems).cast(); return super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); allItems.invalidate(); outputItems.invalidate(); }
    @Override public void setRemoved() { removeNetwork(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { removeNetwork(); super.onChunkUnloaded(); }
    private void refreshNetwork() { if (level == null || level.isClientSide) return; Set<Direction> connections = PneumaticUtil.allConnections(); if (node != null && !node.isExpired() && !node.getConnections().equals(connections)) removeNetwork(); if (node == null || node.isExpired()) node = PneumaticNodespace.createNode(level, new PneumaticNode(worldPosition, connections)); }
    private void removeNetwork() { if (level != null && !level.isClientSide) PneumaticNodespace.destroyNode(level, worldPosition); node = null; if (cache != null) { cache.dissolveCache(); cache = null; } }
    private final class OutputHandler implements IItemHandler {
        @Override public int getSlots() { return FILTER_SLOT_COUNT; }
        @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < FILTER_SLOT_COUNT ? items.getStackInSlot(slot + FILTER_SLOT_COUNT) : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return slot >= 0 && slot < FILTER_SLOT_COUNT ? items.extractItem(slot + FILTER_SLOT_COUNT, amount, simulate) : ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    }
}
