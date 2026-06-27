package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DfcReceiverBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
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

public class DfcReceiverMenu extends AbstractContainerMenu {
    private final DfcReceiverBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData cryogel;
    private long joules;
    private long outputPower;

    public DfcReceiverMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public DfcReceiverMenu(int id, Inventory inventory, DfcReceiverBlockEntity blockEntity) {
        super(ModMenuTypes.DFC_RECEIVER.get(), id);
        this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 84, 166);
        cryogel = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getCryogelTank());
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getJoules, () -> joules, value -> joules = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getOutputPower, () -> outputPower, value -> outputPower = value);
    }

    public HbmFluidGuiHelper.TankData getCryogel() { return cryogel; }
    public long getJoules() { return joules; }
    public long getOutputPower() { return outputPower; }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static DfcReceiverBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DfcReceiverBlockEntity receiver) return receiver;
        throw new IllegalStateException("Expected DFC receiver at " + pos);
    }
}
