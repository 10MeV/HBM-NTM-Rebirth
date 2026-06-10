package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.FluidTankBlockEntity;
import com.hbm.ntm.blockentity.LegacyBigTankBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;

public class FluidTankMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 6;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final FluidTankBlockEntity blockEntity;
    private int mode;
    private int exploded;
    private int onFire;
    private HbmFluidGuiHelper.TankData tank;

    public FluidTankMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public FluidTankMenu(int containerId, Inventory playerInventory, FluidTankBlockEntity blockEntity) {
        super(ModMenuTypes.FLUID_TANK.get(), containerId);
        this.blockEntity = blockEntity;

        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_TYPE_INPUT, 8, 17));
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_TYPE_OUTPUT, 8, 53));
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_LOAD_INPUT, 35, 17));
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_LOAD_OUTPUT, 35, 53));
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_UNLOAD_INPUT, 125, 17));
        addSlot(new SlotItemHandler(blockEntity.getItems(), FluidTankBlockEntity.SLOT_UNLOAD_OUTPUT, 125, 53));
        addPlayerInventory(playerInventory);
        addDataSlots();
    }

    public FluidTankBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getMode() {
        return mode;
    }

    public boolean isExploded() {
        return exploded != 0;
    }

    public boolean isOnFire() {
        return onFire != 0;
    }

    public int getTankFillHeight(int maxHeight) {
        return tank.scaledFill(maxHeight);
    }

    public int getTankTint() {
        return tank.guiTint();
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public Component getTankInfo() {
        return tank.info();
    }

    public List<Component> getTankTooltip() {
        return tank.tooltip();
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (blockEntity instanceof LegacyBigTankBlockEntity
                || blockEntity instanceof com.hbm.ntm.blockentity.FluidBarrelBlockEntity) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START + 1, HOTBAR_END,
                    0, MACHINE_SLOT_COUNT);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, HOTBAR_END,
                0, MACHINE_SLOT_COUNT - 1);
    }

    private void addPlayerInventory(Inventory inventory) {
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 84, 142);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMode, value -> mode = value);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isExploded,
                value -> exploded = value ? 1 : 0);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOnFire,
                value -> onFire = value ? 1 : 0);
    }

    private static FluidTankBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof FluidTankBlockEntity tank) {
            return tank;
        }
        throw new IllegalStateException("Expected fluid tank block entity at " + pos);
    }
}
