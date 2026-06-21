package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.OreSlopperBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModItems;
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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class OreSlopperMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = OreSlopperBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final OreSlopperBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private long consumption;
    private int progress;
    private boolean processing;
    private final HbmFluidGuiHelper.TankData waterTank;
    private final HbmFluidGuiHelper.TankData slopTank;

    public OreSlopperMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public OreSlopperMenu(int containerId, Inventory inventory, OreSlopperBlockEntity blockEntity) {
        super(ModMenuTypes.ORE_SLOPPER.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(inventory);
        waterTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getWaterTank());
        slopTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getSlopTank());
        addDataSlots();
    }

    private void addMachineSlots(Inventory inventory) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), OreSlopperBlockEntity.SLOT_BATTERY,
                8, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), OreSlopperBlockEntity.SLOT_IDENTIFIER,
                26, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), OreSlopperBlockEntity.SLOT_INPUT,
                71, 27));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                OreSlopperBlockEntity.SLOT_OUTPUT_START, 134, 18));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                OreSlopperBlockEntity.SLOT_OUTPUT_START + 1, 152, 18));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                OreSlopperBlockEntity.SLOT_OUTPUT_START + 2, 134, 36));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                OreSlopperBlockEntity.SLOT_OUTPUT_START + 3, 152, 36));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                OreSlopperBlockEntity.SLOT_OUTPUT_START + 4, 134, 54));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                OreSlopperBlockEntity.SLOT_OUTPUT_START + 5, 152, 54));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), OreSlopperBlockEntity.SLOT_UPGRADE_1,
                62, 72));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), OreSlopperBlockEntity.SLOT_UPGRADE_2,
                80, 72));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 122, 180);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getConsumption,
                () -> consumption, value -> consumption = value);
        HbmMenuDataSlots.addProgress(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isProcessing, value -> processing = value);
    }

    public OreSlopperBlockEntity getBlockEntity() {
        return blockEntity;
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

    public boolean isProcessing() {
        return processing;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressBarHeight(int maxHeight) {
        return progress * maxHeight / 10_000;
    }

    public HbmFluidGuiHelper.TankData getWaterTank() {
        return waterTank;
    }

    public HbmFluidGuiHelper.TankData getSlopTank() {
        return slopTank;
    }

    public List<Component> waterTooltip(boolean showHidden) {
        return waterTank.tooltip(showHidden);
    }

    public List<Component> slopTooltip(boolean showHidden) {
        return slopTank.tooltip(showHidden);
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
        } else if (!movePlayerStackToMachine(stack)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.is(ModItems.BEDROCK_ORE_BASE.get())) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    OreSlopperBlockEntity.SLOT_INPUT, OreSlopperBlockEntity.SLOT_INPUT + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade upgrade
                && (upgrade.getUpgradeType() == UpgradeType.SPEED || upgrade.getUpgradeType() == UpgradeType.EFFECT)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    OreSlopperBlockEntity.SLOT_UPGRADE_1, OreSlopperBlockEntity.SLOT_UPGRADE_2 + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    OreSlopperBlockEntity.SLOT_IDENTIFIER, OreSlopperBlockEntity.SLOT_IDENTIFIER + 1);
        }
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    OreSlopperBlockEntity.SLOT_BATTERY, OreSlopperBlockEntity.SLOT_BATTERY + 1);
        }
        return false;
    }

    private static OreSlopperBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof OreSlopperBlockEntity slopper) {
            return slopper;
        }
        throw new IllegalStateException("Expected ore slopper block entity at " + pos);
    }
}
