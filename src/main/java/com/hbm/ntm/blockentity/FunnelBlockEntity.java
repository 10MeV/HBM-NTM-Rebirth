package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.FunnelMenu;
import com.hbm.ntm.network.HbmLegacyButtonReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
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

public class FunnelBlockEntity extends BlockEntity implements MenuProvider, HbmLegacyButtonReceiver {
    public static final int SLOT_INPUT_START = 0;
    public static final int SLOT_INPUT_END = 8;
    public static final int SLOT_OUTPUT_START = 9;
    public static final int SLOT_OUTPUT_END = 17;
    public static final int SLOT_COUNT = 18;
    public static final int MODE_ALL = 0;
    public static final int MODE_3X3 = 1;
    public static final int MODE_2X2 = 2;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END && canInsertInput(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> menuItemHandler = LazyOptional.of(AccessibleItemHandler::new);
    private final LazyOptional<IItemHandler> topItemHandler = LazyOptional.of(() -> new SidedItemHandler(Direction.UP));
    private final LazyOptional<IItemHandler> bottomItemHandler = LazyOptional.of(() -> new SidedItemHandler(Direction.DOWN));
    private final LazyOptional<IItemHandler> sideItemHandler = LazyOptional.of(() -> new SidedItemHandler(Direction.NORTH));
    private int mode;

    public FunnelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUNNEL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FunnelBlockEntity funnel) {
        if (level.isClientSide) {
            return;
        }
        boolean changed = false;
        for (int slot = SLOT_INPUT_START; slot <= SLOT_INPUT_END; slot++) {
            changed |= funnel.processSlot(slot);
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            funnel.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getMode() {
        return mode;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    private boolean processSlot(int slot) {
        ItemStack input = items.getStackInSlot(slot);
        if (input.isEmpty()) {
            return false;
        }
        if ((mode == MODE_ALL || mode == MODE_3X3) && input.getCount() >= 9 && tryCraft(slot, 3, 9)) {
            return true;
        }
        return (mode == MODE_ALL || mode == MODE_2X2) && input.getCount() >= 4 && tryCraft(slot, 2, 4);
    }

    private boolean tryCraft(int slot, int size, int count) {
        ItemStack input = items.getStackInSlot(slot);
        ItemStack output = craftCompression(input, size);
        int outputSlot = slot + 9;
        if (output.isEmpty() || !canMergeIntoSlot(outputSlot, output)) {
            return false;
        }
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, outputSlot, outputSlot, output);
        items.extractItem(slot, count, false);
        return true;
    }

    private boolean canInsertInput(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!items.getStackInSlot(slot).isEmpty()) {
            return true;
        }
        return !craftCompression(stack, 3).isEmpty() || !craftCompression(stack, 2).isEmpty();
    }

    private boolean canMergeIntoSlot(int slot, ItemStack stack) {
        ItemStack existing = items.getStackInSlot(slot);
        return existing.isEmpty()
                || (ItemStack.isSameItemSameTags(existing, stack)
                && existing.getCount() + stack.getCount() <= existing.getMaxStackSize());
    }

    private ItemStack craftCompression(ItemStack input, int size) {
        if (level == null || input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        CraftingContainer crafting = new TransientCraftingContainer(new EmptyCraftingMenu(), size, size);
        for (int slot = 0; slot < size * size; slot++) {
            crafting.setItem(slot, input.copyWithCount(1));
        }
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level)
                .map(recipe -> recipe.assemble(crafting, level.registryAccess()))
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.machineFunnel", "Compression Funnel");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FunnelMenu(containerId, inventory, this);
    }

    @Override
    public void handleLegacyButton(ServerPlayer player, int value, int id) {
        mode = (mode + 1) % 3;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "Inventory", items);
        tag.putInt("mode", mode);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "Inventory", items);
        mode = Math.floorMod(tag.getInt("mode"), 3);
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
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        load(packet.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        menuItemHandler.invalidate();
        topItemHandler.invalidate();
        bottomItemHandler.invalidate();
        sideItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return menuItemHandler.cast();
            }
            if (side == Direction.UP) {
                return topItemHandler.cast();
            }
            return side == Direction.DOWN ? bottomItemHandler.cast() : sideItemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private final class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return valid(slot) ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return valid(slot) && slot <= SLOT_INPUT_END ? items.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return valid(slot) ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return valid(slot) ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return valid(slot) && items.isItemValid(slot, stack);
        }

        private boolean valid(int slot) {
            return slot >= 0 && slot < SLOT_COUNT;
        }
    }

    private final class SidedItemHandler implements IItemHandler {
        private final Direction side;

        private SidedItemHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return side == Direction.DOWN ? 9 : 9;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int mapped = mapSlot(slot);
            return mapped >= 0 ? items.getStackInSlot(mapped) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int mapped = mapSlot(slot);
            return mapped >= SLOT_INPUT_START && mapped <= SLOT_INPUT_END
                    ? items.insertItem(mapped, stack, simulate)
                    : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int mapped = mapSlot(slot);
            if (mapped < 0) {
                return ItemStack.EMPTY;
            }
            if (side == Direction.DOWN && mapped >= SLOT_OUTPUT_START) {
                return items.extractItem(mapped, amount, simulate);
            }
            if (side != Direction.UP && mapped <= SLOT_INPUT_END) {
                return items.extractItem(mapped, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int mapped = mapSlot(slot);
            return mapped >= 0 ? items.getSlotLimit(mapped) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int mapped = mapSlot(slot);
            return mapped >= SLOT_INPUT_START && mapped <= SLOT_INPUT_END && items.isItemValid(mapped, stack);
        }

        private int mapSlot(int slot) {
            if (slot < 0 || slot >= 9) {
                return -1;
            }
            return side == Direction.DOWN ? SLOT_OUTPUT_START + slot : SLOT_INPUT_START + slot;
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
