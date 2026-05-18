package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class BasicMachineMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 3;
    private static final int PLAYER_INVENTORY_START = 4;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final BasicMachineBlockEntity blockEntity;

    public BasicMachineMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public BasicMachineMenu(int containerId, Inventory playerInventory, BasicMachineBlockEntity blockEntity) {
        super(ModMenuTypes.BASIC_MACHINE.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_FUEL, 26, 35));
        addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_STAMP, 62, 35));
        addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_INPUT, 98, 35));
        addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_OUTPUT, 134, 35));
        addPlayerInventory(playerInventory);
        addMachineDataSlots(blockEntity);
    }

    public int getProgressWidth(int maxWidth) {
        int maxProgress = blockEntity.getMaxProgress();
        if (maxProgress <= 0) {
            return 0;
        }
        return blockEntity.getProgress() * maxWidth / maxProgress;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < PLAYER_INVENTORY_START) {
                if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, PLAYER_INVENTORY_START - 1, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    private void addMachineDataSlots(BasicMachineBlockEntity blockEntity) {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getProgress();
            }

            @Override
            public void set(int value) {
                blockEntity.setProgress(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getMaxProgress();
            }

            @Override
            public void set(int value) {
                blockEntity.setMaxProgress(value);
            }
        });
    }

    private static BasicMachineBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof BasicMachineBlockEntity basicMachine) {
            return basicMachine;
        }
        throw new IllegalStateException("Expected basic machine block entity at " + pos);
    }
}
