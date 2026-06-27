package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DfcStabilizerBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DfcStabilizerMenu extends AbstractContainerMenu {
    private final DfcStabilizerBlockEntity blockEntity;
    private long power;
    private int watts;
    private int beam;

    public DfcStabilizerMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public DfcStabilizerMenu(int id, Inventory inventory, DfcStabilizerBlockEntity blockEntity) {
        super(ModMenuTypes.DFC_STABILIZER.get(), id);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 80, 17));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 84, 166);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getWatts, value -> watts = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBeam, value -> beam = value);
    }

    public DfcStabilizerBlockEntity getBlockEntity() { return blockEntity; }
    public long getPower() { return power; }
    public int getWatts() { return watts; }
    public int getBeam() { return beam; }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 1, true)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static DfcStabilizerBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DfcStabilizerBlockEntity stabilizer) return stabilizer;
        throw new IllegalStateException("Expected DFC stabilizer at " + pos);
    }
}
