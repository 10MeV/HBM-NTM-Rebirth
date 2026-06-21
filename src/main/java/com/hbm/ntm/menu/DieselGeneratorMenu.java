package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.DieselGeneratorBlockEntity;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class DieselGeneratorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = DieselGeneratorBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final DieselGeneratorBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData tank;
    private long power;
    private long maxPower;
    private boolean wasOn;
    private long output;

    public DieselGeneratorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public DieselGeneratorMenu(int containerId, Inventory playerInventory, DieselGeneratorBlockEntity blockEntity) {
        super(ModMenuTypes.DIESEL_GENERATOR.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                DieselGeneratorBlockEntity.SLOT_FLUID_INPUT, 44, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                DieselGeneratorBlockEntity.SLOT_FLUID_OUTPUT, 44, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                DieselGeneratorBlockEntity.SLOT_BATTERY, 116, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                DieselGeneratorBlockEntity.SLOT_IDENTIFIER, 8, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                DieselGeneratorBlockEntity.SLOT_IDENTIFIER_OUTPUT, 8, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public DieselGeneratorBlockEntity getBlockEntity() {
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

    public boolean wasOn() {
        return wasOn;
    }

    public long getOutput() {
        return output;
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    public boolean hasAcceptableFuel() {
        return blockEntity.hasAcceptableFuel();
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 256.0D);
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
        if (stack.getItem() instanceof HbmBatteryItem || stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent()) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    DieselGeneratorBlockEntity.SLOT_BATTERY,
                    DieselGeneratorBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    DieselGeneratorBlockEntity.SLOT_IDENTIFIER,
                    DieselGeneratorBlockEntity.SLOT_IDENTIFIER + 1);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                DieselGeneratorBlockEntity.SLOT_FLUID_INPUT,
                DieselGeneratorBlockEntity.SLOT_FLUID_INPUT + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::wasOn, value -> wasOn = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getLastPowerProduced,
                () -> output, value -> output = value);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
    }

    private static DieselGeneratorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DieselGeneratorBlockEntity diesel) {
            return diesel;
        }
        throw new IllegalStateException("Expected diesel generator block entity at " + pos);
    }
}
