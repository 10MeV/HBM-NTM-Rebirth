package com.hbm.ntm.menu;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class RBMKBoilerMenu extends AbstractContainerMenu {
    private final RBMKColumnBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData feedTank;
    private final HbmFluidGuiHelper.TankData steamTank;

    public RBMKBoilerMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RBMKBoilerMenu(int containerId, Inventory inventory, RBMKColumnBlockEntity blockEntity) {
        super(ModMenuTypes.RBMK_BOILER.get(), containerId);
        this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 104, 162);
        feedTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.boilerFeedTank(),
                blockEntity::hasOperationalLayout);
        steamTank = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.boilerSteamTank(),
                blockEntity::hasOperationalLayout);
    }

    public RBMKColumnBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public HbmFluidGuiHelper.TankData getFeedTank() {
        return feedTank;
    }

    public HbmFluidGuiHelper.TankData getSteamTank() {
        return steamTank;
    }

    public List<Component> getFeedTankTooltip(boolean showHidden) {
        return feedTank.tooltip(showHidden);
    }

    public List<Component> getSteamTankTooltip(boolean showHidden) {
        return steamTank.tooltip(showHidden);
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
                && column.kind() == RBMKColumnBlock.Kind.BOILER) {
            return column;
        }
        throw new IllegalStateException("Expected RBMK boiler column block entity at " + pos);
    }
}
