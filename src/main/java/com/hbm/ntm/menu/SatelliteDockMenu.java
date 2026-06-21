package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.SatelliteDockBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.satellite.ISatelliteChip;
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
import net.minecraftforge.items.SlotItemHandler;

public class SatelliteDockMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = SatelliteDockBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final SatelliteDockBlockEntity blockEntity;

    public SatelliteDockMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public SatelliteDockMenu(int containerId, Inventory playerInventory, SatelliteDockBlockEntity blockEntity) {
        super(ModMenuTypes.SATELLITE_DOCK.get(), containerId);
        this.blockEntity = blockEntity;

        HbmInventoryMenuHelper.addTakeOnlySlots(this::addSlot, blockEntity.getItems(), 0, 62, 17, 3, 5, 18);
        addSlot(new SlotItemHandler(blockEntity.getItems(), SatelliteDockBlockEntity.SLOT_CHIP, 26, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ISatelliteChip;
            }
        });
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    public SatelliteDockBlockEntity getBlockEntity() {
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
                0, SatelliteDockBlockEntity.OUTPUT_SLOT_COUNT);
    }

    private static SatelliteDockBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof SatelliteDockBlockEntity dock) {
            return dock;
        }
        throw new IllegalStateException("Expected satellite dock block entity at " + pos);
    }
}
