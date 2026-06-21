package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ZirnoxReactorBlockEntity;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.recipe.ZirnoxFuelRuntime;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ZirnoxReactorMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = ZirnoxReactorBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;
    private static final int MENU_CO2_INPUT = ZirnoxReactorBlockEntity.ROD_SLOT_COUNT;
    private static final int MENU_CO2_OUTPUT = MENU_CO2_INPUT + 1;
    private static final int MENU_WATER_INPUT = MENU_CO2_INPUT + 2;
    private static final int MENU_WATER_OUTPUT = MENU_CO2_INPUT + 3;
    private static final int[][] ROD_SLOTS = {
            {26, 16}, {62, 16}, {98, 16}, {8, 34}, {44, 34}, {80, 34}, {116, 34}, {26, 52},
            {62, 52}, {98, 52}, {8, 70}, {44, 70}, {80, 70}, {116, 70}, {26, 88}, {62, 88},
            {98, 88}, {8, 106}, {44, 106}, {80, 106}, {116, 106}, {26, 124}, {62, 124}, {98, 124}
    };

    private final ZirnoxReactorBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData steamTank;
    private final HbmFluidGuiHelper.TankData carbonDioxideTank;
    private final HbmFluidGuiHelper.TankData waterTank;
    private int heat;
    private int pressure;
    private int output;
    private int on;
    private int redstonePowered;

    public ZirnoxReactorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ZirnoxReactorMenu(int containerId, Inventory playerInventory, ZirnoxReactorBlockEntity blockEntity) {
        super(ModMenuTypes.ZIRNOX_REACTOR.get(), containerId);
        this.blockEntity = blockEntity;
        for (int slot = 0; slot < ROD_SLOTS.length; slot++) {
            addSlot(legacyPlainSlot(blockEntity.getItems(), slot, ROD_SLOTS[slot][0], ROD_SLOTS[slot][1]));
        }
        addSlot(legacyPlainSlot(blockEntity.getItems(),
                ZirnoxReactorBlockEntity.SLOT_CO2_INPUT, 143, 124));
        addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.getItems(),
                ZirnoxReactorBlockEntity.SLOT_CO2_OUTPUT, 143, 142));
        addSlot(legacyPlainSlot(blockEntity.getItems(),
                ZirnoxReactorBlockEntity.SLOT_WATER_INPUT, 179, 124));
        addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.getItems(),
                ZirnoxReactorBlockEntity.SLOT_WATER_OUTPUT, 179, 142));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 174, 232);
        steamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getSteamTank());
        carbonDioxideTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getCarbonDioxideTank());
        waterTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getWaterTank());
        addDataSlots();
    }

    public ZirnoxReactorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getSteamTank() {
        return steamTank;
    }

    public HbmFluidGuiHelper.TankData getCarbonDioxideTank() {
        return carbonDioxideTank;
    }

    public HbmFluidGuiHelper.TankData getWaterTank() {
        return waterTank;
    }

    public int getHeat() {
        return heat;
    }

    public int getPressure() {
        return pressure;
    }

    public int getOutput() {
        return output;
    }

    public boolean isOn() {
        return on != 0;
    }

    public boolean isRedstonePowered() {
        return redstonePowered != 0;
    }

    public int getTemperatureDisplay() {
        return (int) Math.round(heat * 1.0E-5D * 780.0D + 20.0D);
    }

    public int getPressureDisplay() {
        return (int) Math.round(pressure * 1.0E-5D * 30.0D);
    }

    public int getHeatScaled(int max) {
        return heat <= 0 ? 0 : heat * max / ZirnoxReactorBlockEntity.MAX_HEAT;
    }

    public int getPressureScaled(int max) {
        return pressure <= 0 ? 0 : pressure * max / ZirnoxReactorBlockEntity.MAX_PRESSURE;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, 256.0D);
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
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (ZirnoxFuelRuntime.isRod(stack)) {
            if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0,
                    ZirnoxReactorBlockEntity.ROD_SLOT_COUNT)) {
                return ItemStack.EMPTY;
            }
        } else if (HbmFluidItemTransfer.getItemFluid(stack).type() == HbmFluids.CARBONDIOXIDE) {
            if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, MENU_CO2_INPUT, MENU_CO2_OUTPUT + 1)) {
                return ItemStack.EMPTY;
            }
        } else if (HbmFluidItemTransfer.getItemFluid(stack).type() == HbmFluids.WATER) {
            if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, MENU_WATER_INPUT, MENU_WATER_OUTPUT + 1)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getHeat, value -> heat = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getPressure, value -> pressure = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getOutput, value -> output = value);
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override
            public int get() {
                return blockEntity.isOn() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                on = value;
            }
        });
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override
            public int get() {
                return blockEntity.isRedstonePowered() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                redstonePowered = value;
            }
        });
    }

    private static SlotItemHandler legacyPlainSlot(IItemHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }

            @Override
            public int getMaxStackSize() {
                return Math.max(super.getMaxStackSize(), hasItem() ? getItem().getCount() : 1);
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return Math.max(super.getMaxStackSize(stack), hasItem() ? getItem().getCount() : 1);
            }
        };
    }

    private static ZirnoxReactorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ZirnoxReactorBlockEntity reactor) {
            return reactor;
        }
        throw new IllegalStateException("Expected zirnox reactor block entity at " + pos);
    }
}
