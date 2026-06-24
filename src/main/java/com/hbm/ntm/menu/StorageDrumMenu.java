package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.StorageDrumBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class StorageDrumMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = StorageDrumBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final StorageDrumBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData liquidTank;
    private HbmFluidGuiHelper.TankData gasTank;

    public StorageDrumMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public StorageDrumMenu(int containerId, Inventory playerInventory, StorageDrumBlockEntity blockEntity) {
        super(ModMenuTypes.STORAGE_DRUM.get(), containerId);
        this.blockEntity = blockEntity;
        int index = 0;
        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 6; i++) {
                if (i + j > 1 && i + j < 9 && 5 - i + j > 1 && i + 5 - j > 1) {
                    addSlot(new SlotItemHandler(blockEntity.getItems(), index++, 35 + i * 18, 24 + j * 18));
                }
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 152, 210);
        addDataSlots();
    }

    public StorageDrumBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getLiquidTank() {
        return liquidTank;
    }

    public HbmFluidGuiHelper.TankData getGasTank() {
        return gasTank;
    }

    public List<Component> getTankTooltip(HbmFluidGuiHelper.TankData tank, boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
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
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, MACHINE_SLOT_COUNT, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addDataSlots() {
        liquidTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getLiquidTank());
        gasTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getGasTank());
    }

    private static StorageDrumBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof StorageDrumBlockEntity drum) {
            return drum;
        }
        throw new IllegalStateException("Expected storage drum block entity at " + pos);
    }
}
