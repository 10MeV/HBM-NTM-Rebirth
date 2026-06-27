package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DfcCoreBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
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

public class DfcCoreMenu extends AbstractContainerMenu {
    private final DfcCoreBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData fuel1;
    private final HbmFluidGuiHelper.TankData fuel2;
    private int field;
    private int heat;
    private int color;

    public DfcCoreMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public DfcCoreMenu(int id, Inventory inventory, DfcCoreBlockEntity blockEntity) {
        super(ModMenuTypes.DFC_CORE.get(), id);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 62, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 80, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 2, 98, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 84, 166);
        fuel1 = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getFuel1());
        fuel2 = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getFuel2());
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getField, value -> field = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeat, value -> heat = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getColor, value -> color = value);
    }

    public DfcCoreBlockEntity getBlockEntity() { return blockEntity; }
    public HbmFluidGuiHelper.TankData getFuel1() { return fuel1; }
    public HbmFluidGuiHelper.TankData getFuel2() { return fuel2; }
    public int getField() { return field; }
    public int getHeat() { return heat; }
    public int getColor() { return color; }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveMachine(player, index, 3);
    }

    protected ItemStack quickMoveMachine(Player player, int index, int machineSlots) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, machineSlots, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static DfcCoreBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DfcCoreBlockEntity core) return core;
        throw new IllegalStateException("Expected DFC core at " + pos);
    }
}
