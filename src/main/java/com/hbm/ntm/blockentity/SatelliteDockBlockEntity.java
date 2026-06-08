package com.hbm.ntm.blockentity;

import com.hbm.ntm.entity.missile.MinerRocketEntity;
import com.hbm.ntm.explosion.ExplosionNukeSmall;
import com.hbm.ntm.itempool.HbmItemPoolRegistry;
import com.hbm.ntm.menu.SatelliteDockMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public class SatelliteDockBlockEntity extends BlockEntity implements MenuProvider {
    public static final int OUTPUT_SLOT_COUNT = 15;
    public static final int SLOT_CHIP = 15;
    public static final int SLOT_COUNT = 16;
    private static final long CARGO_DELAY_MILLIS = 10L * 60L * 1000L;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_CHIP && stack.getItem() instanceof ISatelliteChip;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    public SatelliteDockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SAT_DOCK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SatelliteDockBlockEntity dock) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SatelliteSavedData data = SatelliteSavedData.get(serverLevel);
        ItemStack chip = dock.items.getStackInSlot(SLOT_CHIP);
        if (chip.getItem() instanceof ISatelliteChip) {
            int frequency = ISatelliteChip.getFrequencyFromStack(chip);
            Satellite satellite = data.getSatFromFreq(frequency);
            if (satellite != null && satellite.cargoPool().isPresent()
                    && satellite.lastOperationMillis() + CARGO_DELAY_MILLIS < System.currentTimeMillis()) {
                MinerRocketEntity rocket = new MinerRocketEntity(serverLevel);
                rocket.setPos(pos.getX() + 0.5D, 300.0D, pos.getZ() + 0.5D);
                rocket.setSatelliteFrequency(frequency);
                serverLevel.addFreshEntity(rocket);
                satellite.setLastOperationMillis(System.currentTimeMillis());
                data.markDirty();
            }
        }

        AABB landingBox = new AABB(
                pos.getX() + 0.25D, pos.getY() + 0.75D, pos.getZ() + 0.25D,
                pos.getX() + 0.75D, pos.getY() + 2.0D, pos.getZ() + 0.75D);
        for (MinerRocketEntity rocket : serverLevel.getEntitiesOfClass(MinerRocketEntity.class, landingBox)) {
            if (chip.getItem() instanceof ISatelliteChip) {
                int frequency = ISatelliteChip.getFrequencyFromStack(chip);
                if (frequency != rocket.satelliteFrequency()) {
                    rocket.discard();
                    ExplosionNukeSmall.explode(serverLevel, pos.getX() + 0.5D, pos.getY() + 0.5D,
                            pos.getZ() + 0.5D, ExplosionNukeSmall.PARAMS_TOTS);
                    break;
                }
                if (rocket.mode() == MinerRocketEntity.MODE_UNLOADING && rocket.timer() == 50) {
                    Satellite satellite = data.getSatFromFreq(frequency);
                    if (satellite != null) {
                        satellite.cargoPool().ifPresent(pool -> dock.unloadCargo(serverLevel, pool));
                    }
                }
            }
        }

        dock.ejectInto(pos.offset(2, 0, 0));
        dock.ejectInto(pos.offset(-2, 0, 0));
        dock.ejectInto(pos.offset(0, 0, 2));
        dock.ejectInto(pos.offset(0, 0, -2));
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    private void unloadCargo(ServerLevel level, String pool) {
        int itemAmount = level.random.nextInt(6) + 10;
        Vec3 origin = Vec3.atCenterOf(worldPosition);
        for (int i = 0; i < itemAmount; i++) {
            addToOutput(HbmItemPoolRegistry.getStack(level, pool, origin));
        }
    }

    private void addToOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            ItemStack current = items.getStackInSlot(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameTags(current, remaining)
                    && current.getCount() < current.getMaxStackSize()) {
                int toAdd = Math.min(current.getMaxStackSize() - current.getCount(), remaining.getCount());
                current.grow(toAdd);
                remaining.shrink(toAdd);
                if (remaining.isEmpty()) {
                    setChanged();
                    return;
                }
            }
        }

        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                ItemStack one = remaining.copy();
                one.setCount(1);
                items.setStackInSlot(slot, one);
                setChanged();
                return;
            }
        }
    }

    private void ejectInto(BlockPos targetPos) {
        if (level == null) {
            return;
        }
        BlockEntity target = level.getBlockEntity(targetPos);
        if (target == null) {
            return;
        }
        AtomicBoolean handledCapability = new AtomicBoolean(false);
        target.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            handledCapability.set(true);
            for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack single = stack.copy();
                single.setCount(1);
                ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, single, false);
                if (remainder.isEmpty()) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        items.setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    setChanged();
                    return;
                }
            }
        });
        if (handledCapability.get()) {
            return;
        }
        if (target instanceof Container container) {
            ejectIntoContainer(container);
        }
    }

    private void ejectIntoContainer(Container container) {
        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack single = stack.copy();
            single.setCount(1);
            for (int targetSlot = 0; targetSlot < container.getContainerSize(); targetSlot++) {
                ItemStack current = container.getItem(targetSlot);
                if (!current.isEmpty() && ItemStack.isSameItemSameTags(current, single)
                        && current.getCount() < current.getMaxStackSize()) {
                    current.grow(1);
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        items.setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    container.setChanged();
                    setChanged();
                    return;
                }
            }
        }

        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack single = stack.copy();
            single.setCount(1);
            for (int targetSlot = 0; targetSlot < container.getContainerSize(); targetSlot++) {
                if (container.getItem(targetSlot).isEmpty() && container.canPlaceItem(targetSlot, single)) {
                    container.setItem(targetSlot, single);
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        items.setStackInSlot(slot, ItemStack.EMPTY);
                    }
                    container.setChanged();
                    setChanged();
                    return;
                }
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_ntm_rebirth.sat_dock");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SatelliteDockMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag inventory = HbmInventoryMenuHelper.saveLegacyItems(items);
        tag.put(HbmInventoryMenuHelper.LEGACY_ITEMS_TAG,
                inventory.getList(HbmInventoryMenuHelper.LEGACY_ITEMS_TAG, net.minecraft.nbt.Tag.TAG_COMPOUND));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyItems(tag, items);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }
}
