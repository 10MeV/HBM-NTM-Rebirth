package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.item.RBMKFuelRodItem;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class RBMKRodMenu extends AbstractContainerMenu {
    private static final int TILE_SLOTS = 1;
    private final RBMKColumnBlockEntity blockEntity;

    public RBMKRodMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKRodMenu(int containerId, Inventory inventory, RBMKColumnBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_ROD.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(new FuelRodSlot(blockEntity.rodItems(), 0, 80, 45, blockEntity));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 104, 162);
    }

    public RBMKColumnBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D)
                && MultiblockHelper.isOperationalCoreLayoutComplete(player.level(), blockEntity.getBlockPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!blockEntity.hasOperationalLayout()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < TILE_SLOTS) {
                if (!player.getAbilities().instabuild && !blockEntity.canManualUnloadFuelRod()) {
                    return ItemStack.EMPTY;
                }
                if (!moveItemStackTo(stack, TILE_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, TILE_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    private static RBMKColumnBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().isClientSide
                ? MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos)
                : MultiblockHelper.resolveOperationalCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RBMKColumnBlockEntity column && column.kind().rod()) {
            return column;
        }
        throw new IllegalStateException("Expected RBMK fuel column block entity at " + pos);
    }

    private static class FuelRodSlot extends SlotItemHandler {
        private final RBMKColumnBlockEntity blockEntity;

        FuelRodSlot(IItemHandler items, int slot, int x, int y, RBMKColumnBlockEntity blockEntity) {
            super(items, slot, x, y);
            this.blockEntity = blockEntity;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return stack.getItem() instanceof RBMKFuelRodItem && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(Player player) {
            return player.getAbilities().instabuild || blockEntity.canManualUnloadFuelRod();
        }
    }
}
