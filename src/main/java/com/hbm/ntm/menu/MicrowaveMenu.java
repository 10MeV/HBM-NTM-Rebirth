package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.MicrowaveBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
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

public class MicrowaveMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = MicrowaveBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final MicrowaveBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int time;
    private int speed;

    public MicrowaveMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public MicrowaveMenu(int containerId, Inventory playerInventory, MicrowaveBlockEntity blockEntity) {
        super(ModMenuTypes.MICROWAVE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), MicrowaveBlockEntity.SLOT_INPUT, 80, 35));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), MicrowaveBlockEntity.SLOT_OUTPUT, 140, 35));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), MicrowaveBlockEntity.SLOT_BATTERY, 8, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public MicrowaveBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressWidth(int maxWidth) {
        return time * maxWidth / MicrowaveBlockEntity.MAX_TIME;
    }

    public int getSpeed() {
        return speed;
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
        } else {
            if (!moveItemStackTo(stack, MicrowaveBlockEntity.SLOT_INPUT, MicrowaveBlockEntity.SLOT_INPUT + 1, true)
                    && !moveItemStackTo(stack, MicrowaveBlockEntity.SLOT_BATTERY,
                    MicrowaveBlockEntity.SLOT_BATTERY + 1, true)) {
                return ItemStack.EMPTY;
            }
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getTime, value -> time = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSpeed, value -> speed = value);
    }

    private static MicrowaveBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof MicrowaveBlockEntity microwave) {
            return microwave;
        }
        throw new IllegalStateException("Expected microwave block entity at " + pos);
    }
}
