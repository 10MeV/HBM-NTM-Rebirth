package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.CustomMissileLauncherBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.missile.CustomMissilePartProfile.PartSize;
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
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class CustomMissileLauncherMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = CustomMissileLauncherBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final CustomMissileLauncherBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int solidFuel;
    private int maxSolidFuel;
    private int solidState;
    private int liquidState;
    private int oxidizerState;
    private int padSizeOrdinal;
    private HbmFluidGuiHelper.TankData fuelTank;
    private HbmFluidGuiHelper.TankData oxidizerTank;

    protected CustomMissileLauncherMenu(MenuType<?> type, int containerId, Inventory playerInventory,
            CustomMissileLauncherBlockEntity blockEntity) {
        super(type, containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CustomMissileLauncherBlockEntity.SLOT_MISSILE, 26, 36));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CustomMissileLauncherBlockEntity.SLOT_DESIGNATOR, 26, 72));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CustomMissileLauncherBlockEntity.SLOT_FUEL_INPUT, 116, 72));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CustomMissileLauncherBlockEntity.SLOT_OXIDIZER_INPUT, 134, 72));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CustomMissileLauncherBlockEntity.SLOT_SOLID_FUEL, 152, 90));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CustomMissileLauncherBlockEntity.SLOT_BATTERY, 116, 108));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CustomMissileLauncherBlockEntity.SLOT_FUEL_OUTPUT, 116, 90));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CustomMissileLauncherBlockEntity.SLOT_OXIDIZER_OUTPUT, 134, 90));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
        addDataSlots();
    }

    public CustomMissileLauncherBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarWidth(int width) {
        return maxPower <= 0L ? 0 : (int) (power * width / maxPower);
    }

    public int getSolidFuel() {
        return solidFuel;
    }

    public int getMaxSolidFuel() {
        return maxSolidFuel;
    }

    public int getSolidBarHeight(int height) {
        return maxSolidFuel <= 0 ? 0 : solidFuel * height / maxSolidFuel;
    }

    public int getSolidState() {
        return solidState;
    }

    public int getLiquidState() {
        return liquidState;
    }

    public int getOxidizerState() {
        return oxidizerState;
    }

    public PartSize getPadSize() {
        PartSize[] values = PartSize.values();
        return padSizeOrdinal >= 0 && padSizeOrdinal < values.length ? values[padSizeOrdinal] : PartSize.SIZE_10;
    }

    public HbmFluidGuiHelper.TankData getFuelTankData() {
        return fuelTank;
    }

    public HbmFluidGuiHelper.TankData getOxidizerTankData() {
        return oxidizerTank;
    }

    public List<Component> getFuelTankTooltip(boolean showHidden) {
        return fuelTank.tooltip(showHidden);
    }

    public List<Component> getOxidizerTankTooltip(boolean showHidden) {
        return oxidizerTank.tooltip(showHidden);
    }

    public ItemStack getMissileStack() {
        return getSlot(CustomMissileLauncherBlockEntity.SLOT_MISSILE).getItem();
    }

    public boolean hasDesignator() {
        return blockEntity.hasDesignator();
    }

    public boolean isMissileValid() {
        return blockEntity.isMissileValid();
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                0, MACHINE_SLOT_COUNT);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getStoredPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxStoredPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSolidFuel, value -> solidFuel = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxSolidFuel, value -> maxSolidFuel = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::solidState, value -> solidState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::liquidState, value -> liquidState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::oxidizerState, value -> oxidizerState = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getPadSize().ordinal(), value -> padSizeOrdinal = value);
        fuelTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.fuelTank());
        oxidizerTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.oxidizerTank());
    }

    protected static CustomMissileLauncherBlockEntity getLauncher(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof CustomMissileLauncherBlockEntity launcher) {
            return launcher;
        }
        throw new IllegalStateException("Expected custom missile launcher block entity at " + pos);
    }
}
