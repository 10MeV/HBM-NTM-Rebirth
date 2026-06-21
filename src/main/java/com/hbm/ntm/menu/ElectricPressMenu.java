package com.hbm.ntm.menu;

import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.blockentity.ElectricPressBlockEntity;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ElectricPressMenu extends AbstractContainerMenu implements LegacyUpgradeInfoProvider {
    private static final int MACHINE_SLOT_COUNT = ElectricPressBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final ElectricPressBlockEntity blockEntity;
    private long power;
    private int press;

    public ElectricPressMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ElectricPressMenu(int containerId, Inventory playerInventory, ElectricPressBlockEntity blockEntity) {
        super(ModMenuTypes.ELECTRIC_PRESS.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                ElectricPressBlockEntity.SLOT_BATTERY, 44, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                ElectricPressBlockEntity.SLOT_STAMP, 80, 17));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                ElectricPressBlockEntity.SLOT_INPUT, 80, 53));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                ElectricPressBlockEntity.SLOT_OUTPUT, 140, 35));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(),
                ElectricPressBlockEntity.SLOT_UPGRADE, 44, 21));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public ElectricPressBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return blockEntity.getMaxPower();
    }

    public int getPowerBarHeight(int maxHeight) {
        long maxPower = getMaxPower();
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getPressHeight(int maxHeight) {
        return press * maxHeight / ElectricPressBlockEntity.MAX_PRESS;
    }

    public long getConsumption() {
        return ElectricPressBlockEntity.CONSUMPTION;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < MACHINE_SLOT_COUNT) {
                if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
                if (!moveItemStackTo(stack, ElectricPressBlockEntity.SLOT_BATTERY,
                        ElectricPressBlockEntity.SLOT_BATTERY + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof ItemPressStamp) {
                if (!moveItemStackTo(stack, ElectricPressBlockEntity.SLOT_STAMP,
                        ElectricPressBlockEntity.SLOT_STAMP + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (isSpeedUpgrade(stack)) {
                if (!moveItemStackTo(stack, ElectricPressBlockEntity.SLOT_UPGRADE,
                        ElectricPressBlockEntity.SLOT_UPGRADE + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, ElectricPressBlockEntity.SLOT_INPUT,
                    ElectricPressBlockEntity.SLOT_INPUT + 1, false)) {
                return ItemStack.EMPTY;
            }
            HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        }
        return result;
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
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getPress, value -> press = value);
    }

    private static boolean isSpeedUpgrade(ItemStack stack) {
        return stack.getItem() instanceof com.hbm.ntm.item.ItemMachineUpgrade upgrade
                && upgrade.getUpgradeType() == UpgradeType.SPEED;
    }

    private static ElectricPressBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ElectricPressBlockEntity press) {
            return press;
        }
        throw new IllegalStateException("Expected electric press block entity at " + pos);
    }
}
