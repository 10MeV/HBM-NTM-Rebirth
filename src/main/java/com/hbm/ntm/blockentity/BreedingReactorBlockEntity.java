package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.BreedingReactorMenu;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.api.tile.IInfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.recipe.BreedingReactorRecipeRuntime;
import com.hbm.ntm.recipe.BreedingReactorRecipeRuntime.BreederRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

public class BreedingReactorBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyLoadedTile, IInfoProviderEC {
    private static final String TAG_INVENTORY = "items";
    private static final String TAG_FLUX = "flux";
    private static final String TAG_PROGRESS = "progress";
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int SLOT_COUNT = 2;

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_SLOT && !stack.isEmpty();
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());

    private int flux;
    private float progress;

    public BreedingReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREEDING_REACTOR.get(), pos, state);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BreedingReactorBlockEntity breeder) {
        breeder.flux = breeder.collectFlux(level, pos);
        boolean changed = breeder.processTick(level);
        breeder.networkPackNT(20);
        if (changed || level.getGameTime() % 20L == 0L) {
            breeder.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private int collectFlux(Level level, BlockPos pos) {
        int collected = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos side = pos.relative(direction);
            if (MultiblockHelper.resolveCoreBlockEntity(level, side) instanceof ResearchReactorBlockEntity reactor) {
                collected += reactor.getTotalFlux();
            }
        }
        return collected;
    }

    private boolean processTick(Level level) {
        if (!canProcess(level)) {
            boolean changed = progress != 0.0F;
            progress = 0.0F;
            return changed;
        }
        BreederRecipe recipe = BreedingReactorRecipeRuntime.recipeFor(level, items.getStackInSlot(INPUT_SLOT));
        progress += 0.0025F * (flux / (float) recipe.flux());
        if (progress >= 1.0F) {
            progress = 0.0F;
            processItem(level, recipe);
        }
        return true;
    }

    private boolean canProcess(Level level) {
        BreederRecipe recipe = BreedingReactorRecipeRuntime.recipeFor(level, items.getStackInSlot(INPUT_SLOT));
        if (recipe == null || flux < recipe.flux()) {
            return false;
        }
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        ItemStack recipeOutput = recipe.output();
        return ItemStack.isSameItem(output, recipeOutput) && output.getCount() < output.getMaxStackSize();
    }

    private void processItem(Level level, BreederRecipe recipe) {
        if (!canProcess(level)) {
            return;
        }
        ItemStack output = items.getStackInSlot(OUTPUT_SLOT);
        ItemStack recipeOutput = recipe.output();
        if (output.isEmpty()) {
            items.setStackInSlot(OUTPUT_SLOT, recipeOutput.copy());
        } else if (ItemStack.isSameItem(output, recipeOutput)) {
            output.grow(recipeOutput.getCount());
            items.setStackInSlot(OUTPUT_SLOT, output);
        }
        items.extractItem(INPUT_SLOT, 1, false);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getFlux() {
        return flux;
    }

    public float getProgress() {
        return progress;
    }

    public int getProgressScaled() {
        return (int) (progress * 10_000.0F);
    }

    public int getProgressWidth(int width) {
        return (int) (progress * width);
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putInt(CompatEnergyControl.I_FLUX, flux);
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.putInt(TAG_FLUX, flux);
        tag.putFloat(TAG_PROGRESS, progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        flux = tag.getInt(TAG_FLUX);
        progress = tag.getFloat(TAG_PROGRESS);
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
        writeLegacyLoadedTileBinary(data);
        data.writeInt(flux);
        data.writeFloat(progress);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        flux = data.readInt();
        progress = data.readFloat();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition, worldPosition.offset(1, 3, 1));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.reactorBreeding", "Breeding Reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new BreedingReactorMenu(containerId, inventory, this);
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
            return slot == INPUT_SLOT ? items.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == OUTPUT_SLOT ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < SLOT_COUNT ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_SLOT && !stack.isEmpty();
        }
    }
}
