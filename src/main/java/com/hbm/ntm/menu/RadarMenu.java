package com.hbm.ntm.menu;

import com.hbm.ntm.api.entity.RadarInventoryProfile;
import com.hbm.ntm.api.entity.RadarMenuLayout;
import com.hbm.ntm.api.entity.RadarMenuState;
import com.hbm.ntm.api.entity.RadarRedstoneMode;
import com.hbm.ntm.blockentity.RadarBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class RadarMenu extends AbstractContainerMenu {
    private final RadarBlockEntity blockEntity;
    private RadarMenuState state = RadarMenuState.DEFAULT;

    public RadarMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RadarMenu(int containerId, Inventory playerInventory, RadarBlockEntity blockEntity) {
        super(ModMenuTypes.RADAR.get(), containerId);
        this.blockEntity = blockEntity;

        for (RadarMenuLayout.MachineSlot slot : RadarMenuLayout.machineSlots()) {
            addSlot(new SlotItemHandler(blockEntity.getItems(), slot.slot(), slot.x(), slot.y()));
        }
        addPlayerInventory(playerInventory);
        addRadarDataSlots();
    }

    public RadarBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public RadarMenuState getState() {
        return state;
    }

    public long getPower() {
        return state.power();
    }

    public long getMaxPower() {
        return state.maxPower();
    }

    public boolean scanMissiles() {
        return state.scanSettings().scanMissiles();
    }

    public boolean scanShells() {
        return state.scanSettings().scanShells();
    }

    public boolean scanPlayers() {
        return state.scanSettings().scanPlayers();
    }

    public boolean smartMode() {
        return state.scanSettings().smartMode();
    }

    public boolean redMode() {
        return state.redstoneProximityMode();
    }

    public boolean showMap() {
        return state.showMap();
    }

    public boolean jammed() {
        return state.jammed();
    }

    public int getRedPower() {
        return state.redstonePower();
    }

    public int getPowerBarWidth(int maxWidth) {
        return state.powerBarWidth(maxWidth);
    }

    public boolean hasPower() {
        return state.hasPower();
    }

    public boolean hasOperatingPower(long consumption) {
        return state.hasOperatingPower(consumption);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        if (index < 0 || index >= slots.size()) {
            return result;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();
        RadarInventoryProfile.QuickMovePlan plan = RadarInventoryProfile.quickMovePlan(index, result);
        if (!moveByPlan(stack, plan)) {
            return ItemStack.EMPTY;
        }

        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (RadarMenuLayout.PlayerSlot slot : RadarMenuLayout.playerInventorySlots()) {
            addSlot(new Slot(inventory, slot.slot(), slot.x(), slot.y()));
        }
    }

    private boolean moveByPlan(ItemStack stack, RadarInventoryProfile.QuickMovePlan plan) {
        if (plan == null || !plan.hasPrimary()) {
            return false;
        }
        if (moveItemStackTo(stack, plan.primaryStart(), plan.primaryEnd(), plan.reversePrimary())) {
            return true;
        }
        return plan.hasFallback()
                && moveItemStackTo(stack, plan.fallbackStart(), plan.fallbackEnd(), plan.reverseFallback());
    }

    private void addRadarDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> state.power(),
                value -> state = state.withPower(value));
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> state.maxPower(),
                value -> state = state.withMaxPower(value));
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isScanMissiles,
                value -> state = state.withScanMissiles(value));
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isScanShells,
                value -> state = state.withScanShells(value));
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isScanPlayers,
                value -> state = state.withScanPlayers(value));
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isSmartMode,
                value -> state = state.withSmartMode(value));
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isRedMode,
                value -> state = state.withRedstoneMode(RadarRedstoneMode.fromLegacyFlag(value)));
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isShowMap,
                value -> state = state.withShowMap(value));
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isJammed,
                value -> state = state.withJammed(value));
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastRedPower,
                value -> state = state.withRedstonePower(value));
    }

    private static RadarBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RadarBlockEntity radar) {
            return radar;
        }
        throw new IllegalStateException("Expected radar block entity at " + pos);
    }
}
