package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.OilDrillBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class OilDrillMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = OilDrillBlockEntity.ITEM_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final OilDrillBlockEntity blockEntity;
    private final List<HbmFluidGuiHelper.TankData> tanks = new ArrayList<>();
    private long power;
    private long maxPower;
    private int indicator;

    public OilDrillMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public OilDrillMenu(int containerId, Inventory playerInventory, OilDrillBlockEntity blockEntity) {
        super(ModMenuTypes.OIL_DRILL.get(), containerId);
        this.blockEntity = blockEntity;
        ItemStackHandler items = blockEntity.getItems();
        addSlot(new SlotItemHandler(items, OilDrillBlockEntity.SLOT_BATTERY, 8, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return HbmInventoryMenuHelper.isBatteryLike(stack);
            }
        });
        addSlot(new SlotItemHandler(items, OilDrillBlockEntity.SLOT_OIL_CONTAINER, 80, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, OilDrillBlockEntity.SLOT_OIL_CONTAINER_OUTPUT, 80, 53));
        addSlot(new SlotItemHandler(items, OilDrillBlockEntity.SLOT_GAS_CONTAINER, 125, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(items, OilDrillBlockEntity.SLOT_GAS_CONTAINER_OUTPUT, 125, 53));
        addSlot(upgradeSlot(items, OilDrillBlockEntity.SLOT_UPGRADE_START, 152, 17));
        addSlot(upgradeSlot(items, OilDrillBlockEntity.SLOT_UPGRADE_START + 1, 152, 35));
        addSlot(upgradeSlot(items, OilDrillBlockEntity.SLOT_UPGRADE_END, 152, 53));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
        addDataSlots();
    }

    public OilDrillBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean hasFrackingTank() {
        return blockEntity.getFrackingTank() != null;
    }

    public int getIndicator() {
        return indicator;
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

    public HbmFluidGuiHelper.TankData getTank(int index) {
        return index >= 0 && index < tanks.size() ? tanks.get(index) : null;
    }

    public List<Component> getTankTooltip(int index, boolean showHidden) {
        HbmFluidGuiHelper.TankData tank = getTank(index);
        return tank == null ? List.of() : tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                OilDrillBlockEntity.SLOT_UPGRADE_START, OilDrillBlockEntity.SLOT_UPGRADE_END + 1,
                OilDrillBlockEntity.SLOT_BATTERY, OilDrillBlockEntity.SLOT_OIL_CONTAINER_OUTPUT,
                OilDrillBlockEntity.SLOT_GAS_CONTAINER, OilDrillBlockEntity.SLOT_GAS_CONTAINER_OUTPUT);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getIndicator();
            }

            @Override
            public void set(int value) {
                indicator = value;
            }
        });
        blockEntity.getAllTanks().forEach(tank -> tanks.add(HbmFluidGuiHelper.watchTank(this::addDataSlot, tank)));
    }

    private static SlotItemHandler upgradeSlot(ItemStackHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
        };
    }

    private static OilDrillBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof OilDrillBlockEntity oilDrill) {
            return oilDrill;
        }
        throw new IllegalStateException("Expected oil drill block entity at " + pos);
    }
}
