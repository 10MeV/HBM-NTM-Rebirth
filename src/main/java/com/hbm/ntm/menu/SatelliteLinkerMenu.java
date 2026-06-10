package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.SatelliteLinkerBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SatelliteLinkerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = SatelliteLinkerBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final SatelliteLinkerBlockEntity blockEntity;

    public SatelliteLinkerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public SatelliteLinkerMenu(int containerId, Inventory playerInventory, SatelliteLinkerBlockEntity blockEntity) {
        super(ModMenuTypes.SATELLITE_LINKER.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), SatelliteLinkerBlockEntity.SLOT_SOURCE, 44, 35));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), SatelliteLinkerBlockEntity.SLOT_TARGET, 80, 35));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), SatelliteLinkerBlockEntity.SLOT_RANDOMIZE, 116, 35));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    public SatelliteLinkerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                SatelliteLinkerBlockEntity.SLOT_SOURCE, SatelliteLinkerBlockEntity.SLOT_SOURCE + 1);
    }

    private static SatelliteLinkerBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof SatelliteLinkerBlockEntity linker) {
            return linker;
        }
        throw new IllegalStateException("Expected satellite linker block entity at " + pos);
    }
}
