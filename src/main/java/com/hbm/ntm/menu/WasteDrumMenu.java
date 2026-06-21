package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.WasteDrumBlockEntity;
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
import net.minecraftforge.items.SlotItemHandler;

public class WasteDrumMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = WasteDrumBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;
    private static final int[][] SLOT_POSITIONS = {
            { 71, 21 }, { 89, 21 },
            { 53, 39 }, { 71, 39 }, { 89, 39 }, { 107, 39 },
            { 53, 57 }, { 71, 57 }, { 89, 57 }, { 107, 57 },
            { 71, 75 }, { 89, 75 }
    };

    private final WasteDrumBlockEntity blockEntity;

    public WasteDrumMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public WasteDrumMenu(int containerId, Inventory playerInventory, WasteDrumBlockEntity blockEntity) {
        super(ModMenuTypes.WASTE_DRUM.get(), containerId);
        this.blockEntity = blockEntity;
        for (int i = 0; i < SLOT_POSITIONS.length; i++) {
            addSlot(new SlotItemHandler(blockEntity.getItems(), i, SLOT_POSITIONS[i][0], SLOT_POSITIONS[i][1]));
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 107, 165);
    }

    public WasteDrumBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
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
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, MACHINE_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static WasteDrumBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof WasteDrumBlockEntity drum) {
            return drum;
        }
        throw new IllegalStateException("Expected waste drum block entity at " + pos);
    }
}
