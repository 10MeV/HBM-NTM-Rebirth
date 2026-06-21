package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.SolderingStationBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SolderingStationMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = SolderingStationBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final SolderingStationBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private long consumption;
    private int progress;
    private int processTime;
    private int collisionPrevention;
    private int isOn;
    private final HbmFluidGuiHelper.TankData tank;

    public SolderingStationMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public SolderingStationMenu(int containerId, Inventory playerInventory, SolderingStationBlockEntity blockEntity) {
        super(ModMenuTypes.SOLDERING_STATION.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(playerInventory);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
        addDataSlots();
    }

    private void addMachineSlots(Inventory playerInventory) {
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), row * 3 + column,
                        17 + column * 18, 18 + row * 18));
            }
        }
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                SolderingStationBlockEntity.SLOT_OUTPUT, 107, 27));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                SolderingStationBlockEntity.SLOT_BATTERY, 152, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                SolderingStationBlockEntity.SLOT_IDENTIFIER, 17, 63));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(),
                SolderingStationBlockEntity.SLOT_UPGRADE_0, 89, 63));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(),
                SolderingStationBlockEntity.SLOT_UPGRADE_1, 107, 63));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getConsumption,
                () -> consumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProcessTime, value -> processTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isCollisionPrevention() ? 1 : 0,
                value -> collisionPrevention = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.isOn() ? 1 : 0, value -> isOn = value);
    }

    public SolderingStationBlockEntity getBlockEntity() {
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

    public boolean isCollisionPrevention() {
        return collisionPrevention != 0;
    }

    public boolean isOn() {
        return isOn != 0;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressWidth(int maxWidth) {
        return processTime <= 0 ? 0 : progress * maxWidth / processTime;
    }

    public HbmFluidGuiHelper.TankData getTank() {
        return tank;
    }

    public List<Component> tankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
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
        if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    SolderingStationBlockEntity.SLOT_BATTERY,
                    SolderingStationBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    SolderingStationBlockEntity.SLOT_IDENTIFIER,
                    SolderingStationBlockEntity.SLOT_IDENTIFIER + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    SolderingStationBlockEntity.SLOT_UPGRADE_0,
                    SolderingStationBlockEntity.SLOT_UPGRADE_1 + 1);
        }
        if (blockEntity.getItems().isItemValid(SolderingStationBlockEntity.SLOT_TOPPING_0, stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    SolderingStationBlockEntity.SLOT_TOPPING_0,
                    SolderingStationBlockEntity.SLOT_TOPPING_2 + 1);
        }
        if (blockEntity.getItems().isItemValid(SolderingStationBlockEntity.SLOT_PCB_0, stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    SolderingStationBlockEntity.SLOT_PCB_0,
                    SolderingStationBlockEntity.SLOT_PCB_1 + 1);
        }
        if (blockEntity.getItems().isItemValid(SolderingStationBlockEntity.SLOT_SOLDER, stack)) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    SolderingStationBlockEntity.SLOT_SOLDER,
                    SolderingStationBlockEntity.SLOT_SOLDER + 1);
        }
        return false;
    }

    private static SolderingStationBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof SolderingStationBlockEntity station) {
            return station;
        }
        throw new IllegalStateException("Expected soldering station block entity at " + pos);
    }
}
