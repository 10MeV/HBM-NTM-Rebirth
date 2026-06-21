package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RadioAutocalBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RadioAutocalMenu extends AbstractContainerMenu {
    private final RadioAutocalBlockEntity blockEntity;

    public RadioAutocalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RadioAutocalMenu(int containerId, Inventory playerInventory, RadioAutocalBlockEntity blockEntity) {
        super(ModMenuTypes.RADIO_AUTOCAL.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public RadioAutocalBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 15.0D * 15.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static RadioAutocalBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RadioAutocalBlockEntity autocal) {
            return autocal;
        }
        throw new IllegalStateException("Expected AUTOCAL block entity at " + pos);
    }
}
