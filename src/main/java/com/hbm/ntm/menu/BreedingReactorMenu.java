package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.BreedingReactorBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.recipe.BreedingReactorRecipeRuntime;
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

public class BreedingReactorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 2;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final BreedingReactorBlockEntity blockEntity;
    private int flux;
    private int progressScaled;

    public BreedingReactorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public BreedingReactorMenu(int containerId, Inventory playerInventory, BreedingReactorBlockEntity blockEntity) {
        super(ModMenuTypes.BREEDING_REACTOR.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 35, 35));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 1, 125, 35));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFlux, value -> flux = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgressScaled, value -> progressScaled = value);
    }

    public BreedingReactorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getFlux() {
        return flux;
    }

    public int getProgressWidth(int width) {
        return progressScaled * width / 10_000;
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
        } else if (BreedingReactorRecipeRuntime.isInput(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static BreedingReactorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof BreedingReactorBlockEntity breeder) {
            return breeder;
        }
        throw new IllegalStateException("Expected breeding reactor block entity at " + pos);
    }
}
