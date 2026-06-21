package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.AshpitBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AshpitMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = AshpitBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final AshpitBlockEntity blockEntity;

    public AshpitMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public AshpitMenu(int containerId, Inventory playerInventory, AshpitBlockEntity blockEntity) {
        super(ModMenuTypes.ASHPIT.get(), containerId);
        this.blockEntity = blockEntity;

        for (int slot = 0; slot < MACHINE_SLOT_COUNT; slot++) {
            addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.getItems(), slot, 44 + slot * 18, 27));
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 86, 144);
    }

    public AshpitBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        blockEntity.closeMenu(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, MACHINE_SLOT_COUNT,
                PLAYER_INVENTORY_START, PLAYER_SLOT_END);
    }

    private static AshpitBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof AshpitBlockEntity ashpit) {
            return ashpit;
        }
        throw new IllegalStateException("Expected ashpit block entity at " + pos);
    }
}
