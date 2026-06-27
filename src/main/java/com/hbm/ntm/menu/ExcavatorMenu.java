package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ExcavatorBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.DrillbitItem;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;

public class ExcavatorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ExcavatorBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final ExcavatorBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData tank;
    private long power;
    private long maxPower;
    private long consumption;
    private int targetDepth;
    private int flags;

    public ExcavatorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ExcavatorMenu(int containerId, Inventory playerInventory, ExcavatorBlockEntity blockEntity) {
        super(ModMenuTypes.EXCAVATOR.get(), containerId);
        this.blockEntity = blockEntity;
        ItemStackHandler items = blockEntity.getItems();

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, ExcavatorBlockEntity.SLOT_BATTERY, 220, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, ExcavatorBlockEntity.SLOT_FLUID_ID, 202, 72));
        for (int slot = ExcavatorBlockEntity.SLOT_UPGRADE_START; slot <= ExcavatorBlockEntity.SLOT_UPGRADE_END; slot++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, slot, 136 + (slot - ExcavatorBlockEntity.SLOT_UPGRADE_START) * 18, 75));
        }
        HbmInventoryMenuHelper.addTakeOnlySlots(this::addSlot, items, ExcavatorBlockEntity.SLOT_OUTPUT_START,
                136, 5, 3, 3);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 41, 122, 180);

        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPowerConsumption,
                () -> consumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getTargetDepth, value -> targetDepth = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, this::readFlags, value -> flags = value);
        this.tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
    }

    public ExcavatorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getTank() {
        return tank;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public long getConsumption() {
        return consumption;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getTargetDepth() {
        return targetDepth;
    }

    public boolean drillEnabled() {
        return (flags & 1) != 0;
    }

    public boolean crusherEnabled() {
        return (flags & 2) != 0;
    }

    public boolean wallingEnabled() {
        return (flags & 4) != 0;
    }

    public boolean veinMinerEnabled() {
        return (flags & 8) != 0;
    }

    public boolean silkTouchEnabled() {
        return (flags & 16) != 0;
    }

    public boolean operational() {
        return (flags & 32) != 0;
    }

    public boolean canVeinMine() {
        return (flags & 64) != 0;
    }

    public boolean canSilkTouch() {
        return (flags & 128) != 0;
    }

    public boolean hasDrillbit() {
        return (flags & 256) != 0;
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
        var slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        boolean moved;
        if (index < MACHINE_SLOT_COUNT) {
            moved = moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true);
        } else if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
            moved = moveItemStackTo(stack, ExcavatorBlockEntity.SLOT_BATTERY,
                    ExcavatorBlockEntity.SLOT_BATTERY + 1, false);
        } else if (stack.getItem() instanceof com.hbm.ntm.api.fluid.IFluidIdentifierItem) {
            moved = moveItemStackTo(stack, ExcavatorBlockEntity.SLOT_FLUID_ID,
                    ExcavatorBlockEntity.SLOT_FLUID_ID + 1, false);
        } else if (stack.getItem() instanceof DrillbitItem) {
            moved = moveItemStackTo(stack, ExcavatorBlockEntity.SLOT_DRILLBIT,
                    ExcavatorBlockEntity.SLOT_DRILLBIT + 1, false);
        } else if (stack.getItem() instanceof ItemMachineUpgrade) {
            moved = moveItemStackTo(stack, ExcavatorBlockEntity.SLOT_UPGRADE_START,
                    ExcavatorBlockEntity.SLOT_UPGRADE_END + 1, false);
        } else {
            return ItemStack.EMPTY;
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    private int readFlags() {
        int result = 0;
        if (blockEntity.isDrillEnabled()) result |= 1;
        if (blockEntity.isCrusherEnabled()) result |= 2;
        if (blockEntity.isWallingEnabled()) result |= 4;
        if (blockEntity.isVeinMinerEnabled()) result |= 8;
        if (blockEntity.isSilkTouchEnabled()) result |= 16;
        if (blockEntity.isOperational()) result |= 32;
        if (blockEntity.canVeinMine()) result |= 64;
        if (blockEntity.canSilkTouch()) result |= 128;
        if (blockEntity.getInstalledDrillType() != null) result |= 256;
        return result;
    }

    private static ExcavatorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ExcavatorBlockEntity excavator) {
            return excavator;
        }
        throw new IllegalStateException("Expected excavator block entity at " + pos);
    }
}
