package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.ICFReactorBlockEntity;
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

public class ICFReactorMenu extends AbstractContainerMenu {
    private static final double LEGACY_USE_DISTANCE_SQR = 256.0D;
    private static final int MACHINE_SLOT_COUNT = ICFReactorBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_SLOT_END = PLAYER_INVENTORY_START + 36;

    private final ICFReactorBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData coolantTank;
    private final HbmFluidGuiHelper.TankData hotCoolantTank;
    private final HbmFluidGuiHelper.TankData stellarFluxTank;
    private long laser;
    private long maxLaser;
    private long heat;
    private long heatup;
    private int consumption;
    private int output;

    public ICFReactorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public ICFReactorMenu(int containerId, Inventory playerInventory, ICFReactorBlockEntity blockEntity) {
        super(ModMenuTypes.ICF_REACTOR.get(), containerId);
        this.blockEntity = blockEntity;
        for (int i = 0; i < 5; i++) {
            addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), i, 80 + i * 18, 18));
        }
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ICFReactorBlockEntity.SLOT_ACTIVE, 116, 54));
        for (int i = 0; i < 5; i++) {
            addSlot(HbmInventoryMenuHelper.craftingOutputSlot(playerInventory.player, blockEntity.getItems(),
                    6 + i, 80 + i * 18, 90));
        }
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), ICFReactorBlockEntity.SLOT_IDENTIFIER, 44, 90));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 44, 140, 198);
        coolantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getCoolantTank());
        hotCoolantTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getHotCoolantTank());
        stellarFluxTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getStellarFluxTank());
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getLaser, () -> laser, value -> laser = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxLaser, () -> maxLaser, value -> maxLaser = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getHeat, () -> heat, value -> heat = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getHeatup, () -> heatup, value -> heatup = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getConsumption, value -> consumption = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getOutput, value -> output = value);
    }

    public ICFReactorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getCoolantTank() {
        return coolantTank;
    }

    public HbmFluidGuiHelper.TankData getHotCoolantTank() {
        return hotCoolantTank;
    }

    public HbmFluidGuiHelper.TankData getStellarFluxTank() {
        return stellarFluxTank;
    }

    public long getLaser() { return laser; }
    public long getMaxLaser() { return maxLaser; }
    public long getHeat() { return heat; }
    public long getHeatup() { return heatup; }
    public int getConsumption() { return consumption; }
    public int getOutput() { return output; }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, LEGACY_USE_DISTANCE_SQR);
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
        } else if (stack.getItem() instanceof com.hbm.ntm.api.fluid.IFluidIdentifierItem) {
            if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 11, 12)) {
                return ItemStack.EMPTY;
            }
        } else if (!HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 5, 6, 0, 5)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static ICFReactorBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof ICFReactorBlockEntity icf) {
            return icf;
        }
        throw new IllegalStateException("Expected ICF reactor block entity at " + pos);
    }
}
