package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RBMKConsoleBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RBMKConsoleMenu extends AbstractContainerMenu {
    private final RBMKConsoleBlockEntity blockEntity;

    public RBMKConsoleMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKConsoleMenu(int containerId, Inventory inventory, RBMKConsoleBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_CONSOLE.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public RBMKConsoleBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 20.0D * 20.0D
                && MultiblockHelper.isOperationalCoreLayoutComplete(player.level(), blockEntity.getBlockPos());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static RBMKConsoleBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().isClientSide
                ? MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos)
                : MultiblockHelper.resolveOperationalCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RBMKConsoleBlockEntity console) {
            return console;
        }
        throw new IllegalStateException("Expected RBMK console block entity at " + pos);
    }
}
