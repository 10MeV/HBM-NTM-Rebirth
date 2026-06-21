package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RadGenBlockEntity;
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

public class RadGenMenu extends AbstractContainerMenu {
    private static final int LANES = 12;
    private static final int MACHINE_SLOT_COUNT = 24;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final RadGenBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private boolean on;
    private int output;
    private final int[] progress = new int[LANES];
    private final int[] maxProgress = new int[LANES];
    private final int[] production = new int[LANES];

    public RadGenMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RadGenMenu(int containerId, Inventory playerInventory, RadGenBlockEntity blockEntity) {
        super(ModMenuTypes.RADGEN.get(), containerId);
        this.blockEntity = blockEntity;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                int lane = col + row * 3;
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), lane,
                        8 + col * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 3; col++) {
                int lane = col + row * 3;
                addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), lane + LANES,
                        116 + col * 18, 17 + row * 18));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 102, 160);
        addDataSlots();
    }

    public RadGenBlockEntity getBlockEntity() {
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

    public int getPowerBarWidth(int maxWidth) {
        return maxPower <= 0L ? 0 : (int) (power * maxWidth / maxPower);
    }

    public boolean isOn() {
        return on;
    }

    public int getOutput() {
        return output;
    }

    public int getProgressWidth(int lane, int maxWidth) {
        return maxProgress[lane] <= 0 ? 0 : progress[lane] * maxWidth / maxProgress[lane];
    }

    public boolean hasLaneProgress(int lane) {
        return lane >= 0 && lane < LANES && maxProgress[lane] > 0;
    }

    public int getRemainingTicks(int lane) {
        return hasLaneProgress(lane) ? Math.max(0, maxProgress[lane] - progress[lane]) : 0;
    }

    public int getRemainingPercent(int lane) {
        return hasLaneProgress(lane) ? getRemainingTicks(lane) * 100 / maxProgress[lane] : 0;
    }

    public int getProduction(int lane) {
        return lane >= 0 && lane < LANES ? production[lane] : 0;
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0, LANES)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOn, value -> on = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getOutput, value -> output = value);
        for (int lane = 0; lane < LANES; lane++) {
            int index = lane;
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getProgress(index),
                    value -> progress[index] = value);
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getMaxProgress(index),
                    value -> maxProgress[index] = value);
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getProduction(index),
                    value -> production[index] = value);
        }
    }

    private static RadGenBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RadGenBlockEntity radGen) {
            return radGen;
        }
        throw new IllegalStateException("Expected radgen block entity at " + pos);
    }
}
