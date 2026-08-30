package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.ntl.PneumaticConnector;
import com.hbm.ntm.api.ntl.ISlotMonitorProvider;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.PneumaticStorageClutterMenu;
import com.hbm.ntm.network.HbmGuiControlSecurity;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNetwork;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNode;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticSlotMonitor;
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

import java.util.List;
import java.util.Set;

public class PneumaticStorageClutterBlockEntity extends HbmFluidNetworkBlockEntity implements MenuProvider,
        PneumaticConnector, ISlotMonitorProvider, HbmStandardFluidReceiver, HbmLegacyControlReceiver {
    public static final int SLOT_COUNT = 54;
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_CUSTOM_NAME = "name";

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> items);
    private final PneumaticSlotMonitor[] monitors = new PneumaticSlotMonitor[SLOT_COUNT];
    private PneumaticNode node;
    private boolean wasAvailable;
    @Nullable
    private String customName;

    public PneumaticStorageClutterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PNEUMATIC_STORAGE_CLUTTER.get(), pos, state,
                List.of(new HbmFluidTank(HbmFluids.AIR, 4_000).withPressure(1)));
        for (int slot = 0; slot < monitors.length; slot++) {
            monitors[slot] = new PneumaticSlotMonitor(slot, this);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PneumaticStorageClutterBlockEntity storage) {
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, storage);
        storage.refreshNode();
        boolean available = storage.isAvailable();
        if (available != storage.wasAvailable) {
            storage.wasAvailable = available;
            for (PneumaticSlotMonitor monitor : storage.monitors) {
                monitor.availabilityHasChanged();
            }
        }
        PneumaticNetwork network = storage.getRelevantNetwork();
        if (network != null) {
            network.addStorage(storage);
        }
        storage.consumeLegacyIdleAir();
        storage.updateMonitors();
        storage.networkPackNT(15);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    /**
     * Uses the same direct {@code slot0..slot53} block-item layout as the
     * legacy PneumoStorageClutter and the existing crate persistence path.
     */
    public ItemStack createDroppedStack(net.minecraft.world.item.Item item) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack content = items.getStackInSlot(slot);
            if (!content.isEmpty()) {
                tag.put("slot" + slot, content.save(new CompoundTag()));
            }
        }
        if (!tag.isEmpty()) {
            stack.setTag(tag);
        }
        if (customName != null && !customName.isBlank()) {
            stack.setHoverName(Component.literal(customName));
        }
        return stack;
    }

    /** Restores the old populated block-item form on placement. */
    public void loadFromPlacedStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            items.setStackInSlot(slot, tag != null && tag.contains("slot" + slot, net.minecraft.nbt.Tag.TAG_COMPOUND)
                    ? ItemStack.of(tag.getCompound("slot" + slot))
                    : ItemStack.EMPTY);
        }
        customName = stack.hasCustomHoverName() ? stack.getHoverName().getString() : null;
        setChanged();
    }

    /** Prevents onRemove from duplicating the populated block item or spilling it after player harvest. */
    public void clearForRemoval() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        setChanged();
    }

    public HbmFluidTank compair() {
        return getAllTanks().get(0);
    }

    public boolean isAvailable() {
        return !isRemoved() && level != null && compair().getFill() > 0;
    }

    @Override
    public boolean canConnectPneumatic(Direction side) {
        return side != null;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(compair());
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return type == HbmFluids.AIR && pressure == compair().getPressure() ? compair().getSpace() : 0L;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == HbmFluids.AIR;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected Iterable<FluidPort> getNetworkFluidPorts(FluidType type) {
        if (type != HbmFluids.AIR) {
            return List.of();
        }
        List<FluidPort> ports = new java.util.ArrayList<>();
        for (Direction side : Direction.values()) {
            ports.add(FluidPort.of(side.getStepX(), side.getStepY(), side.getStepZ(), side));
        }
        return ports;
    }

    @Override
    protected boolean shouldRefreshFluidNetworkSubscriptionsNow() {
        // TileEntityPneumaticStorageBase called trySubscribe exactly on the
        // world-time % 10 pass, rather than using the generic dirty cadence.
        return level != null && level.getGameTime() % 10L == 0L;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return type == HbmFluids.AIR && side != null;
    }

    @Override
    public PneumaticSlotMonitor[] getMonitors() {
        return monitors;
    }

    @Override
    public ItemStack getSlotAt(int index) {
        return index >= 0 && index < SLOT_COUNT ? items.getStackInSlot(index) : ItemStack.EMPTY;
    }

    @Override
    public long getAmountAt(int index) {
        return getSlotAt(index).getCount();
    }

    @Override
    public long useUpItem(int index, long amount) {
        if (amount <= 0L || index < 0 || index >= SLOT_COUNT) {
            return Math.max(0L, amount);
        }
        ItemStack extracted = items.extractItem(index, (int) Math.min(Integer.MAX_VALUE, amount), false);
        return amount - extracted.getCount();
    }

    @Override
    public long addItem(int index, long amount) {
        if (amount <= 0L || index < 0 || index >= SLOT_COUNT) {
            return Math.max(0L, amount);
        }
        ItemStack current = items.getStackInSlot(index);
        if (current.isEmpty()) {
            return amount;
        }
        ItemStack offered = current.copy();
        offered.setCount((int) Math.min(Integer.MAX_VALUE, amount));
        return items.insertItem(index, offered, false).getCount();
    }

    @Override
    public boolean allowTypeSetting() {
        return true;
    }

    @Override
    public long setupType(int index, ItemStack stack, long amount) {
        if (amount <= 0L || index < 0 || index >= SLOT_COUNT || !items.getStackInSlot(index).isEmpty()) {
            return Math.max(0L, amount);
        }
        ItemStack offered = stack.copy();
        offered.setCount((int) Math.min(Integer.MAX_VALUE, amount));
        return items.insertItem(index, offered, false).getCount();
    }

    @Override
    public boolean isAvailableToCache(PneumaticStackCache cache) {
        if (!isAvailable() || cache == null) {
            return false;
        }
        BlockPos cachePos = cache.getPos();
        long dx = worldPosition.getX() - cachePos.getX();
        long dy = worldPosition.getY() - cachePos.getY();
        long dz = worldPosition.getZ() - cachePos.getZ();
        int range = PneumaticUtil.rangeForPressure(compair().getPressure());
        return dx * dx + dy * dy + dz * dz <= (long) range * range;
    }

    @Override
    public PneumaticNetwork getRelevantNetwork() {
        return node == null ? null : node.getPneumaticNet();
    }

    @Override
    public boolean hasExpired() {
        return !isAvailable() || node == null || node.isExpired();
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.pneumoStorageClutter", "Pneumatic Storage Clutter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PneumaticStorageClutterMenu(containerId, inventory, this);
    }

    @Override
    public boolean hasPermission(net.minecraft.server.level.ServerPlayer player) {
        return HbmGuiControlSecurity.hasLegacyMachineUsePermission(player, this);
    }

    @Override
    public void receiveControl(net.minecraft.server.level.ServerPlayer player, CompoundTag data) {
        if (!data.contains("pressure")) {
            return;
        }
        int pressure = compair().getPressure() + 1;
        if (pressure > HbmFluidTank.HIGHEST_VALID_PRESSURE) {
            pressure = 1;
        }
        compair().setTankType(HbmFluids.AIR);
        compair().withPressure(pressure);
        for (PneumaticSlotMonitor monitor : monitors) {
            monitor.availabilityHasChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ITEMS, items.serializeNBT());
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_ITEMS)) {
            items.deserializeNBT(tag.getCompound(TAG_ITEMS));
        }
        customName = tag.contains(TAG_CUSTOM_NAME) ? tag.getString(TAG_CUSTOM_NAME) : null;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemCapability.cast() : LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    public void setRemoved() {
        removeNode();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        detachFromPneumaticNetwork();
    }

    private void refreshNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (node == null || node.isExpired()) {
            node = PneumaticNodespace.createNode(level, new PneumaticNode(worldPosition, Set.of(Direction.values())));
        }
    }

    private void removeNode() {
        detachFromPneumaticNetwork();
        if (level != null && !level.isClientSide) {
            PneumaticNodespace.destroyNode(level, worldPosition);
        }
        node = null;
    }

    private void detachFromPneumaticNetwork() {
        for (PneumaticSlotMonitor monitor : monitors) {
            monitor.detachFromCaches();
        }
        PneumaticNetwork network = getRelevantNetwork();
        if (network != null) {
            network.removeStorage(this);
        }
    }

    private void consumeLegacyIdleAir() {
        int fill = compair().getFill();
        if (fill <= 0) {
            return;
        }
        int consumption = (int) Math.ceil((double) fill * 9.0D / compair().getMaxFill()) + 1;
        compair().setFill(Math.max(fill - consumption, 0));
    }
}
