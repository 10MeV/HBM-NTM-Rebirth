package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.AmmoPressBlockEntity;
import com.hbm.ntm.recipe.AmmoPressRecipe;
import com.hbm.ntm.recipe.AmmoPressRecipeRuntime;
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

public class AmmoPressMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = AmmoPressBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final AmmoPressBlockEntity blockEntity;
    private int selectedRecipe;

    public AmmoPressMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public AmmoPressMenu(int containerId, Inventory playerInventory, AmmoPressBlockEntity blockEntity) {
        super(ModMenuTypes.AMMO_PRESS.get(), containerId);
        this.blockEntity = blockEntity;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = row * 3 + column;
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), slot,
                        116 + column * 18, 18 + row * 18));
            }
        }
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                AmmoPressBlockEntity.SLOT_OUTPUT, 134, 72));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 118, 176);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSelectedRecipeIndex,
                value -> selectedRecipe = value);
    }

    public AmmoPressBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getSelectedRecipeIndex() {
        return selectedRecipe;
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
        AmmoPressRecipe recipe = blockEntity.getSelectedRecipe();
        if (recipe == null) {
            return false;
        }
        for (int slot = 0; slot < AmmoPressRecipe.INPUT_SLOTS; slot++) {
            if (recipe.matchesSlot(slot, stack)
                    && HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, slot, slot + 1)) {
                return true;
            }
        }
        return false;
    }

    private static AmmoPressBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AmmoPressBlockEntity press) {
            return press;
        }
        throw new IllegalStateException("Expected ammo press block entity at " + pos);
    }
}
