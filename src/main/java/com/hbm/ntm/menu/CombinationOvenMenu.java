package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.CombinationOvenBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
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

public class CombinationOvenMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = CombinationOvenBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final CombinationOvenBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData tank;
    private int progress;
    private int heat;
    private boolean wasOn;

    public CombinationOvenMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public CombinationOvenMenu(int containerId, Inventory playerInventory, CombinationOvenBlockEntity blockEntity) {
        super(ModMenuTypes.COMBINATION_OVEN.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                CombinationOvenBlockEntity.SLOT_INPUT, 26, 36));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                CombinationOvenBlockEntity.SLOT_OUTPUT, 89, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                CombinationOvenBlockEntity.SLOT_TANK_INPUT, 136, 18));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                CombinationOvenBlockEntity.SLOT_TANK_OUTPUT, 136, 54));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 104, 162);
        addDataSlots();
    }

    public CombinationOvenBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getProgressPixels(int width) {
        return Math.max(0, Math.min(width, progress * width / CombinationOvenBlockEntity.PROCESS_TIME));
    }

    public int getHeatPixels(int width) {
        return Math.max(0, Math.min(width, heat * width / CombinationOvenBlockEntity.MAX_HEAT));
    }

    public int getProgress() {
        return progress;
    }

    public int getHeat() {
        return heat;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                CombinationOvenBlockEntity.SLOT_INPUT, CombinationOvenBlockEntity.SLOT_INPUT + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeat, value -> heat = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::wasOn, value -> wasOn = value);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
    }

    private static CombinationOvenBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof CombinationOvenBlockEntity oven) {
            return oven;
        }
        throw new IllegalStateException("Expected combination oven block entity at " + pos);
    }
}
