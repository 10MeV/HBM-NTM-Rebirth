package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.SatelliteDockBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.satellite.SatelliteChipItem;
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
                // ContainerSatDock accepts ItemSatChip and its subclasses, not
                // every ISatChip implementation (notably the armor mod lens).
                return stack.getItem() instanceof SatelliteChipItem;
            }
        });
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    public SatelliteDockBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, HbmInventoryMenuHelper.legacyMenuUseDistanceSqr(blockEntity));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!HbmInventoryMenuHelper.legacyMergeItemStack(slots, stack, PLAYER_INVENTORY_START,
                    PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!HbmInventoryMenuHelper.legacyMergeItemStack(slots, stack, 0,
                SatelliteDockBlockEntity.OUTPUT_SLOT_COUNT, false)) {
            // ContainerSatDock's player branch is precisely [0, 15): it may
            // merge into an existing take-only output before placement checks,
            // but it cannot populate an empty output or shift-click into slot 15.
            return ItemStack.EMPTY;
        }

        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    private static SatelliteDockBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof SatelliteDockBlockEntity dock) {
            return dock;
        }
        throw new IllegalStateException("Expected satellite dock block entity at " + pos);
    }
}
