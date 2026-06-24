package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.FelBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FelMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = FelBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final FelBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int mode;
    private int on;
    private int missingValidSilex;
    private int distance;

    public FelMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public FelMenu(int containerId, Inventory inventory, FelBlockEntity blockEntity) {
        super(ModMenuTypes.FEL.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), FelBlockEntity.SLOT_BATTERY, 182, 144));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), FelBlockEntity.SLOT_CRYSTAL, 141, 23));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 83, 141);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getMode().ordinal(), value -> mode = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isOn() ? 1 : 0, value -> on = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isMissingValidSilex() ? 1 : 0,
                value -> missingValidSilex = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getDistance, value -> distance = value);
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getModeOrdinal() {
        return mode;
    }

    public boolean isOn() {
        return on != 0;
    }

    public boolean isMissingValidSilex() {
        return missingValidSilex != 0;
    }

    public boolean isBeamActive() {
        return isOn() && mode > 0 && power > FelBlockEntity.visualBeamPowerRequest(mode) && distance > 0;
    }

    public FelBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
            if (!moveItemStackTo(stack, FelBlockEntity.SLOT_BATTERY, FelBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, FelBlockEntity.SLOT_CRYSTAL, FelBlockEntity.SLOT_CRYSTAL + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static FelBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof FelBlockEntity fel) {
            return fel;
        }
        throw new IllegalStateException("Expected FEL block entity at " + pos);
    }
}
