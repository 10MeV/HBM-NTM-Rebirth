package com.hbm.ntm.blockentity;

import com.hbm.ntm.drone.DroneFilter;
import com.hbm.ntm.drone.DroneLogisticsNetwork;
import com.hbm.ntm.entity.item.RequestDroneEntity;
import com.hbm.ntm.item.DroneItem;
import com.hbm.ntm.menu.DroneLogisticsMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.HbmItemStackUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Dock/provider/requester inventory and legacy once-per-second request-network announcement. */
public class DroneLogisticsBlockEntity extends BlockEntity implements MenuProvider {
    private final Kind kind;
    /**
     * Legacy dock/provider/requester code changed its backing slot array as one operation.
     * Keep the modern ItemStackHandler callback for normal menu/capability edits, but avoid
     * broadcasting intermediate states while a drone is atomically launched or returned.
     */
    private boolean batchingInventoryChange;
    private final ItemStackHandler items;
    private final LazyOptional<IItemHandler> itemCapability;
    private final DroneFilter[] filters = new DroneFilter[9];

    public DroneLogisticsBlockEntity(BlockPos pos, BlockState state) { this(pos, state, kindFromState(state)); }
    public DroneLogisticsBlockEntity(BlockPos pos, BlockState state, Kind kind) {
        super(ModBlockEntities.DRONE_LOGISTICS.get(), pos, state);
        this.kind = kind;
        this.items = new ItemStackHandler(kind.slots) {
            @Override protected void onContentsChanged(int slot) {
                if (!batchingInventoryChange) {
                    setChangedAndSync();
                }
            }
        };
        this.itemCapability = LazyOptional.of(() -> switch (kind) {
            case REQUESTER -> new RequesterExternalItemHandler(items);
            case PROVIDER -> new ProviderExternalItemHandler(items);
            case DOCK -> NoExternalItemHandler.INSTANCE;
        });
    }
    public Kind kind() { return kind; }
    public ItemStackHandler items() { return items; }
    public DroneFilter filter(int slot) { return filters[slot]; }
    /** Standard initialization is owned by the shared legacy pattern-matcher contract. */
    public void setFilter(int slot, ItemStack stack) { filters[slot] = stack.isEmpty() ? null : new DroneFilter(stack, null); setChangedAndSync(); }
    public void clearFilter(int slot) { filters[slot] = null; setChangedAndSync(); }
    public void cycleFilter(int slot) { if (filters[slot] != null) { filters[slot].cycleMode(); setChangedAndSync(); } }

