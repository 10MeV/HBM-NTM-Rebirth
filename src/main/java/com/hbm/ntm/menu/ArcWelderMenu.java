package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.ArcWelderBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class ArcWelderMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 8;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ArcWelderBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private long consumption;
    private int progress;
    private int processTime;
    private HbmFluidGuiHelper.TankData inputTank;

    public ArcWelderMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ArcWelderMenu(int containerId, Inventory playerInventory, ArcWelderBlockEntity blockEntity) {
        super(ModMenuTypes.ARC_WELDER.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ArcWelderBlockEntity.SLOT_INPUT_0, 17, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ArcWelderBlockEntity.SLOT_INPUT_1, 35, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ArcWelderBlockEntity.SLOT_INPUT_2, 53, 36));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), ArcWelderBlockEntity.SLOT_OUTPUT, 107, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ArcWelderBlockEntity.SLOT_BATTERY, 152, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                ArcWelderBlockEntity.SLOT_FLUID_IDENTIFIER, 17, 63));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), ArcWelderBlockEntity.SLOT_UPGRADE_0, 89, 63));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), ArcWelderBlockEntity.SLOT_UPGRADE_1, 107, 63));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
        addDataSlots();
    }

    public ArcWelderBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public long getConsumption() {
        return consumption;
    }

    public int getProgressWidth(int maxWidth) {
        return processTime <= 0 ? 0 : progress * maxWidth / processTime;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public HbmFluidGuiHelper.TankData getInputTankData() {
        return inputTank;
    }

    public List<Component> getInputTankTooltip(boolean showHidden) {
        return inputTank.tooltip(showHidden);
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!movePlayerStackToMachine(stack)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private boolean movePlayerStackToMachine(ItemStack stack) {
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ArcWelderBlockEntity.SLOT_BATTERY, ArcWelderBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ArcWelderBlockEntity.SLOT_FLUID_IDENTIFIER, ArcWelderBlockEntity.SLOT_FLUID_IDENTIFIER + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    ArcWelderBlockEntity.SLOT_UPGRADE_0, ArcWelderBlockEntity.SLOT_UPGRADE_1 + 1);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                ArcWelderBlockEntity.SLOT_INPUT_0, ArcWelderBlockEntity.SLOT_INPUT_2 + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(), () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getConsumption(), () -> consumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getProgress(), value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getProcessTime(), value -> processTime = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
    }

    private static ArcWelderBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ArcWelderBlockEntity arcWelder) {
            return arcWelder;
        }
        throw new IllegalStateException("Expected arc welder block entity at " + pos);
    }
}
