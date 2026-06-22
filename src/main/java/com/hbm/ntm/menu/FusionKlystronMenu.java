package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.FusionKlystronBlockEntity;
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

public class FusionKlystronMenu extends AbstractContainerMenu {
    private static final double LEGACY_USE_DISTANCE_SQR = 128.0D;
    private static final int MACHINE_SLOT_COUNT = FusionKlystronBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final FusionKlystronBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData airTank;
    private long power;
    private long maxPower;
    private long outputTarget;
    private long output;

    public FusionKlystronMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public FusionKlystronMenu(int containerId, Inventory inventory, FusionKlystronBlockEntity blockEntity) {
        super(ModMenuTypes.FUSION_KLYSTRON.get(), containerId);
        this.blockEntity = blockEntity;
        this.maxPower = blockEntity.getMaxPower();
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), FusionKlystronBlockEntity.SLOT_BATTERY, 8, 72));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 17, 118, 176);
        airTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getAirTank());
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower, () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getOutputTarget, () -> outputTarget, value -> outputTarget = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getOutput, () -> output, value -> output = value);
    }

    public FusionKlystronBlockEntity getBlockEntity() { return blockEntity; }
    public HbmFluidGuiHelper.TankData getAirTank() { return airTank; }
    public long getPower() { return power; }
    public long getMaxPower() { return maxPower; }
    public long getOutputTarget() { return outputTarget; }
    public long getOutput() { return output; }

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
        } else if (HbmInventoryMenuHelper.isBatteryLike(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private static FusionKlystronBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof FusionKlystronBlockEntity klystron) return klystron;
        throw new IllegalStateException("Expected fusion klystron at " + pos);
    }
}
