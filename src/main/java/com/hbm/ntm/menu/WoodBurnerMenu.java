package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.WoodBurnerBlockEntity;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.recipe.WoodBurnerRecipeRuntime;
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

public class WoodBurnerMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 6;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final WoodBurnerBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData tank;
    private long power;
    private long maxPower;
    private boolean on;
    private boolean liquidBurn;
    private int burnTime;
    private int maxBurnTime;
    private int powerGen;

    public WoodBurnerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public WoodBurnerMenu(int containerId, Inventory playerInventory, WoodBurnerBlockEntity blockEntity) {
        super(ModMenuTypes.WOOD_BURNER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), WoodBurnerBlockEntity.SLOT_FUEL,
                26, 18));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), WoodBurnerBlockEntity.SLOT_ASH,
                26, 54));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), WoodBurnerBlockEntity.SLOT_IDENTIFIER,
                98, 54));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), WoodBurnerBlockEntity.SLOT_FLUID_INPUT,
                98, 18));
        addSlot(HbmInventoryMenuHelper.outputSlot(blockEntity.getItems(), WoodBurnerBlockEntity.SLOT_FLUID_OUTPUT,
                98, 36));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), WoodBurnerBlockEntity.SLOT_BATTERY,
                143, 54));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 104, 186);
        addDataSlots();
    }

    public WoodBurnerBlockEntity getBlockEntity() {
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

    public boolean isOn() {
        return on;
    }

    public boolean isLiquidBurn() {
        return liquidBurn;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getBurnBarHeight(int maxHeight) {
        return maxBurnTime <= 0 || liquidBurn ? 0 : burnTime * maxHeight / maxBurnTime;
    }

    public int getPowerGen() {
        return powerGen;
    }

    public HbmFluidGuiHelper.TankData getTankData() {
        return tank;
    }

    public List<Component> getTankTooltip(boolean showHidden) {
        return tank.tooltip(showHidden);
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 64.0D);
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
                    WoodBurnerBlockEntity.SLOT_BATTERY, WoodBurnerBlockEntity.SLOT_BATTERY + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    WoodBurnerBlockEntity.SLOT_IDENTIFIER, WoodBurnerBlockEntity.SLOT_IDENTIFIER + 1);
        }
        if (WoodBurnerRecipeRuntime.burnModule().getBurnTime(stack) > 0) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    WoodBurnerBlockEntity.SLOT_FUEL, WoodBurnerBlockEntity.SLOT_FUEL + 1);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                WoodBurnerBlockEntity.SLOT_FLUID_INPUT, WoodBurnerBlockEntity.SLOT_FLUID_INPUT + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOn, value -> on = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isLiquidBurn, value -> liquidBurn = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBurnTime, value -> burnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxBurnTime, value -> maxBurnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getPowerGen, value -> powerGen = value);
        tank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getTank());
    }

    private static WoodBurnerBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof WoodBurnerBlockEntity burner) {
            return burner;
        }
        throw new IllegalStateException("Expected wood burner block entity at " + pos);
    }
}
