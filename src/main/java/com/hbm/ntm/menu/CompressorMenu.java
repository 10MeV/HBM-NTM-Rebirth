package com.hbm.ntm.menu;

import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.CompressorBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import java.util.Map;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CompressorMenu extends AbstractContainerMenu implements LegacyUpgradeInfoProvider {
    private static final int MACHINE_SLOT_COUNT = CompressorBlockEntity.ITEM_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final CompressorBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int progress;
    private int processTime = 100;
    private int powerRequirement;
    private int inputPressure;
    private HbmFluidGuiHelper.TankData inputTank;
    private HbmFluidGuiHelper.TankData outputTank;

    public CompressorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public CompressorMenu(int containerId, Inventory playerInventory, CompressorBlockEntity blockEntity) {
        super(ModMenuTypes.COMPRESSOR.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CompressorBlockEntity.SLOT_IDENTIFIER, 17, 72));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CompressorBlockEntity.SLOT_BATTERY, 152, 72));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CompressorBlockEntity.SLOT_UPGRADE_SPEED, 52, 72));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(blockEntity.getItems(),
                CompressorBlockEntity.SLOT_UPGRADE_POWER, 70, 72));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
        addDataSlots();
    }

    public CompressorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerRequirement() {
        return powerRequirement;
    }

    public int getInputPressure() {
        return inputPressure;
    }

    public int getProgressWidth(int maxWidth) {
        return processTime <= 0 ? 0 : progress * maxWidth / processTime;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public HbmFluidGuiHelper.TankData getInputTankData() {
        return inputTank;
    }

    public HbmFluidGuiHelper.TankData getOutputTankData() {
        return outputTank;
    }

    public List<Component> getInputTankTooltip() {
        return inputTank.tooltip();
    }

    public List<Component> getOutputTankTooltip() {
        return outputTank.tooltip();
    }

    public List<Component> getInputTankTooltip(boolean showHidden) {
        return inputTank.tooltip(showHidden);
    }

    public List<Component> getOutputTankTooltip(boolean showHidden) {
        return outputTank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slots.get(index).getItem();
        if (index >= MACHINE_SLOT_COUNT && !stack.isEmpty()) {
            if (HbmInventoryMenuHelper.isLegacyBatteryItem(stack)) {
                return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                        MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, HOTBAR_END,
                        CompressorBlockEntity.SLOT_BATTERY, CompressorBlockEntity.SLOT_BATTERY + 1);
            }
            if (stack.getItem() instanceof IFluidIdentifierItem) {
                return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                        MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, HOTBAR_END,
                        CompressorBlockEntity.SLOT_IDENTIFIER, CompressorBlockEntity.SLOT_IDENTIFIER + 1);
            }
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, HOTBAR_END,
                CompressorBlockEntity.SLOT_UPGRADE_SPEED, CompressorBlockEntity.SLOT_UPGRADE_POWER + 1);
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return blockEntity.getValidUpgrades();
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<Component> info, boolean extendedInfo) {
        blockEntity.provideInfo(type, level, info, extendedInfo);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getPower(), () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, () -> blockEntity.getMaxPower(), () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProcessTime,
                value -> processTime = Math.max(1, value));
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getPowerRequirement,
                value -> powerRequirement = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getInputTank().getPressure(),
                value -> inputPressure = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        outputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank());
    }

    private static CompressorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof CompressorBlockEntity compressor) {
            return compressor;
        }
        throw new IllegalStateException("Expected compressor block entity at " + pos);
    }
}
