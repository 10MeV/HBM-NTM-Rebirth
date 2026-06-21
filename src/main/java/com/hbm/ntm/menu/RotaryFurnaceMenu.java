package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.inventory.material.Mats;
import com.hbm.ntm.blockentity.RotaryFurnaceBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
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
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;

public class RotaryFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = RotaryFurnaceBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final RotaryFurnaceBlockEntity blockEntity;
    private HbmFluidGuiHelper.TankData inputTank;
    private HbmFluidGuiHelper.TankData steamTank;
    private HbmFluidGuiHelper.TankData spentSteamTank;
    private int progressScaled;
    private int burnTime;
    private int maxBurnTime;
    private int outputAmount;
    private int outputColor;
    private int outputMaterialId = -1;
    private String outputMaterialName = "";
    private boolean progressing;
    private boolean venting;

    public RotaryFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public RotaryFurnaceMenu(int containerId, Inventory playerInventory, RotaryFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.ROTARY_FURNACE.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                RotaryFurnaceBlockEntity.SLOT_INPUT_0, 8, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                RotaryFurnaceBlockEntity.SLOT_INPUT_1, 26, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                RotaryFurnaceBlockEntity.SLOT_INPUT_2, 44, 18));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                RotaryFurnaceBlockEntity.SLOT_FLUID_ID, 8, 54));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(),
                RotaryFurnaceBlockEntity.SLOT_FUEL, 44, 54));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 104, 162);
        addDataSlots();
    }

    public double getProgressRatio() {
        return MthClamp.ratio(progressScaled, 10_000);
    }

    public int getProgressPixels() {
        return (int) Math.ceil(getProgressRatio() * 33.0D);
    }

    public int getBurnPixels() {
        return maxBurnTime <= 0 ? 0 : Math.max(0, Math.min(14, burnTime * 14 / maxBurnTime));
    }

    public int getOutputPixels() {
        return Math.max(0, Math.min(52, outputAmount * 52 / RotaryFurnaceBlockEntity.MAX_OUTPUT));
    }

    public int getOutputAmount() {
        return outputAmount;
    }

    public int getOutputColor() {
        return outputColor;
    }

    public String getOutputText(boolean showMb) {
        if (outputAmount <= 0 || outputMaterialId < 0 || outputMaterialName.isEmpty()) {
            return "Empty";
        }
        return outputMaterialName + ": " + Mats.formatAmount(outputAmount, showMb);
    }

    public boolean isProgressing() {
        return progressing;
    }

    public boolean isVenting() {
        return venting;
    }

    public HbmFluidGuiHelper.TankData getInputTank() {
        return inputTank;
    }

    public HbmFluidGuiHelper.TankData getSteamTank() {
        return steamTank;
    }

    public HbmFluidGuiHelper.TankData getSpentSteamTank() {
        return spentSteamTank;
    }

    public List<Component> getInputTankTooltip(boolean showHidden) {
        return inputTank.tooltip(showHidden);
    }

    public List<Component> getSteamTankTooltip(boolean showHidden) {
        return steamTank.tooltip(showHidden);
    }

    public List<Component> getSpentSteamTankTooltip(boolean showHidden) {
        return spentSteamTank.tooltip(showHidden);
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
        if (ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    RotaryFurnaceBlockEntity.SLOT_FUEL, RotaryFurnaceBlockEntity.SLOT_FUEL + 1);
        }
        if (stack.getItem() instanceof IFluidIdentifierItem) {
            return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                    MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                    RotaryFurnaceBlockEntity.SLOT_FLUID_ID, RotaryFurnaceBlockEntity.SLOT_FLUID_ID + 1);
        }
        return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index,
                MACHINE_SLOT_COUNT, PLAYER_INVENTORY_START, PLAYER_SLOT_END,
                RotaryFurnaceBlockEntity.SLOT_INPUT_0, RotaryFurnaceBlockEntity.SLOT_INPUT_2 + 1);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getProgressScaled,
                value -> progressScaled = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBurnTime, value -> burnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getMaxBurnTime, value -> maxBurnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getOutputAmount, value -> outputAmount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getOutputColor, value -> outputColor = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getOutputMaterialId,
                value -> outputMaterialId = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isProgressing, value -> progressing = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isVenting, value -> venting = value);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        steamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getSteamTank());
        spentSteamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getSpentSteamTank());
        outputMaterialName = blockEntity.getOutputMaterialName();
    }

    private static RotaryFurnaceBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RotaryFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected rotary furnace block entity at " + pos);
    }

    private static final class MthClamp {
        private static double ratio(int value, int max) {
            if (max <= 0) {
                return 0.0D;
            }
            return Math.max(0.0D, Math.min(1.0D, value / (double) max));
        }
    }
}
