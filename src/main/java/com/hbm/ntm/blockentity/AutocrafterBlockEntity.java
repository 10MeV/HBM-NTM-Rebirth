package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.IBatteryItem;
import com.hbm.ntm.menu.AutocrafterMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.LegacyPatternMatcher;
import java.util.ArrayList;
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
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
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

public class AutocrafterBlockEntity extends HbmEnergyBlockEntity
        implements MenuProvider {
    public static final int SLOT_TEMPLATE_START = 0;
    public static final int SLOT_TEMPLATE_END = 8;
    public static final int SLOT_TEMPLATE_OUTPUT = 9;
    public static final int SLOT_INPUT_START = 10;
    public static final int SLOT_INPUT_END = 18;
    public static final int SLOT_OUTPUT = 19;
    public static final int SLOT_BATTERY = 20;
    public static final int SLOT_COUNT = 21;
    public static final long CONSUMPTION = 100L;
    public static final long MAX_POWER = CONSUMPTION * 100L;

    private static final String TAG_ITEMS = "items";
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_POWER = "power";
    private static final String TAG_RECIPE = "rec";
    private static final String TAG_NAME = "name";

    private final LegacyPatternMatcher matcher = new LegacyPatternMatcher(9);
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChangedAndUpdate();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END) {
                return canInsertRecipeSlot(slot, stack);
            }
            return slot == SLOT_BATTERY && isLegacyBattery(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> menuItems = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> automationItems = LazyOptional.of(AccessibleItemHandler::new);
    private final List<CraftingRecipe> recipes = new ArrayList<>();
    private int recipeIndex;
    private int recipeCount;
    @Nullable
    private String customName;

    public AutocrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTOCRAFTER.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AutocrafterBlockEntity autocrafter) {
        if (level.isClientSide) {
            return;
        }
        long oldPower = autocrafter.energy.getPower();
        ItemStack oldOutput = autocrafter.items.getStackInSlot(SLOT_OUTPUT).copy();

        HbmEnergyUtil.chargeStorageFromItem(autocrafter.items.getStackInSlot(SLOT_BATTERY), autocrafter.energy,
                autocrafter.energy.getReceiverSpeed());
        autocrafter.subscribeEnergyReceiverToAllSides();

        if (!autocrafter.recipes.isEmpty() && autocrafter.energy.getPower() >= CONSUMPTION) {
            autocrafter.tryCraft();
        }

        if (oldPower != autocrafter.energy.getPower()
                || !ItemStack.isSameItemSameTags(oldOutput, autocrafter.items.getStackInSlot(SLOT_OUTPUT))
                || oldOutput.getCount() != autocrafter.items.getStackInSlot(SLOT_OUTPUT).getCount()) {
            autocrafter.setChangedAndUpdate();
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getRecipeIndex() {
        return recipeIndex;
    }

    public int getRecipeCount() {
        return recipeCount;
    }

    public int getModeIndex(int slot) {
        return matcher.getModeIndex(items.getStackInSlot(slot), slot);
    }

    public long getConsumption() {
        return CONSUMPTION;
    }

    public void nextMode(int slot) {
        matcher.nextMode(items.getStackInSlot(slot), slot);
        updateTemplateGrid();
        setChangedAndUpdate();
    }

    public void updatePatternSlot(int slot, ItemStack stack) {
        matcher.initPatternSmart(stack, slot);
        updateTemplateGrid();
        setChangedAndUpdate();
    }

    public void nextTemplate() {
        if (level == null || level.isClientSide) {
            return;
        }
        recipeIndex++;
        if (recipeIndex >= recipes.size()) {
            recipeIndex = 0;
        }
        refreshPreview();
        setChangedAndUpdate();
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> drops = new ArrayList<>();
        for (int slot = SLOT_INPUT_START; slot <= SLOT_BATTERY; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                items.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
        for (int slot = SLOT_TEMPLATE_START; slot <= SLOT_TEMPLATE_OUTPUT; slot++) {
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return drops;
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
        return Component.translatableWithFallback("container.autocrafter", "Automatic Crafting Table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AutocrafterMenu(containerId, inventory, this);
    }

    private void updateTemplateGrid() {
        if (level == null || level.isClientSide) {
            return;
        }
        recipes.clear();
        CraftingContainer grid = templateGrid();
        for (CraftingRecipe recipe : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            if (recipe.matches(grid, level)) {
                recipes.add(recipe);
            }
        }
        recipeCount = recipes.size();
        recipeIndex = 0;
        refreshPreview();
    }

    private void refreshPreview() {
        if (level == null || recipes.isEmpty()) {
            items.setStackInSlot(SLOT_TEMPLATE_OUTPUT, ItemStack.EMPTY);
            recipeCount = recipes.size();
            recipeIndex = 0;
            return;
        }
        recipeIndex = Math.floorMod(recipeIndex, recipes.size());
        ItemStack result = recipes.get(recipeIndex).assemble(templateGrid(), level.registryAccess());
        items.setStackInSlot(SLOT_TEMPLATE_OUTPUT, result.isEmpty() ? ItemStack.EMPTY : result.copy());
        recipeCount = recipes.size();
    }

    private void tryCraft() {
        if (level == null || recipes.isEmpty()) {
            return;
        }
        recipeIndex = Math.floorMod(recipeIndex, recipes.size());
        CraftingRecipe recipe = recipes.get(recipeIndex);
        CraftingContainer grid = recipeGrid();
        if (!recipe.matches(grid, level)) {
            return;
        }
        ItemStack result = recipe.assemble(grid, level.registryAccess());
        if (result.isEmpty() || !canMergeOutput(result)) {
            return;
        }
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, result.copy());
        } else {
            output.grow(result.getCount());
            items.setStackInSlot(SLOT_OUTPUT, output);
        }
        consumeInputs();
        energy.setPower(energy.getPower() - CONSUMPTION);
    }

    private boolean canMergeOutput(ItemStack stack) {
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        return output.isEmpty()
                || (ItemStack.isSameItemSameTags(output, stack)
                && output.getCount() + stack.getCount() <= output.getMaxStackSize());
    }

    private void consumeInputs() {
        for (int slot = SLOT_INPUT_START; slot <= SLOT_INPUT_END; slot++) {
            ItemStack ingredient = items.getStackInSlot(slot);
            if (ingredient.isEmpty()) {
                continue;
            }
            ItemStack container = ingredient.getCraftingRemainingItem();
            items.extractItem(slot, 1, false);
            if (items.getStackInSlot(slot).isEmpty() && !container.isEmpty()
                    && (!container.isDamageableItem() || container.getDamageValue() <= container.getMaxDamage())) {
                items.setStackInSlot(slot, container.copy());
            }
        }
    }

    private CraftingContainer templateGrid() {
        return gridFrom(SLOT_TEMPLATE_START);
    }

    private CraftingContainer recipeGrid() {
        return gridFrom(SLOT_INPUT_START);
    }

    private CraftingContainer gridFrom(int start) {
        CraftingContainer crafting = new TransientCraftingContainer(new EmptyCraftingMenu(), 3, 3);
        for (int slot = 0; slot < 9; slot++) {
            crafting.setItem(slot, items.getStackInSlot(start + slot).copy());
        }
        return crafting;
    }

    private boolean canInsertRecipeSlot(int slot, ItemStack stack) {
        if (stack.isEmpty() || slot < SLOT_INPUT_START || slot > SLOT_INPUT_END) {
            return false;
        }
        if (stack.getCount() > 1 && stack.hasCraftingRemainingItem()) {
            return false;
        }
        ItemStack filter = items.getStackInSlot(slot - SLOT_INPUT_START);
        if (filter.isEmpty()) {
            return false;
        }
        ItemStack existing = items.getStackInSlot(slot);
        if (!existing.isEmpty() && existing.getCount() + stack.getCount() > 4) {
            return false;
        }
        if (stack.getCount() > 4) {
            return false;
        }
        List<Integer> validSlots = new ArrayList<>();
        for (int i = SLOT_TEMPLATE_START; i <= SLOT_TEMPLATE_END; i++) {
            ItemStack slotFilter = items.getStackInSlot(i);
            if (!slotFilter.isEmpty() && matcher.getMode(i) != null
                    && matcher.isValidForFilter(slotFilter, i, stack)) {
                int inputSlot = i + SLOT_INPUT_START;
                validSlots.add(inputSlot);
                if (inputSlot == slot && existing.isEmpty()) {
                    return true;
                }
            }
        }
        if (!validSlots.contains(slot)) {
            return false;
        }
        int size = existing.getCount();
        for (int validSlot : validSlots) {
            ItemStack valid = items.getStackInSlot(validSlot);
            if (valid.isEmpty()) {
                return false;
            }
            if (!ItemStack.isSameItem(valid, stack)) {
                continue;
            }
            if (valid.getCount() < size) {
                return false;
            }
        }
        return !stack.hasCraftingRemainingItem();
    }

    private boolean canExtractSlot(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT) {
            return true;
        }
        if (slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END) {
            int filterSlot = slot - SLOT_INPUT_START;
            ItemStack filter = items.getStackInSlot(filterSlot);
            return filter.isEmpty() || matcher.getMode(filterSlot) == null
                    || !matcher.isValidForFilter(filter, filterSlot, stack);
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, TAG_ITEMS, items);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_RECIPE, recipeIndex);
        matcher.writeToNbt(tag);
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
        matcher.readFromNbt(tag);
        recipeIndex = Math.max(0, tag.getInt(TAG_RECIPE));
        customName = tag.contains(TAG_NAME, Tag.TAG_STRING) ? tag.getString(TAG_NAME) : null;
        if (level != null && !level.isClientSide) {
            updateTemplateGrid();
            if (!recipes.isEmpty()) {
                recipeIndex = Math.min(recipeIndex, recipes.size() - 1);
                refreshPreview();
            }
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
        menuItems.invalidate();
        automationItems.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? menuItems.cast() : automationItems.cast();
        }
        return super.getCapability(capability, side);
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static boolean isLegacyBattery(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IBatteryItem;
    }

    private final class AccessibleItemHandler implements IItemHandler {
        private final int[] slots = {
                SLOT_INPUT_START, 11, 12, 13, 14, 15, 16, 17, SLOT_INPUT_END, SLOT_OUTPUT
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
            return target >= SLOT_INPUT_START && target <= SLOT_INPUT_END
                    ? items.insertItem(target, stack, simulate)
                    : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!valid(slot) || amount <= 0) {
                return ItemStack.EMPTY;
            }
            int target = slots[slot];
            ItemStack stack = items.getStackInSlot(target);
            return canExtractSlot(target, stack) ? items.extractItem(target, amount, simulate) : ItemStack.EMPTY;
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
    }

    private static final class EmptyCraftingMenu extends AbstractContainerMenu {
        private EmptyCraftingMenu() {
            super(null, -1);
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }
    }
}
