package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.OilburnerBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.trait.FlammableFluidTrait;
import com.hbm.ntm.registry.ModMenuTypes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class OilburnerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = OilburnerBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final OilburnerBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData tank;
    private boolean on;
    private int setting;
    private int heatEnergy;
    private int lastBurned;
    private int lastHeatProduced;

    public OilburnerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public OilburnerMenu(int containerId, Inventory playerInventory, OilburnerBlockEntity blockEntity) {
        super(ModMenuTypes.OILBURNER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                OilburnerBlockEntity.SLOT_FLUID_INPUT, 26, 17));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(),
                OilburnerBlockEntity.SLOT_FLUID_OUTPUT, 26, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                OilburnerBlockEntity.SLOT_IDENTIFIER, 44, 71));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 121, 179);
        addDataSlots();
    }

    public OilburnerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    public boolean isOn() {
        return on;
    }

    public int getSetting() {
        return setting;
    }

    public int getHeatEnergy() {
        return heatEnergy;
    }

    public int getLastBurned() {
        return lastBurned;
    }

    public int getLastHeatProduced() {
        return lastHeatProduced;
    }

    public int getCurrentHeatOutputPerTick() {
        if (tank == null || tank.isEmpty()) {
            return 0;
        }
        FlammableFluidTrait flammable = tank.type().getTrait(FlammableFluidTrait.class);
        return flammable == null ? 0 : (int) (flammable.getHeatEnergyPerBucket() * setting / 1_000L);
    }

    public int heatBarHeight() {
        return heatEnergy * 52 / OilburnerBlockEntity.MAX_HEAT;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 256.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < MACHINE_SLOT_COUNT) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END);
        }
        ItemStack stack = slots.get(index).getItem();
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    OilburnerBlockEntity.SLOT_IDENTIFIER, OilburnerBlockEntity.SLOT_IDENTIFIER + 1);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                OilburnerBlockEntity.SLOT_FLUID_INPUT, OilburnerBlockEntity.SLOT_FLUID_INPUT + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOn, value -> on = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getSetting, value -> setting = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeatEnergy, value -> heatEnergy = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastBurned, value -> lastBurned = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getLastHeatProduced,
                value -> lastHeatProduced = value);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
    }

    private static OilburnerBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof OilburnerBlockEntity burner) {
            return burner;
        }
        throw new IllegalStateException("Expected oilburner block entity at " + pos);
    }
}