    public static void tick(Level level, DroneLogisticsBlockEntity entity) {
        if (!level.isClientSide && level.getGameTime() % 20L == 0L) entity.publish((ServerLevel) level);
    }
    private void publish(ServerLevel level) {
        DroneLogisticsNetwork network = DroneLogisticsNetwork.forLevel(level);
        DroneLogisticsNetwork.Node node = network.publish(level, worldPosition.above(), kind.nodeKind,
                !level.hasNeighborSignal(worldPosition), offers(), requests());
        if (kind == Kind.DOCK && node.active()) dispatch(level, network, node);
    }
    private List<ItemStack> offers() {
        if (kind != Kind.PROVIDER) return List.of();
        List<ItemStack> offer = new ArrayList<>();
        for (int i = 0; i < 9; i++) offer.add(items.getStackInSlot(i));
        return offer;
    }
    private List<DroneFilter> requests() {
        if (kind != Kind.REQUESTER) return List.of();
        List<DroneFilter> requested = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            DroneFilter filter = filters[i]; ItemStack stock = items.getStackInSlot(i + 9);
            if (filter != null && (stock.isEmpty() || !filter.matches(stock))) requested.add(filter);
        }
        return requested;
    }
    private void dispatch(ServerLevel level, DroneLogisticsNetwork network, DroneLogisticsNetwork.Node dock) {
        int droneSlot = requestDroneSlot();
        if (droneSlot < 0) return;
        ObjectArrayList<DroneLogisticsNetwork.Node> requests = new ObjectArrayList<>();
        ObjectArrayList<DroneLogisticsNetwork.Node> offers = new ObjectArrayList<>();
        Set<Long> pathNodeSnapshot = new HashSet<>();
        for (DroneLogisticsNetwork.Node node : network.localNodes(worldPosition, 5)) {
            pathNodeSnapshot.add(node.pos().asLong());
            if (node.kind() == DroneLogisticsNetwork.NodeKind.REQUESTER) requests.add(node);
            if (node.kind() == DroneLogisticsNetwork.NodeKind.PROVIDER) offers.add(node);
        }
        for (int attempt = 0; attempt < 5; attempt++) {
            Util.shuffle(requests, level.random); Util.shuffle(offers, level.random);
            DroneLogisticsNetwork.Node request = requests.stream().filter(node -> node.active() && !node.request().isEmpty()).findFirst().orElse(null);
            if (request == null) return;
            DroneFilter wanted = request.request().get(level.random.nextInt(request.request().size()));
            DroneLogisticsNetwork.Node offer = offers.stream().filter(node -> node.active() && node.offer().stream().anyMatch(wanted::matches)).findFirst().orElse(null);
            if (offer == null) continue;
            // TileEntityDroneDock supplied this same dock-centered local-node snapshot for
            // all three legs; rebuilding it around offer/request changes the legal route.
            List<BlockPos> toOffer = network.findPath(dock, offer, pathNodeSnapshot), toRequest = network.findPath(offer, request, pathNodeSnapshot), home = network.findPath(request, dock, pathNodeSnapshot);
            if (toOffer == null || toRequest == null || home == null) continue;
            batchingInventoryChange = true;
            try {
                items.extractItem(droneSlot, 1, false);
            } finally {
                batchingInventoryChange = false;
            }
            RequestDroneEntity drone = new RequestDroneEntity(ModEntityTypes.REQUEST_DRONE.get(), level);
            drone.moveTo(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, 0.0F, 0.0F);
            drone.addRoute(toOffer); drone.addPosition(offer.pos()); drone.addPickup(wanted); drone.addRoute(toRequest); drone.addPosition(request.pos());
            drone.addUnload(); drone.addRoute(home); drone.addPosition(dock.pos()); drone.addDock();
            level.addFreshEntity(drone);
            level.playSound(null, worldPosition, ModSounds.BLOCK_STORAGE_OPEN.get(), net.minecraft.sounds.SoundSource.BLOCKS, 2.0F, 1.0F);
            setChangedAndSync(); return;
        }
    }
    private int requestDroneSlot() {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.is(ModItems.DRONE.get()) && DroneItem.typeOf(stack) == DroneItem.DroneType.REQUEST) return i;
        }
        return -1;
    }
    public boolean insertRequestDrone(ItemStack drone, ItemStack held) {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack docked = items.getStackInSlot(i);
            // EntityRequestDrone used ItemStack#isItemEqual here: the drone's legacy
            // metadata selected REQUEST, while arbitrary NBT on the stored item did not
            // prevent the stack from accepting a returning request drone.  In the modern
            // item this variant lives in the droneType tag, so ItemStackHandler#insertItem
            // would be too strict: its normal stackability test also compares tag/component
            // data unrelated to the legacy item+metadata identity.
            boolean emptySlot = docked.isEmpty();
            boolean matchingRequestDrone = !emptySlot
                    && docked.is(drone.getItem())
                    && DroneItem.typeOf(docked) == DroneItem.DroneType.REQUEST
                    && docked.getCount() < docked.getMaxStackSize();
            if (!emptySlot && !matchingRequestDrone) continue;
            // Legacy docked cargo only in the following empty slot. Do not consume either
            // stack unless that exact handoff is possible, otherwise the drone drops both.
            int cargoSlot = i + 1;
            if (!held.isEmpty() && (cargoSlot >= items.getSlots() || !items.getStackInSlot(cargoSlot).isEmpty())) continue;
            batchingInventoryChange = true;
            try {
                if (emptySlot) items.setStackInSlot(i, drone.copy());
                else {
                    docked.grow(1);
                    items.setStackInSlot(i, docked);
                }
                if (!held.isEmpty()) items.setStackInSlot(cargoSlot, held.copy());
            } finally {
                batchingInventoryChange = false;
            }
            setChangedAndSync(); return true;
        }
        return false;
    }
    /** DroneDock dropped only concrete inventory slots; requester filters remain ghost data. */
    public void dropContents() {
        if (level == null) return;
        int firstSlot = kind == Kind.REQUESTER ? 9 : 0;
        int endExclusive = kind == Kind.REQUESTER ? 18 : 9;
        for (int slot = firstSlot; slot < endExclusive; slot++) {
            HbmItemStackUtil.spillStack(level, worldPosition, items.getStackInSlot(slot));
        }
    }
    public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5) <= 128; }
    @Override public Component getDisplayName() { return Component.translatable(kind.titleKey); }
    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new DroneLogisticsMenu(id, inventory, this); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.put("items", items.serializeNBT()); for(int i=0;i<9;i++) if(filters[i]!=null) tag.put("filter"+i, filters[i].save()); }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        batchingInventoryChange = true;
        try {
            if (tag.contains("items")) items.deserializeNBT(tag.getCompound("items"));
        } finally {
            batchingInventoryChange = false;
        }
        for (int i = 0; i < 9; i++) {
            filters[i] = tag.contains("filter" + i) ? DroneFilter.load(tag.getCompound("filter" + i)) : null;
        }
    }
    @Override public CompoundTag getUpdateTag() { CompoundTag tag=super.getUpdateTag(); saveAdditional(tag); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag) { load(tag); }
    @Override public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) { return cap==ForgeCapabilities.ITEM_HANDLER ? itemCapability.cast() : super.getCapability(cap, side); }
    @Override public void invalidateCaps() { super.invalidateCaps(); itemCapability.invalidate(); }
    private void setChangedAndSync() { setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    private static Kind kindFromState(BlockState state) {
        if (state.getBlock() instanceof com.hbm.ntm.block.DroneLogisticsBlock block) return block.kind();
        return Kind.DOCK;
    }
    /** Legacy requester sided inventory: stock slots 9-17 are extract-only; filters are GUI-only. */
    private static final class RequesterExternalItemHandler implements IItemHandler {
        private final ItemStackHandler delegate;
        private RequesterExternalItemHandler(ItemStackHandler delegate) { this.delegate = delegate; }
        @Override public int getSlots() { return 9; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return valid(slot) ? delegate.getStackInSlot(slot + 9) : ItemStack.EMPTY; }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return valid(slot) ? delegate.extractItem(slot + 9, amount, simulate) : ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return valid(slot) ? delegate.getSlotLimit(slot + 9) : 0; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
        private boolean valid(int slot) { return slot >= 0 && slot < 9; }
    }
    /** TileEntityDroneProvider allowed insertion but explicitly rejected every sided extraction. */
    private static final class ProviderExternalItemHandler implements IItemHandler {
        private final ItemStackHandler delegate;
        private ProviderExternalItemHandler(ItemStackHandler delegate) { this.delegate = delegate; }
        @Override public int getSlots() { return delegate.getSlots(); }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return valid(slot) ? delegate.getStackInSlot(slot) : ItemStack.EMPTY; }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return valid(slot) ? delegate.insertItem(slot, stack, simulate) : stack; }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return valid(slot) ? delegate.getSlotLimit(slot) : 0; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return valid(slot) && delegate.isItemValid(slot, stack); }
        private boolean valid(int slot) { return slot >= 0 && slot < delegate.getSlots(); }
    }
    /** TileEntityDroneDock inherited an empty sided inventory; its drone slots are player GUI only. */
    private enum NoExternalItemHandler implements IItemHandler {
        INSTANCE;
        @Override public int getSlots() { return 0; }
        @Override public @NotNull ItemStack getStackInSlot(int slot) { return ItemStack.EMPTY; }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) { return stack; }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 0; }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) { return false; }
    }
    public enum Kind { DOCK(9,"container.droneDock",DroneLogisticsNetwork.NodeKind.DOCK), PROVIDER(9,"container.droneProvider",DroneLogisticsNetwork.NodeKind.PROVIDER), REQUESTER(18,"container.droneRequester",DroneLogisticsNetwork.NodeKind.REQUESTER); final int slots; final String titleKey; final DroneLogisticsNetwork.NodeKind nodeKind; Kind(int slots,String titleKey,DroneLogisticsNetwork.NodeKind nodeKind){this.slots=slots;this.titleKey=titleKey;this.nodeKind=nodeKind;} }
}
