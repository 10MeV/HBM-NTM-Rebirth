package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.SilexBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
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

public class SilexMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = SilexBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final SilexBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData tank;
    private int currentFill;
    private int progress;
    private int mode;

    public SilexMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public SilexMenu(int containerId, Inventory inventory, SilexBlockEntity blockEntity) {
        super(ModMenuTypes.SILEX.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), SilexBlockEntity.SLOT_INPUT, 80, 12));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), SilexBlockEntity.SLOT_IDENTIFIER, 8, 24));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), SilexBlockEntity.SLOT_CONTAINER_IN, 26, 24));
        addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.getItems(), SilexBlockEntity.SLOT_CONTAINER_OUT, 44, 24));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), SilexBlockEntity.SLOT_OUTPUT, 116, 90));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 5, 134, 72));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 6, 152, 72));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 7, 134, 90));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 8, 152, 90));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 9, 134, 108));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(), 10, 152, 108));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 140, 198);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getCurrentFill, value -> currentFill = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getMode().ordinal(), value -> mode = value);
    }

    public HbmFluidGuiHelper.TankData getTank() {
        return tank;
    }

    public int getCurrentFill() {
        return currentFill;
    }

    public int getProgressWidth(int maxWidth) {
        return progress * maxWidth / SilexBlockEntity.PROCESS_TIME;
    }

    public int getCurrentFillHeight(int maxHeight) {
        return currentFill * maxHeight / SilexBlockEntity.MAX_FILL;
    }

    public int getModeOrdinal() {
        return mode;
    }

    public SilexBlockEntity getBlockEntity() {
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof IFluidIdentifierItem) {
            if (!moveItemStackTo(stack, SilexBlockEntity.SLOT_IDENTIFIER, SilexBlockEntity.SLOT_IDENTIFIER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, SilexBlockEntity.SLOT_CONTAINER_IN, SilexBlockEntity.SLOT_CONTAINER_IN + 1, false)
                && !moveItemStackTo(stack, SilexBlockEntity.SLOT_INPUT, SilexBlockEntity.SLOT_INPUT + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static SilexBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof SilexBlockEntity silex) {
            return silex;
        }
        throw new IllegalStateException("Expected SILEX block entity at " + pos);
    }
}
