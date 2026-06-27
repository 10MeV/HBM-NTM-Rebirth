package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.DfcEmitterBlockEntity;
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

public class DfcEmitterMenu extends AbstractContainerMenu {
    private final DfcEmitterBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData cryogel;
    private long power;
    private int watts;
    private long prev;
    private int beam;
    private boolean on;

    public DfcEmitterMenu(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public DfcEmitterMenu(int id, Inventory inventory, DfcEmitterBlockEntity blockEntity) {
        super(ModMenuTypes.DFC_EMITTER.get(), id);
        this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 84, 166);
        cryogel = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.getCryogelTank());
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getWatts, value -> watts = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPrev, () -> prev, value -> prev = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getBeam, value -> beam = value);
        HbmMenuDataSlots.addBoolean(this::addDataSlot, blockEntity::isOn, value -> on = value);
    }

    public DfcEmitterBlockEntity getBlockEntity() { return blockEntity; }
    public HbmFluidGuiHelper.TankData getCryogel() { return cryogel; }
    public long getPower() { return power; }
    public int getWatts() { return watts; }
    public long getPrev() { return prev; }
    public int getBeam() { return beam; }
    public boolean isOn() { return on; }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 64.0D);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    private static DfcEmitterBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof DfcEmitterBlockEntity emitter) return emitter;
        throw new IllegalStateException("Expected DFC emitter at " + pos);
    }
}
