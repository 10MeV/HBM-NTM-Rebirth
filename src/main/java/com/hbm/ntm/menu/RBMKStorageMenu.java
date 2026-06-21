package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RBMKStorageMenu extends AbstractContainerMenu {
    private static final int STORAGE_SLOTS = 12;
    private final RBMKColumnBlockEntity blockEntity;

    public RBMKStorageMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKStorageMenu(int containerId, Inventory inventory, RBMKColumnBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_STORAGE.get(), containerId);
        this.blockEntity = blockEntity;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 4; column++) {
                int slot = row + column * 3;
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.storageMenuItems(), slot,
                        32 + 32 * column, 29 + 16 * row));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 104, 162);
    }

    public RBMKColumnBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D)
                && MultiblockHelper.isOperationalCoreLayoutComplete(player.level(), blockEntity.getBlockPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!blockEntity.hasOperationalLayout()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < STORAGE_SLOTS) {
                if (!moveItemStackTo(stack, STORAGE_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, STORAGE_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    private static RBMKColumnBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().isClientSide
                ? MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos)
                : MultiblockHelper.resolveOperationalCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RBMKColumnBlockEntity column && column.kind().storage()) {
            return column;
        }
        throw new IllegalStateException("Expected RBMK storage column block entity at " + pos);
    }
}
