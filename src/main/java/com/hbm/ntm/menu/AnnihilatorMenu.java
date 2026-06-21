package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.AnnihilatorBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.math.BigInteger;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AnnihilatorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = AnnihilatorBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final AnnihilatorBlockEntity blockEntity;

    public AnnihilatorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public AnnihilatorMenu(int containerId, Inventory playerInventory, AnnihilatorBlockEntity blockEntity) {
        super(ModMenuTypes.ANNIHILATOR.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(playerInventory);
    }

    private void addMachineSlots(Inventory playerInventory) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                AnnihilatorBlockEntity.SLOT_TRASH, 17, 45));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                AnnihilatorBlockEntity.SLOT_FLUID_IDENTIFIER, 35, 45));
        HbmInventoryMenuHelper.addOutputSlots(this::addSlot, blockEntity.getItems(),
                AnnihilatorBlockEntity.SLOT_PAYOUT_START, 80, 36, 3, 2);
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                AnnihilatorBlockEntity.SLOT_MONITOR, 152, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                AnnihilatorBlockEntity.SLOT_REQUEST, 152, 62));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                AnnihilatorBlockEntity.SLOT_REQUEST_OUTPUT, 152, 80));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 126, 184);
    }

    public AnnihilatorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public String getPool() {
        return blockEntity.getPool();
    }

    public BigInteger getMonitorBigInt() {
        return blockEntity.getMonitorBigInt();
    }

    public ItemStack getMonitorStack() {
        return blockEntity.getItems().getStackInSlot(AnnihilatorBlockEntity.SLOT_MONITOR);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof IFluidIdentifierItem) {
            if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    AnnihilatorBlockEntity.SLOT_FLUID_IDENTIFIER,
                    AnnihilatorBlockEntity.SLOT_FLUID_IDENTIFIER + 1)) {
                return ItemStack.EMPTY;
            }
        } else if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                AnnihilatorBlockEntity.SLOT_TRASH,
                AnnihilatorBlockEntity.SLOT_TRASH + 1)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static AnnihilatorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof AnnihilatorBlockEntity annihilator) {
            return annihilator;
        }
        throw new IllegalStateException("Expected annihilator block entity at " + pos);
    }
}
