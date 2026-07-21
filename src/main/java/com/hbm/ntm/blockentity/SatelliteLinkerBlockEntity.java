package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.SatelliteLinkerMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

public class SatelliteLinkerBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_SOURCE = 0;
    public static final int SLOT_TARGET = 1;
    public static final int SLOT_RANDOMIZE = 2;
    public static final int SLOT_COUNT = 3;

    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_CUSTOM_NAME = "name";
    private static final int[] SLOTS_TOP = new int[] { SLOT_SOURCE };
    private static final int[] SLOTS_BOTTOM = new int[] { SLOT_TARGET };
    private static final int[] SLOTS_SIDE = new int[] { SLOT_RANDOMIZE };
    private static final int[] SLOTS_ALL = new int[] { SLOT_SOURCE, SLOT_TARGET, SLOT_RANDOMIZE };

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // TileEntityMachineSatLinker#isItemValidForSlot returns false for
            // all three slots.  The old container uses ordinary Slots, which
            // delegate their placement check to that inventory contract.
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler =
            LazyOptional.of(() -> new ExtractOnlySidedItemHandler(items, SLOTS_ALL));
    private final LazyOptional<IItemHandler> topItemHandler =
            LazyOptional.of(() -> new ExtractOnlySidedItemHandler(items, SLOTS_TOP));
    private final LazyOptional<IItemHandler> bottomItemHandler =
            LazyOptional.of(() -> new ExtractOnlySidedItemHandler(items, SLOTS_BOTTOM));
    private final LazyOptional<IItemHandler> sideItemHandler =
            LazyOptional.of(() -> new ExtractOnlySidedItemHandler(items, SLOTS_SIDE));
    private String customName;

    public SatelliteLinkerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINE_SATLINKER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SatelliteLinkerBlockEntity linker) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;
        ItemStack source = linker.items.getStackInSlot(SLOT_SOURCE);
        ItemStack target = linker.items.getStackInSlot(SLOT_TARGET);
        if (source.getItem() instanceof ISatelliteChip && target.getItem() instanceof ISatelliteChip) {
            // TileEntityMachineSatLinker unconditionally calls setFreqS on the
            // target after reading the source.  Besides copying a changed value,
            // that call materializes the target's legacy "freq" tag when both
            // chips currently report the default zero.  Do not skip this write
            // merely because the two numeric values compare equal.
            ItemStack before = target.copy();
            int sourceFrequency = ISatelliteChip.getFrequencyFromStack(source);
            ISatelliteChip.setFrequencyOnStack(target, sourceFrequency);
            if (!ItemStack.matches(target, before)) {
                changed = true;
            }
        }

        ItemStack randomize = linker.items.getStackInSlot(SLOT_RANDOMIZE);
        if (randomize.getItem() instanceof ISatelliteChip) {
            OptionalInt availableFrequency = SatelliteSavedData.get(serverLevel).randomAvailableFrequency(level.random);
            // The legacy linker does not read the randomizer stack before the
            // availability check.  A taken candidate therefore leaves a
            // tag-less chip untouched; an available candidate is written even
            // when it happens to equal the chip's current numeric frequency.
            if (availableFrequency.isPresent()) {
                ItemStack before = randomize.copy();
                ISatelliteChip.setFrequencyOnStack(randomize, availableFrequency.getAsInt());
                if (!ItemStack.matches(randomize, before)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            linker.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        if (customName != null && !customName.isEmpty()) {
            return Component.literal(customName);
        }
        return Component.translatable("container.satLinker");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SatelliteLinkerMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        // TileEntityMachineSatLinker writes every non-null customName, while
        // hasCustomInventoryName separately decides that an empty value should
        // display the default title.
        if (customName != null) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_INVENTORY, Tag.TAG_COMPOUND)) {
            HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        } else if (tag.contains(TAG_INVENTORY, Tag.TAG_LIST)) {
            HbmInventoryMenuHelper.loadLegacyItems(tag, TAG_INVENTORY, items);
        } else {
            HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        }
        customName = tag.getString(TAG_CUSTOM_NAME);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        topItemHandler.invalidate();
        bottomItemHandler.invalidate();
        sideItemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return getItemHandler(side).cast();
        }
        return super.getCapability(capability, side);
    }

    private LazyOptional<IItemHandler> getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return itemHandler;
        }
        if (side == Direction.UP) {
            return topItemHandler;
        }
        if (side == Direction.DOWN) {
            return bottomItemHandler;
        }
        return sideItemHandler;
    }

    private static final class ExtractOnlySidedItemHandler implements IItemHandler {
        private final ItemStackHandler items;
        private final int[] slots;

        private ExtractOnlySidedItemHandler(ItemStackHandler items, int[] slots) {
            this.items = items;
            this.slots = slots;
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(mapSlot(slot));
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return items.extractItem(mapSlot(slot), amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(mapSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        private int mapSlot(int slot) {
            if (slot < 0 || slot >= slots.length) {
                throw new IndexOutOfBoundsException(slot);
            }
            return slots[slot];
        }
    }
}
