package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.ICFPressBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModItems;
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

public class ICFPressMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ICFPressBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final ICFPressBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData deuteriumTank;
    private final HbmFluidGuiHelper.TankData tritiumTank;
    private int muon;

    public ICFPressMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ICFPressMenu(int containerId, Inventory playerInventory, ICFPressBlockEntity blockEntity) {
        super(ModMenuTypes.ICF_PRESS.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ICFPressBlockEntity.SLOT_EMPTY, 98, 18));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), ICFPressBlockEntity.SLOT_OUTPUT, 98, 54));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ICFPressBlockEntity.SLOT_MUON, 8, 18));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), ICFPressBlockEntity.SLOT_MUON_OUT, 8, 54));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ICFPressBlockEntity.SLOT_FUEL_1, 62, 54));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ICFPressBlockEntity.SLOT_FUEL_2, 134, 54));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ICFPressBlockEntity.SLOT_FLUID_ID_1, 62, 18));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(), ICFPressBlockEntity.SLOT_FLUID_ID_2, 134, 18));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 97, 155);
        deuteriumTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getDeuteriumTank());
        tritiumTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTritiumTank());
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMuon, value -> muon = value);
    }

    public ICFPressBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getDeuteriumTank() {
        return deuteriumTank;
    }

    public HbmFluidGuiHelper.TankData getTritiumTank() {
        return tritiumTank;
    }

    public int getMuon() {
        return muon;
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
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ModItems.ICF_PELLET_EMPTY.get())) {
            if (!moveItemStackTo(stack, ICFPressBlockEntity.SLOT_EMPTY, ICFPressBlockEntity.SLOT_EMPTY + 1, false)) return ItemStack.EMPTY;
        } else if (stack.is(ModItems.PARTICLE_MUON.get())) {
            if (!moveItemStackTo(stack, ICFPressBlockEntity.SLOT_MUON, ICFPressBlockEntity.SLOT_MUON + 1, false)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof IFluidIdentifierItem) {
            if (!moveItemStackTo(stack, ICFPressBlockEntity.SLOT_FLUID_ID_1, ICFPressBlockEntity.SLOT_FLUID_ID_2 + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, ICFPressBlockEntity.SLOT_FUEL_1, ICFPressBlockEntity.SLOT_FUEL_2 + 1, false)) return ItemStack.EMPTY;
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static ICFPressBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ICFPressBlockEntity press) {
            return press;
        }
        throw new IllegalStateException("Expected ICF press block entity at " + pos);
    }
}
