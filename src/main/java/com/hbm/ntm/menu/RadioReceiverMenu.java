package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RadioReceiverBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RadioReceiverMenu extends AbstractContainerMenu {
    private final RadioReceiverBlockEntity blockEntity;

    public RadioReceiverMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RadioReceiverMenu(int containerId, Inventory inventory, RadioReceiverBlockEntity blockEntity) {
        super(ModMenuTypes.RADIO_RECEIVER.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public RadioReceiverBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static RadioReceiverBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RadioReceiverBlockEntity receiver) {
            return receiver;
        }
        throw new IllegalStateException("Expected radio receiver block entity at " + pos);
    }
}
