package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.SoyuzCapsuleBlockEntity;
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

        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 62, 18, 3, 6);
        addSlot(HbmInventoryMenuHelper.validatedSlot(blockEntity.getItems(), SoyuzCapsuleBlockEntity.SLOT_ROCKET, 17, 36));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 104, 162);
    }

    public SoyuzCapsuleBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 128.0D);
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
        } else if (!HbmInventoryMenuHelper.legacyMergeItemStack(slots, stack, 0, MACHINE_SLOT_COUNT, false)) {
            // ContainerSoyuzCapsule permits an existing recovered stack to
            // merge before its all-slot insertion predicate rejects empty
            // slots. Keep the source's literal [0,19) destination range.
            return ItemStack.EMPTY;
        }

        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    private static SoyuzCapsuleBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof SoyuzCapsuleBlockEntity capsule) {
            return capsule;
        }
        throw new IllegalStateException("Expected Soyuz capsule block entity at " + pos);
    }
}
