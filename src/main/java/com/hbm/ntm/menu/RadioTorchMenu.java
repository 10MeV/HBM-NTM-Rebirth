package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RadioTorchBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RadioTorchMenu extends AbstractContainerMenu {
    private final RadioTorchBlockEntity blockEntity;

    public RadioTorchMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RadioTorchMenu(int containerId, Inventory inventory, RadioTorchBlockEntity blockEntity) {
        super(ModMenuTypes.RADIO_TORCH.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public RadioTorchBlockEntity getBlockEntity() {
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

    private static RadioTorchBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RadioTorchBlockEntity torch) {
            return torch;
        }
        throw new IllegalStateException("Expected RTTY torch block entity at " + pos);
    }
}
