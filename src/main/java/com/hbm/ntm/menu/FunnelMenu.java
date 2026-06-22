package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.FunnelBlockEntity;
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

public class FunnelMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = FunnelBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final FunnelBlockEntity blockEntity;
    private int mode;

    public FunnelMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public FunnelMenu(int containerId, Inventory playerInventory, FunnelBlockEntity blockEntity) {
        super(ModMenuTypes.FUNNEL.get(), containerId);
        this.blockEntity = blockEntity;
        for (int slot = 0; slot < 9; slot++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                    FunnelBlockEntity.SLOT_INPUT_START + slot, 8 + 18 * slot, 18));
        }
        for (int slot = 0; slot < 9; slot++) {
            addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                    FunnelBlockEntity.SLOT_OUTPUT_START + slot, 8 + 18 * slot, 54));
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 86, 144);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMode, value -> mode = value);
    }

    public FunnelBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getMode() {
        return mode;
    }

    public String getModeLabel() {
        return switch (mode) {
            case FunnelBlockEntity.MODE_3X3 -> "3x3 only";
            case FunnelBlockEntity.MODE_2X2 -> "2x2 only";
            default -> "3x3 then 2x2";
        };
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
        } else if (!moveItemStackTo(stack, FunnelBlockEntity.SLOT_INPUT_START, FunnelBlockEntity.SLOT_INPUT_END + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static FunnelBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof FunnelBlockEntity funnel) {
            return funnel;
        }
        throw new IllegalStateException("Expected funnel block entity at " + pos);
    }
}
