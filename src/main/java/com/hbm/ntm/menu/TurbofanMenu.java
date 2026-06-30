package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.TurbofanBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
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

public class TurbofanMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = TurbofanBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final TurbofanBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData fuelTank;
    private HbmFluidGuiHelper.TankData bloodTank;
    private long power;
    private long maxPower;
    private int afterburner;
    private boolean showBlood;
    private int output;
    private int consumption;

    public TurbofanMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public TurbofanMenu(int containerId, Inventory playerInventory, TurbofanBlockEntity blockEntity) {
        super(ModMenuTypes.TURBOFAN.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                TurbofanBlockEntity.SLOT_FLUID_INPUT, 17, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                TurbofanBlockEntity.SLOT_FLUID_OUTPUT, 17, 53));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(),
                TurbofanBlockEntity.SLOT_AFTERBURN, 98, 71));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                TurbofanBlockEntity.SLOT_BATTERY, 143, 71));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                TurbofanBlockEntity.SLOT_IDENTIFIER, 44, 71));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 121, 179);
        addDataSlots();
    }

    public TurbofanBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getAfterburner() {
        return afterburner;
    }

    public boolean showsBlood() {
        return showBlood;
    }

    public int getOutput() {
        return output;
    }

    public int getConsumption() {
        return consumption;
    }

    public HbmFluidGuiHelper.TankData getFuelTank() {
        return fuelTank;
    }

    public HbmFluidGuiHelper.TankData getBloodTank() {
        return bloodTank;
    }

    public List<Component> fuelTooltip(boolean showHidden) {
        return fuelTank.tooltip(showHidden);
    }

    public List<Component> bloodTooltip(boolean showHidden) {
        return bloodTank.tooltip(showHidden);
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
        ItemStack stack = slots.get(index).getItem();
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    TurbofanBlockEntity.SLOT_BATTERY, TurbofanBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    TurbofanBlockEntity.SLOT_IDENTIFIER, TurbofanBlockEntity.SLOT_IDENTIFIER + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade upgrade
                && upgrade.getUpgradeType() == ItemMachineUpgrade.UpgradeType.AFTERBURN) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    TurbofanBlockEntity.SLOT_AFTERBURN, TurbofanBlockEntity.SLOT_AFTERBURN + 1);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                TurbofanBlockEntity.SLOT_FLUID_INPUT, TurbofanBlockEntity.SLOT_FLUID_INPUT + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getAfterburner, value -> afterburner = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::showsBlood, value -> showBlood = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastOutput, value -> output = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastConsumption, value -> consumption = value);
        fuelTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getFuelTank());
        bloodTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getBloodTank());
    }

    private static TurbofanBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof TurbofanBlockEntity turbofan) {
            return turbofan;
        }
        throw new IllegalStateException("Expected turbofan block entity at " + pos);
    }
}
