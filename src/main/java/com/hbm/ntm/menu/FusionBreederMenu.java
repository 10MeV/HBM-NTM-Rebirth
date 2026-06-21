package com.hbm.ntm.menu;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.FusionBreederBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
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

public class FusionBreederMenu extends AbstractContainerMenu {
    private static final double LEGACY_USE_DISTANCE_SQR = 128.0D;
    private static final int MACHINE_SLOT_COUNT = FusionBreederBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final FusionBreederBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData inputTank;
    private final HbmFluidGuiHelper.TankData outputTank;
    private int neutronEnergy;
    private int progressMilli;

    public FusionBreederMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public FusionBreederMenu(int containerId, Inventory inventory, FusionBreederBlockEntity blockEntity) {
        super(ModMenuTypes.FUSION_BREEDER.get(), containerId);
        this.blockEntity = blockEntity;
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), FusionBreederBlockEntity.SLOT_FLUID_ID, 26, 72));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), FusionBreederBlockEntity.SLOT_INPUT, 48, 45));
        addSlot(HbmInventoryMenuHelper.craftingOutputSlot(inventory.player, blockEntity.getItems(),
                FusionBreederBlockEntity.SLOT_OUTPUT, 112, 45));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 118, 176);
        inputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getInputTank());
        outputTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getOutputTank());
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> (int) Math.round(blockEntity.getNeutronEnergySync()),
                value -> neutronEnergy = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> (int) Math.round(blockEntity.getProgress() / FusionBreederBlockEntity.CAPACITY * 1000.0D),
                value -> progressMilli = value);
    }

    public FusionBreederBlockEntity getBlockEntity() { return blockEntity; }
    public HbmFluidGuiHelper.TankData getInputTank() { return inputTank; }
    public HbmFluidGuiHelper.TankData getOutputTank() { return outputTank; }
    public int getNeutronEnergy() { return neutronEnergy; }
    public double getProgress() { return progressMilli / 1000.0D; }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, LEGACY_USE_DISTANCE_SQR);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
        } else if (stack.getItem() instanceof IFluidIdentifierItem) {
            if (!moveItemStackTo(stack, FusionBreederBlockEntity.SLOT_FLUID_ID, FusionBreederBlockEntity.SLOT_FLUID_ID + 1, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, FusionBreederBlockEntity.SLOT_INPUT, FusionBreederBlockEntity.SLOT_INPUT + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static FusionBreederBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof FusionBreederBlockEntity breeder) return breeder;
        throw new IllegalStateException("Expected fusion breeder at " + pos);
    }
}
