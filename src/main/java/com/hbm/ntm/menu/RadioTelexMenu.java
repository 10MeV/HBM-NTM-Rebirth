package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RadioTelexBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RadioTelexMenu extends AbstractContainerMenu {
    private final RadioTelexBlockEntity blockEntity;

    public RadioTelexMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RadioTelexMenu(int containerId, Inventory playerInventory, RadioTelexBlockEntity blockEntity) {
        super(ModMenuTypes.RADIO_TELEX.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public RadioTelexBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 16.0D * 16.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static RadioTelexBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof RadioTelexBlockEntity telex) {
            return telex;
        }
        throw new IllegalStateException("Expected telex block entity at " + pos);
    }
}
