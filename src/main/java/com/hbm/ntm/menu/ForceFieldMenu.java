package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ForceFieldBlockEntity;
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

public class ForceFieldMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ForceFieldBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final ForceFieldBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int health;
    private int maxHealth;
    private int cooldown;
    private int radius;
    private boolean on;

    public ForceFieldMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ForceFieldMenu(int containerId, Inventory playerInventory, ForceFieldBlockEntity blockEntity) {
        super(ModMenuTypes.FORCE_FIELD.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ForceFieldBlockEntity.SLOT_BATTERY,
                26, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ForceFieldBlockEntity.SLOT_RADIUS,
                89, 35));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ForceFieldBlockEntity.SLOT_HEALTH,
                107, 35));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public ForceFieldBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getRadius() {
        return radius;
    }

    public boolean isOn() {
        return on;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getHealthBarHeight(int maxHeight) {
        return maxHealth <= 0 ? 0 : Math.max(0, Math.min(maxHeight, health * maxHeight / maxHealth));
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        ItemStack stack = slots.get(index).getItem();
        ItemStack original = stack.copy();
        if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                ForceFieldBlockEntity.SLOT_RADIUS, ForceFieldBlockEntity.SLOT_HEALTH + 1,
                ForceFieldBlockEntity.SLOT_BATTERY, ForceFieldBlockEntity.SLOT_BATTERY + 1)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slots.get(index), stack);
        return original;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower,
                value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHealth, value -> health = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxHealth, value -> maxHealth = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getCooldown, value -> cooldown = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> (int) blockEntity.getRadius(), value -> radius = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOn, value -> on = value);
    }

    private static ForceFieldBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof ForceFieldBlockEntity forceField) {
            return forceField;
        }
        throw new IllegalStateException("Expected forcefield block entity at " + pos);
    }
}
