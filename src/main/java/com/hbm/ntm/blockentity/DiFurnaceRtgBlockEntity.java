package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.DiFurnaceRtgBlock;
import com.hbm.ntm.menu.DiFurnaceRtgMenu;
import com.hbm.ntm.recipe.BlastFurnaceRecipe;
import com.hbm.ntm.recipe.HbmItemOutput;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.RtgPelletRuntime;
import java.util.List;
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

public class DiFurnaceRtgBlockEntity extends BlockEntity implements MenuProvider {
    public static final int PROCESS_TIME = 1_200;
    private static final String TAG_ITEMS = "Items";
    private final ItemStackHandler items = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 2) {
                return false;
            }
            return RtgBlockEntity.isRtgPellet(stack) ? slot > 2 : slot < 2;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private int progress;
    private int speed;
    private byte sideUpper = 1;
    private byte sideLower = 1;

    public DiFurnaceRtgBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DIFURNACE_RTG.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DiFurnaceRtgBlockEntity furnace) {
        boolean changed = furnace.tick(level);
        boolean lit = furnace.isProcessing() || furnace.canProcess(level) && furnace.hasPower();
        if (state.hasProperty(DiFurnaceRtgBlock.LIT) && state.getValue(DiFurnaceRtgBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(DiFurnaceRtgBlock.LIT, lit), Block.UPDATE_CLIENTS);
            changed = true;
        }
        if (changed || level.getGameTime() % 10L == 0L) {
            furnace.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_CLIENTS);
        }
    }

    private boolean tick(Level level) {
        int oldProgress = progress;
        int oldSpeed = speed;
        speed = calculateSpeed();
        if (canProcess(level) && hasPower()) {
            progress += speed;
            if (progress >= PROCESS_TIME) {
                progress = 0;
                process(level);
            }
        } else {
            progress = 0;
        }
        return oldProgress != progress || oldSpeed != speed;
    }

    private int calculateSpeed() {
        return RtgPelletRuntime.updateHeat(items, 3, 8);
    }

    private boolean hasPower() {
        return speed >= 15;
    }

    private boolean canProcess(Level level) {
        BlastFurnaceRecipe recipe = findRecipe(level);
        if (recipe == null) {
            return false;
        }
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, 2, 2,
                recipe.outputs().get(0).representativeStack());
    }

    @Nullable
    private BlastFurnaceRecipe findRecipe(Level level) {
        ItemStack upper = items.getStackInSlot(0);
        ItemStack lower = items.getStackInSlot(1);
        if (upper.isEmpty() || lower.isEmpty()) {
            return null;
        }
        for (BlastFurnaceRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.BLAST_FURNACE.type().get())) {
            if (recipe.inputs().size() == 2 && recipe.outputs().size() == 1 && recipe.matches(upper, lower)) {
                return recipe;
            }
        }
        return null;
    }

    private void process(Level level) {
        BlastFurnaceRecipe recipe = findRecipe(level);
        if (recipe == null) {
            return;
        }
        HbmItemOutput output = recipe.outputs().get(0);
        int upperConsumed = recipe.consumedCountForSlot(0, items.getStackInSlot(0), items.getStackInSlot(1));
        int lowerConsumed = recipe.consumedCountForSlot(1, items.getStackInSlot(0), items.getStackInSlot(1));
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, 2, 2, output.representativeStack());
        items.extractItem(0, upperConsumed, false);
        items.extractItem(1, lowerConsumed, false);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getSpeed() {
        return speed;
    }

    public byte getSideUpper() {
        return sideUpper;
    }

    public byte getSideLower() {
        return sideLower;
    }

    public boolean isProcessing() {
        return progress > 0;
    }

    public int getProgressPixels(int width) {
        return progress * width / PROCESS_TIME;
    }

    public void cycleSideMode(int slot) {
        if (slot == 0) {
            sideUpper = (byte) ((sideUpper + 1) % 6);
        } else if (slot == 1) {
            sideLower = (byte) ((sideLower + 1) % 6);
        } else {
            return;
        }
        setChanged();
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.diFurnaceRTG", "RTG Blast Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DiFurnaceRtgMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putShort("progress", (short) progress);
        tag.putShort("speed", (short) speed);
        tag.putByteArray("modes", new byte[] {sideUpper, sideLower});
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        progress = tag.getShort("progress");
        speed = tag.getShort("speed");
        byte[] modes = tag.getByteArray("modes");
        if (modes.length >= 2) {
            sideUpper = modes[0];
            sideLower = modes[1];
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return side == null ? itemHandler.cast() : LazyOptional.of(() -> new AccessibleItemHandler(side)).cast();
        }
        return super.getCapability(capability, side);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Nullable
        private final Direction side;

        private AccessibleItemHandler() {
            this(null);
        }

        private AccessibleItemHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return 9;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < 9 ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot < 0 || slot >= 9 || slot == 2) {
                return stack;
            }
            if (side != null && !canInsertFromSide(slot)) {
                return stack;
            }
            return items.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 2 || slot > 2 && !RtgBlockEntity.isRtgPellet(items.getStackInSlot(slot))) {
                return items.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < 9 ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < 9 && (side == null || canInsertFromSide(slot))
                    && items.isItemValid(slot, stack);
        }

        private boolean canInsertFromSide(int slot) {
            int ordinal = side == null ? -1 : side.ordinal();
            if (slot == 0) {
                return sideUpper == ordinal;
            }
            if (slot == 1) {
                return sideLower == ordinal;
            }
            return slot > 2;
        }
    }
}
