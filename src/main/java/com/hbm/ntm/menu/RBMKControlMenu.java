package com.hbm.ntm.menu;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.neutron.RBMKControlState;
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

public class RBMKControlMenu extends AbstractContainerMenu {
    private final RBMKColumnBlockEntity blockEntity;
    private int levelPercent;
    private int targetPercent;
    private int color = -1;
    private long power;
    private boolean powered;
    private boolean hasPower;

    public RBMKControlMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKControlMenu(int containerId, Inventory inventory, RBMKColumnBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_CONTROL.get(), containerId);
        this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 104, 162);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity.hasOperationalLayout()
                        ? (int) Math.round(blockEntity.controlState().level() * 100.0D)
                        : 0,
                value -> levelPercent = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity.hasOperationalLayout()
                        ? (int) Math.round(blockEntity.controlState().targetLevel() * 100.0D)
                        : 0,
                value -> targetPercent = value);
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override
            public int get() {
                return !blockEntity.hasOperationalLayout() || blockEntity.color() == null
                        ? -1
                        : blockEntity.color().ordinal();
            }

            @Override
            public void set(int value) {
                color = value;
            }
        });
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isPoweredControlRod,
                value -> powered = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::controlHasPower, value -> hasPower = value);
    }

    public RBMKColumnBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getLevelPercent() {
        return levelPercent;
    }

    public int getTargetPercent() {
        return targetPercent;
    }

    public int getColor() {
        return color;
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return RBMKControlState.MAX_POWER;
    }

    public boolean isPoweredControlRod() {
        return powered;
    }

    public boolean hasPower() {
        return hasPower;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static RBMKColumnBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().isClientSide
                ? MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos)
                : MultiblockHelper.resolveOperationalCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RBMKColumnBlockEntity column
                && column.kind().control() && !column.kind().automatic()) {
            return column;
        }
        throw new IllegalStateException("Expected RBMK manual control column block entity at " + pos);
    }
}
