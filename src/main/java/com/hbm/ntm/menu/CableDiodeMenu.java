package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.CableDiodeBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CableDiodeMenu extends AbstractContainerMenu {
    private final CableDiodeBlockEntity blockEntity;
    private long throughputLimit;
    private int priorityOrdinal;

    public CableDiodeMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public CableDiodeMenu(int containerId, Inventory inventory, CableDiodeBlockEntity blockEntity) {
        super(ModMenuTypes.CABLE_DIODE.get(), containerId);
        this.blockEntity = blockEntity;
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getThroughputLimit,
                () -> throughputLimit, value -> throughputLimit = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getConfiguredPriority().ordinal(),
                value -> priorityOrdinal = value);
    }

    public CableDiodeBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public long getThroughputLimit() {
        return throughputLimit;
    }

    public int getPriorityOrdinal() {
        return priorityOrdinal;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= HbmInventoryMenuHelper.legacyMenuUseDistanceSqr(blockEntity);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static CableDiodeBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof CableDiodeBlockEntity diode) {
            return diode;
        }
        throw new IllegalStateException("Expected cable diode block entity at " + pos);
    }
}
