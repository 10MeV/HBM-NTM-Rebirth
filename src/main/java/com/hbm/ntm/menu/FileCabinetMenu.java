package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.LegacyFileCabinetBlockEntity;
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

public class FileCabinetMenu extends AbstractContainerMenu {
    private final LegacyFileCabinetBlockEntity blockEntity;

    public FileCabinetMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public FileCabinetMenu(int containerId, Inventory playerInventory, LegacyFileCabinetBlockEntity blockEntity) {
        super(ModMenuTypes.FILE_CABINET.get(), containerId);
        this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 53, 18, 2, 4);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 88, 146);
        blockEntity.openInventory();
    }

    public LegacyFileCabinetBlockEntity getBlockEntity() {
        return blockEntity;
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
            if (index < LegacyFileCabinetBlockEntity.SLOT_COUNT) {
                if (!moveItemStackTo(stack, LegacyFileCabinetBlockEntity.SLOT_COUNT,
                        LegacyFileCabinetBlockEntity.SLOT_COUNT + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, LegacyFileCabinetBlockEntity.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        blockEntity.closeInventory();
    }

    private static LegacyFileCabinetBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof LegacyFileCabinetBlockEntity cabinet) {
            return cabinet;
        }
        throw new IllegalStateException("Expected file cabinet block entity at " + pos);
    }
}
