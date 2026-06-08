package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.SoyuzCapsuleBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SoyuzCapsuleMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = SoyuzCapsuleBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final SoyuzCapsuleBlockEntity blockEntity;

    public SoyuzCapsuleMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public SoyuzCapsuleMenu(int containerId, Inventory playerInventory, SoyuzCapsuleBlockEntity blockEntity) {
        super(ModMenuTypes.SOYUZ_CAPSULE.get(), containerId);
        this.blockEntity = blockEntity;

        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 44, 17, 3, 6);
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(), SoyuzCapsuleBlockEntity.SLOT_ROCKET, 8, 35));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    public SoyuzCapsuleBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                0, MACHINE_SLOT_COUNT);
    }

    private static SoyuzCapsuleBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SoyuzCapsuleBlockEntity capsule) {
            return capsule;
        }
        throw new IllegalStateException("Expected Soyuz capsule block entity at " + pos);
    }
}
