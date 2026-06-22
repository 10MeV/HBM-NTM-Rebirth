package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.RtgFurnaceBlock;
import com.hbm.ntm.menu.RtgFurnaceMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import com.hbm.ntm.util.RtgPelletRuntime;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class RtgFurnaceBlockEntity extends BlockEntity implements MenuProvider {
    public static final int PROCESS_TIME = 1_000;
    private static final String TAG_ITEMS = "items";
    private final ItemStackHandler items = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < getSlots();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private int cookTime;
    private int heat;

    public RtgFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RTG_FURNACE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RtgFurnaceBlockEntity furnace) {
        boolean changed = furnace.tick();
        boolean lit = furnace.isProcessing();
        if (state.hasProperty(RtgFurnaceBlock.LIT) && state.getValue(RtgFurnaceBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(RtgFurnaceBlock.LIT, lit), Block.UPDATE_CLIENTS);
            changed = true;
        }
        if (changed || level.getGameTime() % 20L == 0L) {
            furnace.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), Block.UPDATE_CLIENTS);
        }
    }

    private boolean tick() {
        int old = cookTime;
        int oldHeat = heat;
        heat = RtgPelletRuntime.updateHeat(items, 1, 3);
        if (heat > 0 && canProcess()) {
            cookTime += heat;
            if (cookTime >= PROCESS_TIME) {
                cookTime = 0;
                processItem();
            }
        } else {
            cookTime = 0;
        }
        return old != cookTime || oldHeat != heat;
    }

    private boolean canProcess() {
        return smeltingResult(items.getStackInSlot(0))
                .filter(result -> HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, 4, 4, result))
                .isPresent();
    }

    private void processItem() {
        smeltingResult(items.getStackInSlot(0)).ifPresent(result -> {
            HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, 4, 4, result);
            items.extractItem(0, 1, false);
        });
    }

    private Optional<ItemStack> smeltingResult(ItemStack input) {
        if (level == null || input.isEmpty()) {
            return Optional.empty();
        }
        Container container = new SimpleContainer(input);
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()).copy());
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getHeat() {
        return heat;
    }

    public boolean isProcessing() {
        return cookTime > 0;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.rtgFurnace", "RTG Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RtgFurnaceMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putShort("cookTime", (short) cookTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        cookTime = tag.contains("CookTime") ? tag.getShort("CookTime") : tag.getShort("cookTime");
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
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
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
            return visibleSlots().length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            int target = targetSlot(slot);
            return target >= 0 ? items.getStackInSlot(target) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int target = targetSlot(slot);
            return target >= 0 ? items.insertItem(target, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            int target = targetSlot(slot);
            if (target < 0) {
                return ItemStack.EMPTY;
            }
            if (side == null) {
                return target == 4 ? items.extractItem(target, amount, simulate) : ItemStack.EMPTY;
            }
            ItemStack stack = items.getStackInSlot(target);
            return side != Direction.DOWN || target != 1 || stack.is(Items.BUCKET)
                    ? items.extractItem(target, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            int target = targetSlot(slot);
            return target >= 0 ? items.getSlotLimit(target) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            int target = targetSlot(slot);
            return target >= 0 && target < 4 && items.isItemValid(target, stack);
        }

        private int targetSlot(int visibleSlot) {
            int[] slots = visibleSlots();
            return visibleSlot >= 0 && visibleSlot < slots.length ? slots[visibleSlot] : -1;
        }

        private int[] visibleSlots() {
            if (side == Direction.DOWN) {
                return new int[] {4};
            }
            if (side == Direction.UP) {
                return new int[] {0};
            }
            if (side != null) {
                return new int[] {1, 2, 3};
            }
            return new int[] {0, 1, 2, 3, 4};
        }
    }
}
