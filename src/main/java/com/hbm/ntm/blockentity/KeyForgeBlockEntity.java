package com.hbm.ntm.blockentity;

import com.hbm.ntm.menu.KeyForgeMenu;
import com.hbm.ntm.item.KeyPinItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KeyForgeBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_SOURCE = 0;
    public static final int SLOT_TARGET = 1;
    public static final int SLOT_RANDOMIZE = 2;
    public static final int SLOT_COUNT = 3;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> menuItems = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> topItems = LazyOptional.of(() -> new ExtractionOnlySideHandler(SLOT_SOURCE));
    private final LazyOptional<IItemHandler> bottomItems = LazyOptional.of(() -> new ExtractionOnlySideHandler(SLOT_TARGET));
    private final LazyOptional<IItemHandler> sideItems = LazyOptional.of(() -> new ExtractionOnlySideHandler(SLOT_RANDOMIZE));

    public KeyForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KEY_FORGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, KeyForgeBlockEntity keyForge) {
        if (level.isClientSide) {
            return;
        }
        ItemStack source = keyForge.items.getStackInSlot(SLOT_SOURCE);
        ItemStack target = keyForge.items.getStackInSlot(SLOT_TARGET);
        if (KeyPinItem.canTransfer(source) && KeyPinItem.canTransfer(target)) {
            KeyPinItem.setPins(target, KeyPinItem.getPins(source));
            keyForge.items.setStackInSlot(SLOT_TARGET, target);
        }
        ItemStack randomize = keyForge.items.getStackInSlot(SLOT_RANDOMIZE);
        if (KeyPinItem.canTransfer(randomize)) {
            KeyPinItem.setPins(randomize, level.random.nextInt(900) + 100);
            keyForge.items.setStackInSlot(SLOT_RANDOMIZE, randomize);
        }
        if (level.getGameTime() % 20L == 0L) {
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
        return Component.translatableWithFallback("container.machineKeyForge", "Key Forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new KeyForgeMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "Inventory", items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "Inventory", items);
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
        menuItems.invalidate();
        topItems.invalidate();
        bottomItems.invalidate();
        sideItems.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return menuItems.cast();
            }
            if (side == Direction.UP) {
                return topItems.cast();
            }
            return side == Direction.DOWN ? bottomItems.cast() : sideItems.cast();
        }
        return super.getCapability(capability, side);
    }

    private final class ExtractionOnlySideHandler implements IItemHandler {
        private final int mappedSlot;

        private ExtractionOnlySideHandler(int mappedSlot) {
            this.mappedSlot = mappedSlot;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot == 0 ? items.getStackInSlot(mappedSlot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 ? items.extractItem(mappedSlot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == 0 ? items.getSlotLimit(mappedSlot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    }
}
