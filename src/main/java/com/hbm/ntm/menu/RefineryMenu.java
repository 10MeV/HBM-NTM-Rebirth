package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RefineryBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.ArrayList;
import java.util.List;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RefineryMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = RefineryBlockEntity.ITEM_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final RefineryBlockEntity blockEntity;
    private final List<HbmFluidGuiHelper.TankData> tanks = new ArrayList<>();
    private long power;
    private long maxPower;

    public RefineryMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RefineryMenu(int containerId, Inventory playerInventory, RefineryBlockEntity blockEntity) {
        super(ModMenuTypes.REFINERY.get(), containerId);
        this.blockEntity = blockEntity;
        ItemStackHandler items = blockEntity.getItems();
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(items, RefineryBlockEntity.SLOT_BATTERY, 158, 108));
        addSlot(new SlotItemHandler(items, RefineryBlockEntity.SLOT_INPUT_CONTAINER, 12, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, RefineryBlockEntity.SLOT_INPUT_CONTAINER_OUTPUT, 12, 108));
        addSlot(new SlotItemHandler(items, RefineryBlockEntity.SLOT_HEAVY_CONTAINER, 64, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, RefineryBlockEntity.SLOT_HEAVY_CONTAINER_OUTPUT, 64, 108));
        addSlot(new SlotItemHandler(items, RefineryBlockEntity.SLOT_NAPHTHA_CONTAINER, 82, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, RefineryBlockEntity.SLOT_NAPHTHA_CONTAINER_OUTPUT, 82, 108));
        addSlot(new SlotItemHandler(items, RefineryBlockEntity.SLOT_LIGHT_CONTAINER, 100, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, RefineryBlockEntity.SLOT_LIGHT_CONTAINER_OUTPUT, 100, 108));
        addSlot(new SlotItemHandler(items, RefineryBlockEntity.SLOT_PETROLEUM_CONTAINER, 118, 90));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, RefineryBlockEntity.SLOT_PETROLEUM_CONTAINER_OUTPUT, 118, 108));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, RefineryBlockEntity.SLOT_SOLID_OUTPUT, 38, 90));
        addSlot(HbmInventoryMenuHelper.plainMachineSlot(items, RefineryBlockEntity.SLOT_IDENTIFIER, 38, 108));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 11, 158, 216);
        addDataSlots();
    }

    public RefineryBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public List<HbmFluidGuiHelper.TankData> getTanks() {
        return tanks;
    }

    public HbmFluidGuiHelper.TankData getTank(int index) {
        return index >= 0 && index < tanks.size() ? tanks.get(index) : null;
    }

    public List<Component> getTankTooltip(int index, boolean showHidden) {
        HbmFluidGuiHelper.TankData tank = getTank(index);
        return tank == null ? List.of() : tank.tooltip(showHidden);
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

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity,
                HbmInventoryMenuHelper.LEGACY_MACHINE_USE_DISTANCE_SQR);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                RefineryBlockEntity.SLOT_BATTERY, RefineryBlockEntity.SLOT_BATTERY + 1,
                RefineryBlockEntity.SLOT_INPUT_CONTAINER, RefineryBlockEntity.SLOT_INPUT_CONTAINER + 1,
                RefineryBlockEntity.SLOT_HEAVY_CONTAINER, RefineryBlockEntity.SLOT_HEAVY_CONTAINER + 1,
                RefineryBlockEntity.SLOT_NAPHTHA_CONTAINER, RefineryBlockEntity.SLOT_NAPHTHA_CONTAINER + 1,
                RefineryBlockEntity.SLOT_LIGHT_CONTAINER, RefineryBlockEntity.SLOT_LIGHT_CONTAINER + 1,
                RefineryBlockEntity.SLOT_PETROLEUM_CONTAINER, RefineryBlockEntity.SLOT_PETROLEUM_CONTAINER + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        tanks.addAll(HbmFluidGuiHelper.watchTanks(this::addDataSlot, blockEntity.getAllTanks()));
    }

    private static RefineryBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RefineryBlockEntity refinery) {
            return refinery;
        }
        throw new IllegalStateException("Expected refinery block entity at " + pos);
    }
}
