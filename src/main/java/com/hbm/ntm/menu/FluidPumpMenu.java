package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.FluidPumpBlockEntity;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FluidPumpMenu extends AbstractContainerMenu {
    private final FluidPumpBlockEntity blockEntity;
    private int bufferSize;
    private int pressure;
    private int priorityOrdinal;
    private int fill;
    private boolean redstoneBlocked;

    public FluidPumpMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public FluidPumpMenu(int containerId, Inventory inventory, FluidPumpBlockEntity blockEntity) {
        super(ModMenuTypes.FLUID_PUMP.get(), containerId);
        this.blockEntity = blockEntity;
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBufferSize, value -> bufferSize = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getPressure, value -> pressure = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getPriority().ordinal(),
                value -> priorityOrdinal = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getTank().getFill(), value -> fill = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isRedstoneBlocked,
                value -> redstoneBlocked = value);
    }

    public FluidPumpBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getPressure() {
        return Math.max(0, Math.min(5, pressure));
    }

    public HbmEnergyReceiver.ConnectionPriority getPriority() {
        HbmEnergyReceiver.ConnectionPriority[] values = HbmEnergyReceiver.ConnectionPriority.values();
        return priorityOrdinal >= 0 && priorityOrdinal < values.length
                ? values[priorityOrdinal]
                : HbmEnergyReceiver.ConnectionPriority.NORMAL;
    }

    public int getFill() {
        return Math.max(0, fill);
    }

    public boolean isRedstoneBlocked() {
        return redstoneBlocked;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 128.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static FluidPumpBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof FluidPumpBlockEntity pump) {
            return pump;
        }
        throw new IllegalStateException("Expected fluid pump block entity at " + pos);
    }
}
