package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DfcInjectorBlockEntity;
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

public class DfcInjectorMenu extends AbstractContainerMenu {
    private final DfcInjectorBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData fuel1;
    private final HbmFluidGuiHelper.TankData fuel2;
    private int beam;

    public DfcInjectorMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public DfcInjectorMenu(int id, Inventory inventory, DfcInjectorBlockEntity blockEntity) {
        super(ModMenuTypes.DFC_INJECTOR.get(), id);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 26, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 1, 26, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 2, 134, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), 3, 134, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 84, 166);
        fuel1 = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getFuel1());
        fuel2 = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getFuel2());
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBeam, value -> beam = value);
    }

    public DfcInjectorBlockEntity getBlockEntity() { return blockEntity; }
    public HbmFluidGuiHelper.TankData getFuel1() { return fuel1; }
    public HbmFluidGuiHelper.TankData getFuel2() { return fuel2; }
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
        if (index <= 3) {
            if (!moveItemStackTo(stack, 4, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static DfcInjectorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DfcInjectorBlockEntity injector) return injector;
        throw new IllegalStateException("Expected DFC injector at " + pos);
    }
}
