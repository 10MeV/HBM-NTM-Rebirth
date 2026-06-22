package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ShredderBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.item.ShredderBladeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

public class ShredderMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ShredderBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ShredderBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int leftGear;
    private int rightGear;

    public ShredderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ShredderMenu(int containerId, Inventory playerInventory, ShredderBlockEntity blockEntity) {
        super(ModMenuTypes.SHREDDER.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(playerInventory, blockEntity.getItems());
        addDataSlots();
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
        return progress * maxWidth / ShredderBlockEntity.PROCESSING_SPEED;
    }

    public int getLeftGear() {
        return leftGear;
    }

    public int getRightGear() {
        return rightGear;
    }

    public boolean bladesBrokenOrMissing() {
        return leftGear == 0 || leftGear == 3 || rightGear == 0 || rightGear == 3;
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
        } else if (ShredderBlockEntity.isLegacyBattery(stack)) {
            if (!moveItemStackTo(stack, ShredderBlockEntity.SLOT_BATTERY,
                    ShredderBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof ShredderBladeItem) {
            if (!moveItemStackTo(stack, ShredderBlockEntity.SLOT_BLADE_LEFT,
                    ShredderBlockEntity.SLOT_BLADE_RIGHT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, ShredderBlockEntity.SLOT_INPUT_START,
                ShredderBlockEntity.SLOT_INPUT_END + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addMachineSlots(Inventory playerInventory, ItemStackHandler items) {
        HbmInventoryMenuHelper.addSlots(this::addSlot, items, 0, 44, 18, 3, 3);
        HbmInventoryMenuHelper.addCraftingOutputSlots(this::addSlot, playerInventory.player, items, 9, 116, 18, 6, 3);
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, ShredderBlockEntity.SLOT_BLADE_LEFT, 44, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, ShredderBlockEntity.SLOT_BLADE_RIGHT, 80, 108));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, ShredderBlockEntity.SLOT_BATTERY, 8, 108));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 151, 209);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(),
                () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(),
                () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getGearLeft, value -> leftGear = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getGearRight, value -> rightGear = value);
    }

    private static ShredderBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ShredderBlockEntity shredder) {
            return shredder;
        }
        throw new IllegalStateException("Expected shredder block entity at " + pos);
    }
}
