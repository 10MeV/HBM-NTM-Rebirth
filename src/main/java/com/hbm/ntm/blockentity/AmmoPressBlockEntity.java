package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.AmmoPressMenu;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.recipe.AmmoPressRecipe;
import com.hbm.ntm.recipe.AmmoPressRecipeRuntime;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AmmoPressBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyLoadedTile {
    private static final String TAG_INVENTORY = "items";
    private static final String TAG_RECIPE = "recipe";
    private static final String TAG_PLAY_ANIMATION = "playAnimation";
    private static final String TAG_ANIM_STATE = "animState";
    private static final String TAG_LIFT = "lift";
    private static final String TAG_PRESS = "press";
    private static final String TAG_SELECTION = "selection";

    public static final int SLOT_INPUT_START = 0;
    public static final int SLOT_INPUT_END = 8;
    public static final int SLOT_OUTPUT = 9;
    public static final int SLOT_COUNT = 10;

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot < SLOT_INPUT_START || slot > SLOT_INPUT_END) {
                return false;
            }
            AmmoPressRecipe recipe = getSelectedRecipe();
            return recipe != null && recipe.matchesSlot(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private int selectedRecipe = -1;
    private int playAnimation;
    private AnimationState animState = AnimationState.LIFTING;
    private float prevLift;
    private float lift;
    private float prevPress;
    private float press;

    public AmmoPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AMMO_PRESS.get(), pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AmmoPressBlockEntity press) {
        int oldAnimation = press.playAnimation;
        int oldRecipe = press.selectedRecipe;
        if (press.playAnimation > 0) {
            press.playAnimation--;
        }
        boolean changed = press.performRecipes(level);
        changed |= oldAnimation != press.playAnimation || oldRecipe != press.selectedRecipe;
        if (changed) {
            press.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        press.networkPackNT(25);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AmmoPressBlockEntity press) {
        press.prevLift = press.lift;
        press.prevPress = press.press;
        if (press.playAnimation <= 0 && press.lift <= 0.0F) {
            return;
        }

        switch (press.animState) {
            case LIFTING -> {
                press.lift += 1.0F / 40.0F;
                if (press.lift >= 1.0F) {
                    press.lift = 1.0F;
                    press.animState = AnimationState.PRESSING;
                }
            }
            case PRESSING -> {
                press.press += 1.0F / 20.0F;
                if (press.press >= 1.0F) {
                    press.press = 1.0F;
                    press.animState = AnimationState.RETRACTING;
                }
            }
            case RETRACTING -> {
                press.press -= 1.0F / 20.0F;
                if (press.press <= 0.0F) {
                    press.press = 0.0F;
                    press.animState = AnimationState.LOWERING;
                }
            }
            case LOWERING -> {
                press.lift -= 1.0F / 40.0F;
                if (press.lift <= 0.0F) {
                    press.lift = 0.0F;
                    press.animState = AnimationState.LIFTING;
                }
            }
        }
    }

    private boolean performRecipes(Level level) {
        AmmoPressRecipe recipe = getSelectedRecipe(level);
        if (recipe == null) {
            return false;
        }

        boolean changed = false;
        int brake = 0;
        while (brake++ < 64 && canProduce(recipe)) {
            consumeInputs(recipe);
            mergeOutput(recipe.output());
            playAnimation = 40;
            changed = true;
        }
        return changed;
    }

    private boolean canProduce(AmmoPressRecipe recipe) {
        ItemStack output = recipe.output();
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameTags(existing, output)) {
            return false;
        }
        if (!existing.isEmpty() && existing.getCount() + output.getCount() > existing.getMaxStackSize()) {
            return false;
        }
        return recipe.matchesGrid(items);
    }

    private void consumeInputs(AmmoPressRecipe recipe) {
        for (int slot = SLOT_INPUT_START; slot <= SLOT_INPUT_END; slot++) {
            if (recipe.input(slot) != null) {
                items.extractItem(slot, recipe.input(slot).count(), false);
            }
        }
    }

    private void mergeOutput(ItemStack output) {
        ItemStack existing = items.getStackInSlot(SLOT_OUTPUT);
        if (existing.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, output.copy());
            return;
        }
        existing.grow(output.getCount());
        items.setStackInSlot(SLOT_OUTPUT, existing);
    }

    @Nullable
    public AmmoPressRecipe getSelectedRecipe() {
        return level == null ? null : getSelectedRecipe(level);
    }

    @Nullable
    private AmmoPressRecipe getSelectedRecipe(Level level) {
        List<AmmoPressRecipe> recipes = AmmoPressRecipeRuntime.recipes(level);
        return selectedRecipe >= 0 && selectedRecipe < recipes.size() ? recipes.get(selectedRecipe) : null;
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getSelectedRecipeIndex() {
        return selectedRecipe;
    }

    public void setSelectedRecipeIndex(int selectedRecipe) {
        if (level == null) {
            this.selectedRecipe = selectedRecipe;
            return;
        }
        int size = AmmoPressRecipeRuntime.recipes(level).size();
        this.selectedRecipe = selectedRecipe >= 0 && selectedRecipe < size ? selectedRecipe : -1;
        setChanged();
    }

    public float getLift(float partialTick) {
        return prevLift + (lift - prevLift) * partialTick;
    }

    public float getPress(float partialTick) {
        return prevPress + (press - prevPress) * partialTick;
    }

    public boolean shouldRenderBullets() {
        return animState == AnimationState.RETRACTING || animState == AnimationState.LOWERING;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putInt(TAG_RECIPE, selectedRecipe);
        tag.putInt(TAG_PLAY_ANIMATION, playAnimation);
        tag.putString(TAG_ANIM_STATE, animState.name());
        tag.putFloat(TAG_LIFT, lift);
        tag.putFloat(TAG_PRESS, press);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        selectedRecipe = tag.getInt(TAG_RECIPE);
        playAnimation = tag.getInt(TAG_PLAY_ANIMATION);
        animState = parseAnimationState(tag.getString(TAG_ANIM_STATE));
        lift = tag.getFloat(TAG_LIFT);
        prevLift = lift;
        press = tag.getFloat(TAG_PRESS);
        prevPress = press;
    }

    private static AnimationState parseAnimationState(String name) {
        try {
            return name == null || name.isBlank() ? AnimationState.LIFTING : AnimationState.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return AnimationState.LIFTING;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
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
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(saveWithoutMetadata());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return tag.contains(TAG_SELECTION);
    }

    @Override
    public void handleClientControl(ServerPlayer player, CompoundTag tag) {
        int selection = tag.getInt(TAG_SELECTION);
        setSelectedRecipeIndex(selection == selectedRecipe ? -1 : selection);
    }

    public static CompoundTag selectionControlTag(int selection) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_SELECTION, selection);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 2, 2));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineAmmoPress", "Ammo Press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AmmoPressMenu(containerId, inventory, this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable net.minecraft.core.Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END ? items.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == SLOT_OUTPUT ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END && items.isItemValid(slot, stack);
        }
    }

    public enum AnimationState {
        LIFTING,
        PRESSING,
        RETRACTING,
        LOWERING
    }
}
