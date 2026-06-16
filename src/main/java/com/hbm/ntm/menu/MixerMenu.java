package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.MixerBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
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

public class MixerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = MixerBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final MixerBlockEntity blockEntity;
    private long power;
    private long maxPower;
    private int consumption;
    private int progress;
    private int processTime;
    private int recipeIndex;
    private int wasOn;
    private final List<HbmFluidGuiHelper.TankData> tanks;

    public MixerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public MixerMenu(int containerId, Inventory playerInventory, MixerBlockEntity blockEntity) {
        super(ModMenuTypes.MIXER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), MixerBlockEntity.SLOT_BATTERY, 23, 77));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), MixerBlockEntity.SLOT_SOLID_INPUT, 43, 77));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), MixerBlockEntity.SLOT_FLUID_ID, 117, 77));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), MixerBlockEntity.SLOT_UPGRADE_1, 137, 24));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), MixerBlockEntity.SLOT_UPGRADE_2, 137, 42));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 122, 180);
        tanks = HbmFluidGuiHelper.watchTanks(this::addDataSlot, blockEntity.getAllTanks());
        addDataSlots();
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getConsumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgress, value -> progress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProcessTime, value -> processTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRecipeIndex, value -> recipeIndex = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.wasOn() ? 1 : 0, value -> wasOn = value);
    }

    public MixerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getConsumption() {
        return consumption;
    }

    public int getRecipeIndex() {
        return recipeIndex;
    }

    public boolean wasOn() {
        return wasOn != 0;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public int getProgressWidth(int maxWidth) {
        return processTime <= 0 ? 0 : progress * maxWidth / processTime;
    }

    public HbmFluidGuiHelper.TankData tank(int index) {
        return index >= 0 && index < tanks.size() ? tanks.get(index) : null;
    }

    public List<Component> tankTooltip(int index, boolean showHidden) {
        HbmFluidGuiHelper.TankData tank = tank(index);
        return tank == null ? List.of() : tank.tooltip(showHidden);
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
                    MixerBlockEntity.SLOT_BATTERY, MixerBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    MixerBlockEntity.SLOT_FLUID_ID, MixerBlockEntity.SLOT_FLUID_ID + 1);
        }
        if (stack.getItem() instanceof ItemMachineUpgrade) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                    MixerBlockEntity.SLOT_UPGRADE_1, MixerBlockEntity.SLOT_UPGRADE_2 + 1);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack,
                MixerBlockEntity.SLOT_SOLID_INPUT, MixerBlockEntity.SLOT_SOLID_INPUT + 1);
    }

    private static MixerBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof MixerBlockEntity mixer) {
            return mixer;
        }
        throw new IllegalStateException("Expected mixer block entity at " + pos);
    }
}
