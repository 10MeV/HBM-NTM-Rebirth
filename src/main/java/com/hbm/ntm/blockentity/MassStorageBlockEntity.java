package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.MassStorageBlock;
import com.hbm.ntm.item.KeyPinItem;
import com.hbm.ntm.item.PadlockItem;
import com.hbm.ntm.menu.MassStorageMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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
import java.util.List;

public class MassStorageBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyControlReceiver {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FILTER = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final String LEGACY_STACK_TAG = "stack";
    public static final String LEGACY_OUTPUT_TAG = "output";
    public static final String LEGACY_CAPACITY_TAG = "capacity";
    public static final String LEGACY_REDSTONE_TAG = "redstone";
    public static final String LEGACY_LOCK_TAG = "lock";
    public static final String LEGACY_LOCKED_TAG = "isLocked";
    public static final String LEGACY_LOCK_MOD_TAG = "lockMod";
    public static final String LEGACY_CHEESABLE_TAG = "cheesable";

    private final ItemStackHandler items = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == SLOT_INPUT) {
                return type().isEmpty() || HbmItemStackUtil.areStacksCompatible(type(), stack);
            }
            return slot == SLOT_FILTER;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndUpdate();
        }
    };
    private final LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> new IItemHandler() {
        @Override
        public int getSlots() {
            return 2;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(SLOT_INPUT) : items.getStackInSlot(SLOT_OUTPUT);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (locked) {
                return stack;
            }
            return slot == 0 ? items.insertItem(SLOT_INPUT, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (locked) {
                return ItemStack.EMPTY;
            }
            return slot == 1 ? items.extractItem(SLOT_OUTPUT, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return !locked && slot == 0 && items.isItemValid(SLOT_INPUT, stack);
        }
    });

    private int stockpile;
    private boolean output;
    private int capacity;
    private int redstone;
    private int lockPins;
    private boolean locked;
    private double lockMod = 0.1D;
    private boolean cheesable = true;

    public MassStorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MASS_STORAGE.get(), pos, state);
        this.capacity = MassStorageBlock.capacity(state.hasProperty(MassStorageBlock.VARIANT)
                ? state.getValue(MassStorageBlock.VARIANT) : 0);
    }

    public ItemStackHandler items() {
        return items;
    }

    public int stockpile() {
        return stockpile;
    }

    public int capacity() {
        return capacity <= 0 ? MassStorageBlock.capacity(0) : capacity;
    }

    public boolean output() {
        return output;
    }

    public int redstone() {
        return redstone;
    }

    public boolean isLocked() {
        return locked;
    }

    public ItemStack type() {
        return items.getStackInSlot(SLOT_FILTER).copy();
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        int newRedstone = capacity() <= 0 ? 0 : stockpile * 15 / capacity();
        if (newRedstone != redstone) {
            redstone = newRedstone;
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }

        if (type().isEmpty()) {
            stockpile = 0;
        }
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (canInsert(input)) {
            int amount = Math.min(capacity() - stockpile, input.getCount());
            input.shrink(amount);
            if (input.isEmpty()) {
                items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
            }
            stockpile += amount;
            setChangedAndUpdate();
        }
        if (output) {
            provide(type().isEmpty() ? 0 : type().getMaxStackSize());
        }
    }

    public boolean canInsert(ItemStack stack) {
        ItemStack type = type();
        return !type.isEmpty() && stockpile < capacity() && !stack.isEmpty()
                && HbmItemStackUtil.areStacksCompatible(type, stack);
    }

    public boolean quickInsert(ItemStack stack) {
        if (!canInsert(stack)) {
            return false;
        }
        int remaining = capacity() - stockpile;
        if (remaining < stack.getCount()) {
            return false;
        }
        stockpile += stack.getCount();
        stack.setCount(0);
        setChangedAndUpdate();
        return true;
    }

    public ItemStack quickExtract() {
        if (!output || type().isEmpty()) {
            return ItemStack.EMPTY;
        }
        int amount = type().getMaxStackSize();
        if (stockpile < amount) {
            return ItemStack.EMPTY;
        }
        ItemStack result = type().copyWithCount(amount);
        stockpile -= amount;
        setChangedAndUpdate();
        return result;
    }

    public int totalStockpile() {
        ItemStack type = type();
        if (type.isEmpty()) {
            return 0;
        }
        int result = stockpile;
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        if (!input.isEmpty() && HbmItemStackUtil.areStacksCompatible(type, input)) {
            result += input.getCount();
        }
        ItemStack outputStack = items.getStackInSlot(SLOT_OUTPUT);
        if (!outputStack.isEmpty() && HbmItemStackUtil.areStacksCompatible(type, outputStack)) {
            result += outputStack.getCount();
        }
        return result;
    }

    public int increaseTotalStockpile(int amount, boolean actually) {
        return changeTotalStockpile(amount, actually, 1);
    }

    public int decreaseTotalStockpile(int amount, boolean actually) {
        return changeTotalStockpile(amount, actually, -1);
    }

    private int changeTotalStockpile(int amount, boolean actually, int sign) {
        ItemStack type = type();
        if (type.isEmpty() || amount <= 0) {
            return amount;
        }

        int stockpileAvailable = sign > 0 ? capacity() - stockpile : stockpile;
        if (stockpileAvailable > 0) {
            int depositStockpile = Math.min(amount, stockpileAvailable);
            if (actually) {
                stockpile += sign * depositStockpile;
            }
            amount -= depositStockpile;
        }

        amount = changeLooseSlot(SLOT_INPUT, type, amount, actually, sign);
        amount = changeLooseSlot(SLOT_OUTPUT, type, amount, actually, sign);

        if (actually) {
            setChangedAndUpdate();
        }
        return amount;
    }

    private int changeLooseSlot(int slot, ItemStack type, int amount, boolean actually, int sign) {
        if (amount <= 0) {
            return 0;
        }
        ItemStack stack = items.getStackInSlot(slot);
        int available = 0;
        if (!stack.isEmpty() && HbmItemStackUtil.areStacksCompatible(type, stack)) {
            available = sign > 0 ? stack.getMaxStackSize() - stack.getCount() : stack.getCount();
        } else if (stack.isEmpty() && sign > 0) {
            available = type.getMaxStackSize();
        }
        if (available <= 0) {
            return amount;
        }
        int changed = Math.min(amount, available);
        if (actually) {
            if (sign > 0) {
                if (stack.isEmpty()) {
                    items.setStackInSlot(slot, type.copyWithCount(changed));
                } else {
                    stack.grow(changed);
                }
            } else {
                stack.shrink(changed);
                if (stack.isEmpty()) {
                    items.setStackInSlot(slot, ItemStack.EMPTY);
                }
            }
        }
        return amount - changed;
    }

    public void provide(int requested) {
        ItemStack type = type();
        if (requested <= 0 || stockpile <= 0 || type.isEmpty()) {
            return;
        }
        ItemStack current = items.getStackInSlot(SLOT_OUTPUT);
        if (!current.isEmpty() && !HbmItemStackUtil.areStacksCompatible(current, type)) {
            return;
        }
        int amount = Math.min(requested, stockpile);
        if (current.isEmpty()) {
            amount = Math.min(amount, type.getMaxStackSize());
            items.setStackInSlot(SLOT_OUTPUT, type.copyWithCount(amount));
        } else {
            amount = Math.min(amount, current.getMaxStackSize() - current.getCount());
            if (amount <= 0) {
                return;
            }
            current.grow(amount);
        }
        stockpile -= amount;
        setChangedAndUpdate();
    }

    public void setFilter(ItemStack stack) {
        if (stockpile > 0) {
            return;
        }
        items.setStackInSlot(SLOT_FILTER, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
    }

    public boolean tryApplyPadlock(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof PadlockItem padlock) || locked || KeyPinItem.getPins(stack) == 0) {
            return false;
        }
        lockPins = KeyPinItem.getPins(stack);
        locked = true;
        lockMod = padlock.lockMod();
        setChangedAndUpdate();
        if (player != null && level != null) {
            LegacySoundPlayer.playSoundAtPlayer(player, "hbm:block.lockHang", 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return true;
    }

    public boolean tryCreateCounterfeitKeys(Player player, InteractionHand hand) {
        if (!locked || player == null) {
            return false;
        }
        if (!cheesable) {
            player.displayClientMessage(Component.literal(
                    "This lock is too elaborate for a counterfeit key to be made"), false);
            player.displayClientMessage(Component.literal(
                    "Perhaps there is another way around here to unlock it"), false);
            return true;
        }
        ItemStack first = new ItemStack(ModItems.KEY_FAKE.get());
        KeyPinItem.setPins(first, lockPins);
        ItemStack second = first.copy();
        player.setItemInHand(hand, first);
        if (!player.getInventory().add(second)) {
            player.drop(second, false);
        }
        player.swing(hand, true);
        return true;
    }

    public boolean canAccess(Player player, ItemStack held) {
        if (!locked) {
            return true;
        }
        if (!held.isEmpty() && (held.is(ModItems.KEY.get()) || held.is(ModItems.KEY_FAKE.get()))
                && KeyPinItem.getPins(held) == lockPins) {
            LegacySoundPlayer.playSoundAtPlayer(player, "hbm:block.lockOpen", 1.0F, 1.0F);
            return true;
        }
        return tryPick(player, held);
    }

    public void playOpenSound() {
        if (level != null && !level.isClientSide) {
            LegacySoundPlayer.playLegacyStorageOpen(level, worldPosition, 0.5F, 1.0F);
        }
    }

    public void playCloseSound() {
        if (level != null && !level.isClientSide) {
            LegacySoundPlayer.playLegacyStorageClose(level, worldPosition, 0.5F, 1.0F);
        }
    }

    public ItemStack createDroppedStack() {
        ItemStack stack = new ItemStack(getBlockState().getBlock());
        stack.getOrCreateTag().putInt(com.hbm.ntm.item.LegacyStateBlockItem.TAG_VARIANT,
                getBlockState().getValue(MassStorageBlock.VARIANT));
        saveToItemStack(stack);
        return stack;
    }

    public void saveToItemStack(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean wrote = false;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack content = items.getStackInSlot(slot);
            if (!content.isEmpty()) {
                tag.put("slot" + slot, content.save(new CompoundTag()));
                wrote = true;
            }
        }
        if (stockpile > 0) {
            tag.putInt(LEGACY_STACK_TAG, stockpile);
            wrote = true;
        }
        if (output) {
            tag.putBoolean(LEGACY_OUTPUT_TAG, true);
            wrote = true;
        }
        if (locked) {
            tag.putInt(LEGACY_LOCK_TAG, lockPins);
            tag.putDouble(LEGACY_LOCK_MOD_TAG, lockMod);
            wrote = true;
        }
        tag.putInt(LEGACY_CAPACITY_TAG, capacity());
        if (!wrote && tag.size() == 1 && tag.contains(com.hbm.ntm.item.LegacyStateBlockItem.TAG_VARIANT)) {
            return;
        }
    }

    public void loadFromPlacedStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        for (int slot = 0; slot < items.getSlots(); slot++) {
            items.setStackInSlot(slot, tag.contains("slot" + slot, net.minecraft.nbt.Tag.TAG_COMPOUND)
                    ? ItemStack.of(tag.getCompound("slot" + slot)) : ItemStack.EMPTY);
        }
        stockpile = tag.getInt(LEGACY_STACK_TAG);
        output = tag.getBoolean(LEGACY_OUTPUT_TAG);
        if (tag.contains(LEGACY_CAPACITY_TAG)) {
            capacity = tag.getInt(LEGACY_CAPACITY_TAG);
        }
        if (tag.contains(LEGACY_LOCK_TAG)) {
            lockPins = tag.getInt(LEGACY_LOCK_TAG);
            lockMod = tag.contains(LEGACY_LOCK_MOD_TAG) ? tag.getDouble(LEGACY_LOCK_MOD_TAG) : 0.1D;
            locked = true;
        } else {
            lockPins = 0;
            lockMod = 0.1D;
            locked = false;
        }
        setChangedAndUpdate();
    }

    public void clearForRemoval() {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        stockpile = 0;
        setChanged();
    }

    public List<ItemStack> getLooseDrops() {
        List<ItemStack> drops = new ArrayList<>();
        ItemStack input = items.getStackInSlot(SLOT_INPUT);
        ItemStack outputStack = items.getStackInSlot(SLOT_OUTPUT);
        if (!input.isEmpty()) {
            drops.add(input.copy());
            items.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
        }
        if (!outputStack.isEmpty()) {
            drops.add(outputStack.copy());
            items.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
        }
        setChanged();
        return drops;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.massStorage");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MassStorageMenu(containerId, inventory, this);
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmItemStackUtil.saveLegacyItemsToTag(tag, items);
        tag.putInt(LEGACY_STACK_TAG, stockpile);
        tag.putBoolean(LEGACY_OUTPUT_TAG, output);
        tag.putInt(LEGACY_CAPACITY_TAG, capacity());
        tag.putByte(LEGACY_REDSTONE_TAG, (byte) redstone);
        tag.putInt(LEGACY_LOCK_TAG, lockPins);
        tag.putBoolean(LEGACY_LOCKED_TAG, locked);
        tag.putDouble(LEGACY_LOCK_MOD_TAG, lockMod);
        tag.putBoolean(LEGACY_CHEESABLE_TAG, cheesable);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmItemStackUtil.loadLegacyItems(tag, items);
        stockpile = tag.getInt(LEGACY_STACK_TAG);
        output = tag.getBoolean(LEGACY_OUTPUT_TAG);
        capacity = tag.contains(LEGACY_CAPACITY_TAG) ? tag.getInt(LEGACY_CAPACITY_TAG)
                : MassStorageBlock.capacity(getBlockState().getValue(MassStorageBlock.VARIANT));
        if (capacity <= 0) {
            capacity = MassStorageBlock.capacity(0);
        }
        redstone = tag.getByte(LEGACY_REDSTONE_TAG) & 255;
        lockPins = tag.getInt(LEGACY_LOCK_TAG);
        locked = tag.getBoolean(LEGACY_LOCKED_TAG);
        lockMod = tag.contains(LEGACY_LOCK_MOD_TAG) ? tag.getDouble(LEGACY_LOCK_MOD_TAG) : 0.1D;
        cheesable = !tag.contains(LEGACY_CHEESABLE_TAG) || tag.getBoolean(LEGACY_CHEESABLE_TAG);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) < 400.0D;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("provide")) {
            provide(data.getBoolean("provide") ? Math.max(1, type().getMaxStackSize()) : 1);
        }
        if (data.contains("toggle")) {
            output = !output;
            setChangedAndUpdate();
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap,
            @Nullable net.minecraft.core.Direction side) {
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

    private boolean tryPick(Player player, ItemStack held) {
        if (player == null || level == null) {
            return false;
        }
        boolean canPick = false;
        double chanceOfSuccess = lockMod * 100.0D;

        if (!held.isEmpty() && held.is(ModItems.PIN.get()) && hasScrewdriver(player)) {
            held.shrink(1);
            canPick = true;
        } else if (!held.isEmpty() && held.is(ModItems.SCREWDRIVER.get()) && consumeOnePin(player)) {
            canPick = true;
        }

        if (!canPick) {
            return false;
        }

        if (isWearingLockpickJacket(player)) {
            chanceOfSuccess *= 100.0D;
        }

        if (chanceOfSuccess > level.random.nextDouble() * 100.0D) {
            LegacySoundPlayer.playSoundAtPlayer(player, "hbm:item.pinUnlock", 1.0F, 1.0F);
            return true;
        }

        LegacySoundPlayer.playSoundAtPlayer(player, "hbm:item.pinBreak", 1.0F,
                0.8F + level.random.nextFloat() * 0.2F);
        return false;
    }

    private static boolean hasScrewdriver(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.SCREWDRIVER.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean consumeOnePin(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.PIN.get())) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static boolean isWearingLockpickJacket(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.is(ModItems.JACKET.get()) || chest.is(ModItems.JACKET2.get());
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}
