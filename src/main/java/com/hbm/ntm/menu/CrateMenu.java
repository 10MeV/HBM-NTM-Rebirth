package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.StorageCrateBlockEntity;
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

public class CrateMenu extends AbstractContainerMenu {
    private final StorageCrateBlockEntity blockEntity;
    private final int slotCount;

    public CrateMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public CrateMenu(int containerId, Inventory playerInventory, StorageCrateBlockEntity blockEntity) {
        super(ModMenuTypes.STORAGE_CRATE.get(), containerId);
        this.blockEntity = blockEntity;
        this.slotCount = blockEntity.slotCount();
        int rows = blockEntity.kind().rows();
        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 8, 18, rows, 9, 18);
        int playerInvY = 18 + rows * 18 + 14;
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, playerInvY,
                playerInvY + 58);
        blockEntity.playOpenSound();
    }

    public StorageCrateBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int rows() {
        return blockEntity.kind().rows();
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < slotCount) {
                if (!moveItemStackTo(stack, slotCount, slotCount + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, slotCount, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        blockEntity.playCloseSound();
    }

    private static StorageCrateBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof StorageCrateBlockEntity crate) {
            return crate;
        }
        throw new IllegalStateException("Expected storage crate block entity at " + pos);
    }
}
