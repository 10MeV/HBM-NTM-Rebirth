package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ReactorControlBlockEntity;
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
import net.minecraftforge.items.SlotItemHandler;

public class ReactorControlMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 1;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final ReactorControlBlockEntity blockEntity;
    private int linked;
    private int flux;
    private int levelPercent;
    private int temperature;
    private int levelLower;
    private int levelUpper;
    private int heatLower;
    private int heatUpper;
    private int function;

    public ReactorControlMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ReactorControlMenu(int containerId, Inventory playerInventory, ReactorControlBlockEntity blockEntity) {
        super(ModMenuTypes.REACTOR_CONTROL.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(new SlotItemHandler(blockEntity.getItems(), 0, 92, 38) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.REACTOR_SENSOR.get());
            }
        });
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public ReactorControlBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isLinked() {
        return linked != 0;
    }

    public int getFlux() {
        return flux;
    }

    public int getLevelPercent() {
        return levelPercent;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getLevelLower() {
        return levelLower;
    }

    public int getLevelUpper() {
        return levelUpper;
    }

    public int getHeatLower() {
        return heatLower;
    }

    public int getHeatUpper() {
        return heatUpper;
    }

    public int getFunction() {
        return function;
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
        } else if (stack.is(ModItems.REACTOR_SENSOR.get())) {
            if (!moveItemStackTo(stack, 0, MACHINE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isLinked() ? 1 : 0, value -> linked = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFlux, value -> flux = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getDisplayData()[0], value -> levelPercent = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getTemperatureDisplay, value -> temperature = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLevelLowerInt, value -> levelLower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLevelUpperInt, value -> levelUpper = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeatLowerDiv50, value -> heatLower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeatUpperDiv50, value -> heatUpper = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getFunctionOrdinal, value -> function = value);
    }

    private static ReactorControlBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ReactorControlBlockEntity control) {
            return control;
        }
        throw new IllegalStateException("Expected reactor control block entity at " + pos);
    }
}
