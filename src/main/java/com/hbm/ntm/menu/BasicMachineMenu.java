package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.BasicMachineBlockEntity;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.SlotItemHandler;

public class BasicMachineMenu extends AbstractContainerMenu {
    private static final int PLAYER_INVENTORY_START = 13;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final BasicMachineBlockEntity blockEntity;

    public BasicMachineMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public BasicMachineMenu(int containerId, Inventory playerInventory, BasicMachineBlockEntity blockEntity) {
        super(ModMenuTypes.BASIC_MACHINE.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_FUEL, 26, 53));
        addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_STAMP, 80, 17));
        addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_INPUT, 80, 53));
        addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_OUTPUT, 140, 35));
        for (int column = 0; column < 9; column++) {
            addSlot(new SlotItemHandler(blockEntity.getItems(), BasicMachineBlockEntity.SLOT_STORAGE_START + column, 8 + column * 18, 84));
        }
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

    public int getPressHeight(int maxHeight) {
        return blockEntity.getPress() * maxHeight / BasicMachineBlockEntity.MAX_PRESS;
    }

    public int getSpeedPercent() {
        return blockEntity.getSpeed() * 100 / BasicMachineBlockEntity.MAX_SPEED;
    }

    public int getStoredOperations() {
        return blockEntity.getBurnTime() / BasicMachineBlockEntity.FUEL_PER_OPERATION;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
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
            } else if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0) {
                if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                        BasicMachineBlockEntity.SLOT_FUEL, BasicMachineBlockEntity.SLOT_FUEL + 1,
                        BasicMachineBlockEntity.SLOT_STORAGE_START, BasicMachineBlockEntity.SLOT_STORAGE_END)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof ItemPressStamp) {
                if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                        BasicMachineBlockEntity.SLOT_STAMP, BasicMachineBlockEntity.SLOT_STAMP + 1,
                        BasicMachineBlockEntity.SLOT_STORAGE_START, BasicMachineBlockEntity.SLOT_STORAGE_END)) {
                    return ItemStack.EMPTY;
                }
            } else if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    BasicMachineBlockEntity.SLOT_INPUT, BasicMachineBlockEntity.SLOT_INPUT + 1,
                    BasicMachineBlockEntity.SLOT_STORAGE_START, BasicMachineBlockEntity.SLOT_STORAGE_END)) {
                return ItemStack.EMPTY;
            }

            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 120 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 178));
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
                return blockEntity.getBurnTime();
            }

            @Override
            public void set(int value) {
                blockEntity.setBurnTime(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getSpeed();
            }

            @Override
            public void set(int value) {
                blockEntity.setSpeed(value);
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getPress();
            }

            @Override
            public void set(int value) {
                blockEntity.setPress(value);
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
