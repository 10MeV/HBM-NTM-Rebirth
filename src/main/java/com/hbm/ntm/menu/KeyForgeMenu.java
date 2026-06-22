package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.KeyForgeBlockEntity;
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

public class KeyForgeMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = KeyForgeBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final KeyForgeBlockEntity blockEntity;

    public KeyForgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public KeyForgeMenu(int containerId, Inventory playerInventory, KeyForgeBlockEntity blockEntity) {
        super(ModMenuTypes.KEY_FORGE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), KeyForgeBlockEntity.SLOT_SOURCE, 44, 35));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), KeyForgeBlockEntity.SLOT_TARGET, 80, 35));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), KeyForgeBlockEntity.SLOT_RANDOMIZE, 116, 35));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    public KeyForgeBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, KeyForgeBlockEntity.SLOT_SOURCE, KeyForgeBlockEntity.SLOT_SOURCE + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static KeyForgeBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof KeyForgeBlockEntity keyForge) {
            return keyForge;
        }
        throw new IllegalStateException("Expected key forge block entity at " + pos);
    }
}
