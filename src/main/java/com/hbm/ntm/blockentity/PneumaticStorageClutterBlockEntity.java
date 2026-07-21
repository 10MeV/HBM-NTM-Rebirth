package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.ntl.PneumaticConnector;
import com.hbm.ntm.api.ntl.ISlotMonitorProvider;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.menu.PneumaticStorageClutterMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNetwork;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNode;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticNodespace;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticSlotMonitor;
import com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticStackCache;
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
        PneumaticConnector, ISlotMonitorProvider, HbmStandardFluidReceiver {
    public static final int SLOT_COUNT = 54;
    private static final String TAG_ITEMS = "Items";

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
        storage.updateMonitors();
        storage.networkPackNT(15);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank compair() {
        return getAllTanks().get(0);
    }

    public boolean isAvailable() {
        return !isRemoved() && level != null;
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
        return false;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
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
    public boolean isAvailableToCache(PneumaticStackCache cache) {
        return isAvailable();
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
        return Component.translatableWithFallback("container.pneumoStorageClutter", "Pneumatic Storage Clutter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PneumaticStorageClutterMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_ITEMS, items.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_ITEMS)) {
            items.deserializeNBT(tag.getCompound(TAG_ITEMS));
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
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
}
