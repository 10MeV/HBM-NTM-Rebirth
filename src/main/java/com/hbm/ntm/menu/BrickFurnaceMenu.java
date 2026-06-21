package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.BrickFurnaceBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BrickFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 4;
    private static final int PLAYER_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;
    private final BrickFurnaceBlockEntity blockEntity;
    private int burnTime;
    private int maxBurnTime;
    private int progress;

    public BrickFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public BrickFurnaceMenu(int containerId, Inventory playerInventory, BrickFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.BRICK_FURNACE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 53, 17));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 53, 53));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 2, 125, 35));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 3, 71, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBurnTime, value -> burnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxBurnTime, value -> maxBurnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
    }

    public int getBurnWidth(int width) {
        return maxBurnTime <= 0 ? 0 : burnTime * width / maxBurnTime;
    }

    public int getProgressWidth(int width) {
        return progress * width / BrickFurnaceBlockEntity.PROCESS_TIME;
    }

    public BrickFurnaceBlockEntity getBlockEntity() {
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
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 1, 2, false) && !moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static BrickFurnaceBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof BrickFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected brick furnace at " + pos);
    }
}
