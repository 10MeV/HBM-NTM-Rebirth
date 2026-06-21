package com.hbm.ntm.menu;

import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.blockentity.CrucibleBlockEntity;
import com.hbm.ntm.recipe.CrucibleRecipeRuntime;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CrucibleMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = CrucibleBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final CrucibleBlockEntity blockEntity;
    private int progress;
    private int heat;
    private int recipeAmount;
    private int wasteAmount;
    private int recipeColor;
    private int wasteColor;

    public CrucibleMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public CrucibleMenu(int containerId, Inventory playerInventory, CrucibleBlockEntity blockEntity) {
        super(ModMenuTypes.CRUCIBLE.get(), containerId);
        this.blockEntity = blockEntity;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                        col + row * 3, 107 + col * 18, 18 + row * 18));
            }
        }
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 132, 190);
        addDataSlots();
    }

    public int getProgressPixels() {
        return Math.max(0, Math.min(33, progress * 33 / Math.max(1, CrucibleBlockEntity.PROCESS_TIME)));
    }

    public int getHeatPixels() {
        return Math.max(0, Math.min(33, heat * 33 / Math.max(1, CrucibleBlockEntity.MAX_HEAT)));
    }

    public String getProgressText() {
        return progress + " / " + CrucibleBlockEntity.PROCESS_TIME + "TU";
    }

    public String getHeatText() {
        return heat + " / " + CrucibleBlockEntity.MAX_HEAT + "TU";
    }

    public int getRecipePixels() {
        return Math.max(0, Math.min(79, recipeAmount * 79 / Math.max(1, CrucibleBlockEntity.RECIPE_CAPACITY)));
    }

    public int getWastePixels() {
        return Math.max(0, Math.min(79, wasteAmount * 79 / Math.max(1, CrucibleBlockEntity.WASTE_CAPACITY)));
    }

    public int getRecipeColor() {
        return recipeColor;
    }

    public int getWasteColor() {
        return wasteColor;
    }

    public List<Component> getRecipeTooltip(boolean showMb) {
        return blockEntity.recipeTooltip(showMb);
    }

    public List<Component> getWasteTooltip(boolean showMb) {
        return blockEntity.wasteTooltip(showMb);
    }

    public List<MaterialStack> getRecipeStacks() {
        return blockEntity.getRecipeStacks();
    }

    public List<MaterialStack> getWasteStacks() {
        return blockEntity.getWasteStacks();
    }

    public int getStackPixels(MaterialStack stack, int capacity, int previous) {
        return Math.max(0, Math.min(79, (previous + stack.amount) * 79 / Math.max(1, capacity)));
    }

    public int recipeCapacity() {
        return CrucibleBlockEntity.RECIPE_CAPACITY;
    }

    public int wasteCapacity() {
        return CrucibleBlockEntity.WASTE_CAPACITY;
    }

    public CrucibleBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public CrucibleRecipeRuntime.Recipe getSelectedRecipe() {
        return blockEntity.getSelectedRecipeDefinition();
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 128.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        ItemStack stack = slots.get(index).getItem();
        if (!Mats.getSmeltingMaterialsFromItem(stack).isEmpty()) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    CrucibleBlockEntity.SLOT_INPUT_START, CrucibleBlockEntity.SLOT_INPUT_END);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeat, value -> heat = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRecipeAmount, value -> recipeAmount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getWasteAmount, value -> wasteAmount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRecipeColor, value -> recipeColor = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getWasteColor, value -> wasteColor = value);
    }

    private static CrucibleBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof CrucibleBlockEntity crucible) {
            return crucible;
        }
        throw new IllegalStateException("Expected crucible block entity at " + pos);
    }
}
