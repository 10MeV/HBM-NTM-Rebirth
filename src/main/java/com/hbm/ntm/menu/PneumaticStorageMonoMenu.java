package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.PneumaticStorageMonoBlockEntity;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PneumaticStorageMonoMenu extends AbstractContainerMenu {
    private final PneumaticStorageMonoBlockEntity blockEntity;
    private final HbmFluidGuiHelper.TankData tankData;
    private final int[] amounts = new int[PneumaticStorageMonoBlockEntity.SLOT_COUNT];
    public PneumaticStorageMonoMenu(int id, Inventory inventory, FriendlyByteBuf data) { this(id, inventory, get(inventory, data.readBlockPos())); }
    public PneumaticStorageMonoMenu(int id, Inventory inventory, PneumaticStorageMonoBlockEntity blockEntity) {
        super(ModMenuTypes.PNEUMATIC_STORAGE_MONO.get(), id); this.blockEntity = blockEntity;
        for (int slot = 0; slot < PneumaticStorageMonoBlockEntity.SLOT_COUNT; slot++)
            addSlot(HbmInventoryMenuHelper.patternSlot(blockEntity.getFilters(), slot, 8, 17 + slot * 18, false));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 99, 157);
        tankData = HbmFluidGuiHelper.watchTank(this::addDataSlot, blockEntity.compair());
        for (int slot = 0; slot < PneumaticStorageMonoBlockEntity.SLOT_COUNT; slot++) { final int i = slot;
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getAmount(i), value -> amounts[i] = value);
        }
    }
    public PneumaticStorageMonoBlockEntity getBlockEntity() { return blockEntity; }
    public HbmFluidGuiHelper.TankData getTankData() { return tankData; }
    public int getAmount(int slot) { return slot >= 0 && slot < amounts.length ? amounts[slot] : 0; }
    public int getRangeFromPressure() { return com.hbm.ntm.uninos.networkproviders.pneumatic.PneumaticUtil.rangeForPressure(tankData.pressure()); }
    @Override public boolean stillValid(Player player) { return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, HbmInventoryMenuHelper.legacyMenuUseDistanceSqr(blockEntity)); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public void clicked(int slotId, int button, ClickType type, Player player) {
        if (slotId >= 0 && slotId < PneumaticStorageMonoBlockEntity.SLOT_COUNT) {
            Slot slot = slots.get(slotId);
            if (blockEntity.getAmount(slotId) > 0 && slot.hasItem()) return;
            ItemStack previous = slot.getItem().copy(); slot.set(getCarried()); setCarried(previous); broadcastChanges(); return;
        }
        super.clicked(slotId, button, type, player);
    }
    private static PneumaticStorageMonoBlockEntity get(Inventory inventory, BlockPos pos) {
        BlockEntity entity = inventory.player.level().getBlockEntity(pos);
        if (entity instanceof PneumaticStorageMonoBlockEntity storage) return storage;
        throw new IllegalStateException("Expected pneumatic storage mono at " + pos);
    }
}
