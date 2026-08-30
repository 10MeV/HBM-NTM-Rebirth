package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.PneumaticStorageClutterBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
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

public class PneumaticStorageClutterMenu extends AbstractContainerMenu {
    private static final int STORAGE_SLOT_COUNT = PneumaticStorageClutterBlockEntity.SLOT_COUNT;
    private final PneumaticStorageClutterBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData tankData;

    public PneumaticStorageClutterMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public PneumaticStorageClutterMenu(int containerId, Inventory inventory, PneumaticStorageClutterBlockEntity blockEntity) {
        super(ModMenuTypes.PNEUMATIC_STORAGE_CLUTTER.get(), containerId);
        this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 8, 17, 6, 9, 18);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 153, 211);
        tankData = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.compair());
    }

    public PneumaticStorageClutterBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tankData;
    }

    public java.util.List<net.minecraft.network.chat.Component> getTankTooltip(boolean showHidden) {
        return tankData.tooltip(showHidden);
    }

    public int getRangeFromPressure() {
        return com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticUtil.rangeForPressure(tankData.pressure());
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5D, blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= HbmInventoryMenuHelper.legacyMenuUseDistanceSqr(blockEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < STORAGE_SLOT_COUNT) {
                if (!moveItemStackTo(stack, STORAGE_SLOT_COUNT, STORAGE_SLOT_COUNT + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, STORAGE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
    }

    private static PneumaticStorageClutterBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof PneumaticStorageClutterBlockEntity storage) {
            return storage;
        }
        throw new IllegalStateException("Expected pneumatic storage clutter at " + pos);
    }
}
