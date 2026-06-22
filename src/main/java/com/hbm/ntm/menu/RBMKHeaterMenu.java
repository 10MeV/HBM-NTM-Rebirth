package com.hbm.ntm.menu;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
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

public class RBMKHeaterMenu extends AbstractContainerMenu {
    private static final int TILE_SLOTS = 1;
    private final RBMKColumnBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData feedTank;
    private final HbmFluidGuiHelper.TankData outputTank;

    public RBMKHeaterMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKHeaterMenu(int containerId, Inventory inventory, RBMKColumnBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_HEATER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.heaterMenuItems(), 0, 41, 45));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 104, 162);
        feedTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.heaterFeedTank(),
                blockEntity::hasOperationalLayout);
        outputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.heaterOutputTank(),
                blockEntity::hasOperationalLayout);
    }

    public RBMKColumnBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getFeedTank() {
        return feedTank;
    }

    public HbmFluidGuiHelper.TankData getOutputTank() {
        return outputTank;
    }

    public List<Component> getFeedTankTooltip(boolean showHidden) {
        return feedTank.tooltip(showHidden);
    }

    public List<Component> getOutputTankTooltip(boolean showHidden) {
        return outputTank.tooltip(showHidden);
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
            if (index < TILE_SLOTS) {
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
        if (blockEntity instanceof RBMKColumnBlockEntity column
                && column.kind() == RBMKColumnBlock.Kind.HEATER) {
            return column;
        }
        throw new IllegalStateException("Expected RBMK heater column block entity at " + pos);
    }
}
