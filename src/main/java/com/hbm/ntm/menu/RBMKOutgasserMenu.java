package com.hbm.ntm.menu;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
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

public class RBMKOutgasserMenu extends AbstractContainerMenu {
    private static final int TILE_SLOTS = 2;
    private final RBMKColumnBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData gasTank;
    private int progress;

    public RBMKOutgasserMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKOutgasserMenu(int containerId, Inventory inventory, RBMKColumnBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_OUTGASSER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.outgasserMenuItems(), 0, 48, 45));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.outgasserMenuItems(),
                1, 112, 69));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 104, 162);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> (int) Math.round(blockEntity.outgasserProgress()),
                value -> progress = value);
        gasTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.outgasserGasTank(),
                blockEntity::hasOperationalLayout);
    }

    public RBMKColumnBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getProgress() {
        return progress;
    }

    public HbmFluidGuiHelper.TankData getGasTank() {
        return gasTank;
    }

    public java.util.List<Component> getGasTankTooltip(boolean showHidden) {
        return gasTank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
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
            if (index == 0) {
                if (!moveItemStackTo(stack, TILE_SLOTS, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
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
        if (blockEntity instanceof RBMKColumnBlockEntity column
                && column.kind() == RBMKColumnBlock.Kind.OUTGASSER) {
            return column;
        }
        throw new IllegalStateException("Expected RBMK outgasser column block entity at " + pos);
    }
}
