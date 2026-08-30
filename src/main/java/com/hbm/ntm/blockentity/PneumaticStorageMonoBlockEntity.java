package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.common.CopiableSettings;
import com.hbm.ntm.api.ntl.ISlotMonitorProvider;
import com.hbm.ntm.api.ntl.PneumaticConnector;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.PneumaticStorageMonoMenu;
import com.hbm.ntm.network.HbmGuiControlSecurity;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNetwork;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNode;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticSlotMonitor;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticStackCache;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Modern equivalent of TileEntityPneumoStorageMono.  Filter stacks define, but never hold, the bulk stock. */
public class PneumaticStorageMonoBlockEntity extends HbmFluidNetworkBlockEntity implements MenuProvider,
        PneumaticConnector, ISlotMonitorProvider, HbmStandardFluidReceiver, HbmLegacyControlReceiver, CopiableSettings {
    public static final int SLOT_COUNT = 3;
    public static final int CAPACITY = 100_000;
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_AMOUNTS = "amounts";
    private static final String TAG_SETTINGS_ITEMS = "items";
    private static final String TAG_SETTINGS_SLOT = "slot";

    private final int[] amounts = new int[SLOT_COUNT];
    private final ItemStackHandler filters = new ItemStackHandler(SLOT_COUNT) {
        @Override protected int getStackLimit(int slot, @NotNull ItemStack stack) { return 1; }
        @Override protected void onContentsChanged(int slot) { setChangedAndUpdate(); }
    };
    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> filters);
    private final PneumaticSlotMonitor[] monitors = new PneumaticSlotMonitor[SLOT_COUNT];
    private PneumaticNode node;
    private boolean wasAvailable;

    public PneumaticStorageMonoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PNEUMATIC_STORAGE_MONO.get(), pos, state,
                List.of(new HbmFluidTank(HbmFluids.AIR, 4_000).withPressure(1)));
        for (int slot = 0; slot < SLOT_COUNT; slot++) monitors[slot] = new PneumaticSlotMonitor(slot, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PneumaticStorageMonoBlockEntity storage) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, storage);
        storage.refreshNode();
        boolean available = storage.isAvailable();
        if (available != storage.wasAvailable) {
            storage.wasAvailable = available;
            for (PneumaticSlotMonitor monitor : storage.monitors) monitor.availabilityHasChanged();
        }
        PneumaticNetwork network = storage.getRelevantNetwork();
        if (network != null) network.addStorage(storage);
        storage.consumeLegacyIdleAir();
        storage.updateMonitors();
        storage.networkPackNT(15);
    }

    public ItemStackHandler getFilters() { return filters; }
    public int getAmount(int slot) { return slot >= 0 && slot < SLOT_COUNT ? amounts[slot] : 0; }
    public void setClientAmount(int slot, int amount) { if (slot >= 0 && slot < SLOT_COUNT) amounts[slot] = Math.max(0, amount); }
    public HbmFluidTank compair() { return getAllTanks().get(0); }
    public boolean isAvailable() { return !isRemoved() && level != null && compair().getFill() > 0; }

    @Override public boolean canConnectPneumatic(Direction side) { return side != null; }
    @Override public List<HbmFluidTank> getReceivingTanks() { return List.of(compair()); }
    @Override public long getReceiverSpeed(FluidType type, int pressure) {
        return type == HbmFluids.AIR && pressure == compair().getPressure() ? compair().getSpace() : 0L;
    }
    @Override protected boolean shouldSubscribeAsFluidReceiver(FluidType type) { return type == HbmFluids.AIR; }
    @Override protected boolean shouldCreateFluidNode() { return false; }
    @Override protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        if (type != HbmFluids.AIR) return List.of();
        List<FluidPort> ports = new ArrayList<>();
        for (Direction side : Direction.values()) ports.add(FluidPort.of(side.getStepX(), side.getStepY(), side.getStepZ(), side));
        return ports;
    }
    @Override protected boolean shouldRefreshFluidNetworkSubscriptionsNow() {
        return level != null && level.getGameTime() % 10L == 0L;
    }
    @Override public boolean canConnectFluid(FluidType type, Direction side) { return type == HbmFluids.AIR && side != null; }
    @Override public PneumaticSlotMonitor[] getMonitors() { return monitors; }
    @Override public ItemStack getSlotAt(int index) { return index >= 0 && index < SLOT_COUNT ? filters.getStackInSlot(index) : ItemStack.EMPTY; }
    @Override public long getAmountAt(int index) { return getAmount(index); }
    @Override public boolean allowTypeSetting() { return false; }
    @Override public long useUpItem(int index, long amount) {
        if (amount <= 0 || index < 0 || index >= SLOT_COUNT) return Math.max(0L, amount);
        int removed = (int) Math.min(amount, amounts[index]);
        amounts[index] -= removed;
        if (removed > 0) setChangedAndUpdate();
        return amount - removed;
    }
    @Override public long addItem(int index, long amount) {
        if (amount <= 0 || index < 0 || index >= SLOT_COUNT || filters.getStackInSlot(index).isEmpty()) return Math.max(0L, amount);
        int added = (int) Math.min(amount, CAPACITY - amounts[index]);
        if (added > 0) { amounts[index] += added; setChangedAndUpdate(); }
        return amount - Math.max(added, 0);
    }
    @Override public long setupType(int index, ItemStack stack, long amount) { return amount; }
    @Override public boolean isAvailableToCache(PneumaticStackCache cache) {
        if (!isAvailable() || cache == null) return false;
        BlockPos cachePos = cache.getPos();
        long dx = worldPosition.getX() - cachePos.getX(), dy = worldPosition.getY() - cachePos.getY(), dz = worldPosition.getZ() - cachePos.getZ();
        int range = PneumaticUtil.rangeForPressure(compair().getPressure());
        return dx * dx + dy * dy + dz * dz <= (long) range * range;
    }
    @Override public PneumaticNetwork getRelevantNetwork() { return node == null ? null : node.getPneumaticNet(); }
    @Override public boolean hasExpired() { return !isAvailable() || node == null || node.isExpired(); }

    @Override public Component getDisplayName() { return Component.translatableWithFallback("container.pneumoStorageMono", "Pneumatic Storage Mono"); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new PneumaticStorageMonoMenu(id, inventory, this); }
    @Override public boolean hasPermission(ServerPlayer player) { return HbmGuiControlSecurity.hasLegacyMachineUsePermission(player, this); }
    @Override public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("pressure")) {
            int pressure = compair().getPressure() + 1;
            if (pressure > HbmFluidTank.HIGHEST_VALID_PRESSURE) pressure = 1;
            compair().setTankType(HbmFluids.AIR);
            compair().withPressure(pressure);
            for (PneumaticSlotMonitor monitor : monitors) monitor.availabilityHasChanged();
        }
    }

    public void loadFromPlacedStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            String itemKey = "slot" + slot;
            if (tag.contains(itemKey, Tag.TAG_COMPOUND)) filters.setStackInSlot(slot, ItemStack.of(tag.getCompound(itemKey)));
            amounts[slot] = Math.max(0, Math.min(CAPACITY, tag.getInt("amount" + slot)));
        }
        setChangedAndUpdate();
    }

    public ItemStack createDroppedStack(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack filter = filters.getStackInSlot(slot);
            if (!filter.isEmpty()) tag.put("slot" + slot, filter.save(new CompoundTag()));
            tag.putInt("amount" + slot, amounts[slot]);
        }
        if (!tag.isEmpty()) stack.setTag(tag);
        return stack;
    }

    public void clearForRemoval() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) { filters.setStackInSlot(slot, ItemStack.EMPTY); amounts[slot] = 0; }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag); tag.put(TAG_ITEMS, filters.serializeNBT()); tag.putIntArray(TAG_AMOUNTS, amounts);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag); if (tag.contains(TAG_ITEMS)) filters.deserializeNBT(tag.getCompound(TAG_ITEMS));
        int[] saved = tag.getIntArray(TAG_AMOUNTS);
        for (int slot = 0; slot < SLOT_COUNT; slot++) amounts[slot] = slot < saved.length ? Math.max(0, Math.min(CAPACITY, saved[slot])) : 0;
    }
    @Override public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag(); tag.put(TAG_ITEMS, filters.serializeNBT()); tag.putIntArray(TAG_AMOUNTS, amounts); return tag;
    }
    @Override public void handleClientSyncTag(CompoundTag tag) { load(tag); }

    @Override public CompoundTag getSettings(Level level, BlockPos pos) {
        CompoundTag tag = new CompoundTag(); ListTag copied = new ListTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) if (!filters.getStackInSlot(slot).isEmpty()) {
            CompoundTag entry = filters.getStackInSlot(slot).save(new CompoundTag()); entry.putByte(TAG_SETTINGS_SLOT, (byte) slot); copied.add(entry);
        }
        if (!copied.isEmpty()) tag.put(TAG_SETTINGS_ITEMS, copied); return tag;
    }
    @Override public void pasteSettings(CompoundTag tag, int index, Level level, Player player, BlockPos pos) {
        if (tag == null) return;
        for (Tag raw : tag.getList(TAG_SETTINGS_ITEMS, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw; int slot = entry.getByte(TAG_SETTINGS_SLOT);
            if (slot >= 0 && slot < SLOT_COUNT) filters.setStackInSlot(slot, ItemStack.of(entry));
        }
        setChangedAndUpdate();
    }

    @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return side == null ? itemCapability.cast() : LazyOptional.empty();
        return super.getCapability(cap, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); itemCapability.invalidate(); }
    @Override public void setRemoved() { removeNode(); super.setRemoved(); }
    @Override public void onChunkUnloaded() { super.onChunkUnloaded(); detachFromPneumaticNetwork(); }

    private void refreshNode() {
        if (level != null && !level.isClientSide && (node == null || node.isExpired()))
            node = PneumaticNodespace.createNode(level, new PneumaticNode(worldPosition, Set.of(Direction.values())));
    }
    private void removeNode() {
        detachFromPneumaticNetwork(); if (level != null && !level.isClientSide) PneumaticNodespace.destroyNode(level, worldPosition); node = null;
    }
    private void detachFromPneumaticNetwork() {
        for (PneumaticSlotMonitor monitor : monitors) monitor.detachFromCaches();
        PneumaticNetwork network = getRelevantNetwork(); if (network != null) network.removeStorage(this);
    }
    private void consumeLegacyIdleAir() {
        int fill = compair().getFill(); if (fill > 0) compair().setFill(Math.max(fill - ((int) Math.ceil((double) fill * 9.0D / compair().getMaxFill()) + 1), 0));
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
}
