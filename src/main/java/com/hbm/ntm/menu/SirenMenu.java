package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.SirenBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class SirenMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 1;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final SirenBlockEntity blockEntity;

    public SirenMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public SirenMenu(int containerId, Inventory playerInventory, SirenBlockEntity blockEntity) {
        super(ModMenuTypes.SIREN.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(new SlotItemHandler(blockEntity.getMenuItemHandler(), SirenBlockEntity.SLOT_CASSETTE, 8, 35));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    public SirenBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public ItemStack getCassette() {
        return slots.get(0).getItem();
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
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, MACHINE_SLOT_COUNT,
                PLAYER_INVENTORY_START, PLAYER_SLOT_END, 0, MACHINE_SLOT_COUNT);
    }

    private static SirenBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SirenBlockEntity siren) {
            return siren;
        }
        throw new IllegalStateException("Expected siren block entity at " + pos);
    }
}
