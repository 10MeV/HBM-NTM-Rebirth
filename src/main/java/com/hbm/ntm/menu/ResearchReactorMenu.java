package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ResearchReactorBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.recipe.ResearchReactorFuelRuntime;
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

public class ResearchReactorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 12;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;
    private static final int[][] ROD_SLOTS = {
            {95, 22}, {131, 22}, {77, 40}, {113, 40}, {149, 40}, {95, 58},
            {131, 58}, {77, 76}, {113, 76}, {149, 76}, {95, 94}, {131, 94}
    };

    private final ResearchReactorBlockEntity blockEntity;
    private int heat;
    private int water;
    private int totalFlux;
    private int levelScaled;
    private int targetLevelScaled;

    public ResearchReactorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ResearchReactorMenu(int containerId, Inventory playerInventory, ResearchReactorBlockEntity blockEntity) {
        super(ModMenuTypes.RESEARCH_REACTOR.get(), containerId);
        this.blockEntity = blockEntity;
        for (int slot = 0; slot < ROD_SLOTS.length; slot++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), slot,
                    ROD_SLOTS[slot][0], ROD_SLOTS[slot][1]));
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
        addDataSlots();
    }

    public ResearchReactorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getHeat() {
        return heat;
    }

    public int getWater() {
        return water;
    }

    public int getTotalFlux() {
        return totalFlux;
    }

    public int getTemperatureDisplay() {
        return (int) Math.round(heat * 0.00002D * 980.0D + 20.0D);
    }

    public double getLevel() {
        return levelScaled / 10_000.0D;
    }

    public int getLevelPercent() {
        return (int) (levelScaled / 100.0D);
    }

    public int getTargetLevelPercent() {
        return (int) (targetLevelScaled / 100.0D);
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
        } else if (ResearchReactorFuelRuntime.isFuel(stack)) {
            if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0, MACHINE_SLOT_COUNT)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeat, value -> heat = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getWater, value -> water = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getTotalFlux, value -> totalFlux = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLevelScaled, value -> levelScaled = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getTargetLevelScaled,
                value -> targetLevelScaled = value);
    }

    private static ResearchReactorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ResearchReactorBlockEntity reactor) {
            return reactor;
        }
        throw new IllegalStateException("Expected research reactor block entity at " + pos);
    }
}
