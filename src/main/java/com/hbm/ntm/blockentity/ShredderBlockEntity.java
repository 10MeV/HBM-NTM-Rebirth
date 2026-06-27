package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.IBatteryItem;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.item.ShredderBladeItem;
import com.hbm.ntm.menu.ShredderMenu;
import com.hbm.ntm.recipe.ItemProcessingRecipe;
import com.hbm.ntm.recipe.ItemProcessingRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShredderBlockEntity extends HbmEnergyBlockEntity
        implements MenuProvider {
    private static final String TAG_ITEMS = "items";
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_POWER = "powerTime";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_NAME = "name";

    public static final long MAX_POWER = 10_000L;
    public static final int PROCESSING_SPEED = 60;
    private static final long CONSUMPTION = 5L;

    public static final int SLOT_INPUT_START = 0;
    public static final int SLOT_INPUT_END = 8;
    public static final int SLOT_OUTPUT_START = 9;
    public static final int SLOT_OUTPUT_END = 26;
    public static final int SLOT_BLADE_LEFT = 27;
    public static final int SLOT_BLADE_RIGHT = 28;
    public static final int SLOT_BATTERY = 29;
    public static final int SLOT_COUNT = 30;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END) {
                return !(stack.getItem() instanceof ShredderBladeItem)
                        && ItemProcessingRecipeRuntime.find(level, ItemProcessingRecipe.Machine.SHREDDER, stack) != null;
            }
            if (slot == SLOT_BATTERY) {
                return isLegacyBattery(stack);
            }
            if (slot == SLOT_BLADE_LEFT || slot == SLOT_BLADE_RIGHT) {
                return stack.getItem() instanceof ShredderBladeItem;
            }
            return false;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };

    private final LazyOptional<IItemHandler> automationItems = LazyOptional.of(AccessibleItemHandler::new);
    private int progress;
    private int soundCycle;
    private String customName;

    public ShredderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_SHREDDER.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ShredderBlockEntity shredder) {
        if (level.isClientSide) {
            return;
        }

        long oldPower = shredder.energy.getPower();
        int oldProgress = shredder.progress;
        shredder.subscribeEnergyReceiverToAllSides();
        HbmEnergyUtil.chargeStorageFromItem(shredder.items.getStackInSlot(SLOT_BATTERY), shredder.energy,
                shredder.energy.getReceiverSpeed());

        if (shredder.progress == 0) {
            shredder.soundCycle = 0;
        }
        if (shredder.energy.getPower() > 0L && shredder.canProcess()) {
            shredder.progress++;
            shredder.energy.setPower(Math.max(0L, shredder.energy.getPower() - CONSUMPTION));
            if (shredder.progress >= PROCESSING_SPEED) {
                shredder.damageBlade(SLOT_BLADE_LEFT);
                shredder.damageBlade(SLOT_BLADE_RIGHT);
                shredder.progress = 0;
                shredder.processItems();
            }
            if (shredder.soundCycle == 0) {
                LegacySoundPlayer.playSoundEffect(level, pos, "minecraft:entity.minecart.riding", 1.0F, 0.75F);
            }
            shredder.soundCycle = (shredder.soundCycle + 1) % 50;
        } else {
            shredder.progress = 0;
        }

        shredder.networkPackNT(50);
        if (oldPower != shredder.energy.getPower() || oldProgress != shredder.progress) {
            shredder.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ShredderBlockEntity shredder) {
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getProgressScaled(int width) {
        return progress * width / PROCESSING_SPEED;
    }

    public int getPowerScaled(int height) {
        return energy.getMaxPower() <= 0L ? 0 : (int) (energy.getPower() * height / energy.getMaxPower());
    }

    public int getGearLeft() {
        return bladeState(SLOT_BLADE_LEFT);
    }

    public int getGearRight() {
        return bladeState(SLOT_BLADE_RIGHT);
    }

    public boolean bladesBrokenOrMissing() {
        return getGearLeft() == 0 || getGearLeft() == 3 || getGearRight() == 0 || getGearRight() == 3;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public Component getDisplayName() {
        if (customName != null && !customName.isBlank()) {
            return Component.literal(customName);
        }
        return Component.translatableWithFallback("container.machineShredder", "Shredder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ShredderMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_PROGRESS, progress);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadInventory(tag);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        progress = tag.getInt(TAG_PROGRESS);
        customName = tag.contains(TAG_NAME, Tag.TAG_STRING) ? tag.getString(TAG_NAME) : null;
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        automationItems.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return automationItems.cast();
        }
        return super.getCapability(capability, side);
    }

    private boolean canProcess() {
        if (getGearLeft() <= 0 || getGearLeft() >= 3 || getGearRight() <= 0 || getGearRight() >= 3) {
            return false;
        }
        for (int slot = SLOT_INPUT_START; slot <= SLOT_INPUT_END; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty() && hasSpace(stack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpace(ItemStack input) {
        ItemProcessingRecipe recipe = ItemProcessingRecipeRuntime.find(level, ItemProcessingRecipe.Machine.SHREDDER, input);
        if (recipe == null) {
            return false;
        }
        List<ItemStack> outputs = recipe.rollOutputStacks(level.random);
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, SLOT_OUTPUT_START, SLOT_OUTPUT_END, outputs);
    }

    private void processItems() {
        for (int inputSlot = SLOT_INPUT_START; inputSlot <= SLOT_INPUT_END; inputSlot++) {
            ItemStack input = items.getStackInSlot(inputSlot);
            if (input.isEmpty() || !hasSpace(input)) {
                continue;
            }
            ItemProcessingRecipe recipe = ItemProcessingRecipeRuntime.find(level, ItemProcessingRecipe.Machine.SHREDDER, input);
            if (recipe == null) {
                continue;
            }
            List<ItemStack> outputs = recipe.rollOutputStacks(level.random);
            if (!HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, SLOT_OUTPUT_START, SLOT_OUTPUT_END, outputs)) {
                continue;
            }
            for (ItemStack output : outputs) {
                HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_OUTPUT_START, SLOT_OUTPUT_END, output);
            }
            items.extractItem(inputSlot, 1, false);
        }
    }

    private int bladeState(int slot) {
        ItemStack blade = items.getStackInSlot(slot);
        if (!(blade.getItem() instanceof ShredderBladeItem)) {
            return 0;
        }
        if (!blade.isDamageableItem()) {
            return 1;
        }
        if (blade.getDamageValue() < blade.getMaxDamage() / 2) {
            return 1;
        }
        if (blade.getDamageValue() < blade.getMaxDamage()) {
            return 2;
        }
        return 3;
    }

    private void damageBlade(int slot) {
        ItemStack blade = items.getStackInSlot(slot);
        if (blade.isDamageableItem()) {
            blade.setDamageValue(Math.min(blade.getMaxDamage(), blade.getDamageValue() + 1));
        }
    }

    private void loadInventory(CompoundTag tag) {
        if (tag.contains(TAG_ITEMS, Tag.TAG_LIST) || tag.contains(TAG_ITEMS, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
            return;
        }
        if (tag.contains(TAG_INVENTORY, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
            return;
        }
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
    }

    public static boolean isLegacyBattery(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IBatteryItem;
    }

    private final class AccessibleItemHandler implements IItemHandler {
        private final int[] slots = {
                SLOT_INPUT_START, 1, 2, 3, 4, 5, 6, 7, SLOT_INPUT_END,
                SLOT_OUTPUT_START, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, SLOT_OUTPUT_END,
                SLOT_BLADE_LEFT, SLOT_BLADE_RIGHT, SLOT_BATTERY
        };

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return valid(slot) ? items.getStackInSlot(slots[slot]) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!valid(slot)) {
                return stack;
            }
            int target = slots[slot];
            if (target >= SLOT_OUTPUT_START && target <= SLOT_OUTPUT_END) {
                return stack;
            }
            if (target >= SLOT_INPUT_START && target <= SLOT_INPUT_END
                    && !canAutomationInsertIntoInput(target, stack)) {
                return stack;
            }
            return items.insertItem(target, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!valid(slot)) {
                return ItemStack.EMPTY;
            }
            int target = slots[slot];
            ItemStack stack = items.getStackInSlot(target);
            if (target >= SLOT_OUTPUT_START && target <= SLOT_OUTPUT_END) {
                return items.extractItem(target, amount, simulate);
            }
            if ((target == SLOT_BLADE_LEFT || target == SLOT_BLADE_RIGHT)
                    && stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
                return items.extractItem(target, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return valid(slot) ? items.getSlotLimit(slots[slot]) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return valid(slot) && items.isItemValid(slots[slot], stack);
        }

        private boolean valid(int slot) {
            return slot >= 0 && slot < slots.length;
        }

        private boolean canAutomationInsertIntoInput(int target, ItemStack stack) {
            ItemStack targetStack = items.getStackInSlot(target);
            if (targetStack.isEmpty()) {
                return true;
            }
            int targetSize = targetStack.getCount();
            for (int inputSlot = SLOT_INPUT_START; inputSlot <= SLOT_INPUT_END; inputSlot++) {
                ItemStack existing = items.getStackInSlot(inputSlot);
                if (existing.isEmpty()) {
                    return false;
                }
                if (HbmItemStackUtil.areStacksCompatible(existing, stack) && existing.getCount() < targetSize) {
                    return false;
                }
            }
            return true;
        }
    }
}
