package com.hbm.ntm.entity.train;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** 1.7.10 EntityRailCarCargo inventory/NBT contract. */
public abstract class LegacyRailCarCargoEntity extends LegacyRailCarEntity implements Container {
    private static final String ITEMS_KEY = "Items";
    private static final String SLOT_KEY = "Slot";
    private static final EntityDataAccessor<Integer> OCCUPIED_SLOTS = SynchedEntityData.defineId(
            LegacyRailCarCargoEntity.class, EntityDataSerializers.INT);
    private final ItemStackHandler items;
    private final LazyOptional<IItemHandler> itemCapability;

    protected LegacyRailCarCargoEntity(EntityType<? extends LegacyRailCarCargoEntity> type, Level level) {
        super(type, level);
        items = new ItemStackHandler(getContainerSize());
        itemCapability = LazyOptional.of(() -> items);
    }

    public ItemStackHandler items() {
        return items;
    }

    public int countOccupiedSlots() {
        int count = 0;
        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (!items.getStackInSlot(slot).isEmpty()) count++;
        }
        return count;
    }

    public int getOccupiedSlots() { return entityData.get(OCCUPIED_SLOTS); }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OCCUPIED_SLOTS, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) entityData.set(OCCUPIED_SLOTS, countOccupiedSlots());
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (!items.getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return items.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = items.getStackInSlot(slot);
        items.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty() && copy.getCount() > getMaxStackSize()) {
            copy.setCount(getMaxStackSize());
        }
        items.setStackInSlot(slot, copy);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return !isRemoved() && player.distanceToSqr(this) <= 64.0D;
    }

    @Override
    public void clearContent() {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag serialized = new ListTag();
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                CompoundTag entry = stack.save(new CompoundTag());
                entry.putByte(SLOT_KEY, (byte) slot);
                serialized.add(entry);
            }
        }
        tag.put(ITEMS_KEY, serialized);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        clearContent();
        ListTag serialized = tag.getList(ITEMS_KEY, Tag.TAG_COMPOUND);
        for (int index = 0; index < serialized.size(); index++) {
            CompoundTag entry = serialized.getCompound(index);
            int slot = entry.getByte(SLOT_KEY) & 255;
            if (slot >= 0 && slot < getContainerSize()) {
                items.setStackInSlot(slot, ItemStack.of(entry));
            }
        }
        // Legacy readEntityFromNBT refreshed data-watcher slot 10 immediately.
        entityData.set(OCCUPIED_SLOTS, countOccupiedSlots());
    }
}
