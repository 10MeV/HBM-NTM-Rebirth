package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.FireboxHeaterBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FireboxHeaterMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = FireboxHeaterBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final FireboxHeaterBlockEntity blockEntity;
    private int maxBurnTime;
    private int burnTime;
    private int burnHeat;
    private int heatEnergy;
    private int maxHeat;
    private boolean wasOn;
    private boolean oven;

    public FireboxHeaterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public FireboxHeaterMenu(int containerId, Inventory playerInventory, FireboxHeaterBlockEntity blockEntity) {
        super(ModMenuTypes.FIREBOX_HEATER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 44, 27));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 62, 27));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 86, 144);
        addDataSlots();
    }

    public FireboxHeaterBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getHeatEnergy() {
        return heatEnergy;
    }

    public int getMaxHeat() {
        return maxHeat;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getBurnHeat() {
        return burnHeat;
    }

    public boolean wasOn() {
        return wasOn;
    }

    public boolean isOven() {
        return oven;
    }

    public int heatBarWidth() {
        return maxHeat <= 0 ? 0 : heatEnergy * 69 / maxHeat;
    }

    public int burnBarWidth() {
        return burnTime * 70 / Math.max(maxBurnTime, 1);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        blockEntity.closeMenu(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, MACHINE_SLOT_COUNT,
                    PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, MACHINE_SLOT_COUNT,
                PLAYER_INVENTORY_START, PLAYER_SLOT_END, 0, MACHINE_SLOT_COUNT);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxBurnTime, value -> maxBurnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBurnTime, value -> burnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBurnHeat, value -> burnHeat = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeatEnergy, value -> heatEnergy = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::maxHeat, value -> maxHeat = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::wasOn, value -> wasOn = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot,
                () -> blockEntity.kind() == FireboxHeaterBlockEntity.Kind.OVEN,
                value -> oven = value);
    }

    private static FireboxHeaterBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof FireboxHeaterBlockEntity firebox) {
            return firebox;
        }
        throw new IllegalStateException("Expected firebox heater block entity at " + pos);
    }
}
