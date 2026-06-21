package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.multiblock.MultiblockHelper;
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

public class RBMKControlAutoMenu extends AbstractContainerMenu {
    private final RBMKColumnBlockEntity blockEntity;
    private int levelLower;
    private int levelUpper;
    private int heatLower;
    private int heatUpper;
    private int function;
    private int levelPercent;

    public RBMKControlAutoMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKControlAutoMenu(int containerId, Inventory inventory, RBMKColumnBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_CONTROL_AUTO.get(), containerId);
        this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 104, 162);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> (int) Math.round(blockEntity.autoSettings().levelLower()), value -> levelLower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> (int) Math.round(blockEntity.autoSettings().levelUpper()), value -> levelUpper = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> (int) Math.round(blockEntity.autoSettings().heatLower()), value -> heatLower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> (int) Math.round(blockEntity.autoSettings().heatUpper()), value -> heatUpper = value);
        addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override
            public int get() {
                return blockEntity.hasOperationalLayout() ? blockEntity.autoSettings().function().ordinal() : 0;
            }

            @Override
            public void set(int value) {
                function = value;
            }
        });
        HbmMenuDataSlots.addInt(this::addDataSlot,
                () -> blockEntity.hasOperationalLayout()
                        ? (int) Math.round(blockEntity.controlState().level() * 100.0D)
                        : 0,
                value -> levelPercent = value);
    }

    public RBMKColumnBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getLevelLower() {
        return levelLower;
    }

    public int getLevelUpper() {
        return levelUpper;
    }

    public int getHeatLower() {
        return heatLower;
    }

    public int getHeatUpper() {
        return heatUpper;
    }

    public int getFunction() {
        return function;
    }

    public int getLevelPercent() {
        return levelPercent;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D)
                && MultiblockHelper.isOperationalCoreLayoutComplete(player.level(), blockEntity.getBlockPos());
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
                && column.kind().control() && column.kind().automatic()) {
            return column;
        }
        throw new IllegalStateException("Expected RBMK automatic control column block entity at " + pos);
    }
}
