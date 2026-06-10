package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RBMKPanelBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RBMKPanelMenu extends AbstractContainerMenu {
    private final RBMKPanelBlockEntity blockEntity;

    public RBMKPanelMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKPanelMenu(int containerId, Inventory inventory, RBMKPanelBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_PANEL.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public RBMKPanelBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 15.0D * 15.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static RBMKPanelBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RBMKPanelBlockEntity panel) {
            return panel;
        }
        throw new IllegalStateException("Expected RBMK RTTY panel block entity at " + pos);
    }
}
