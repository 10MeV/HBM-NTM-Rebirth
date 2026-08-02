package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.PneumaticStorageExporterBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PneumaticStorageExporterMenu extends AbstractContainerMenu {
    private final PneumaticStorageExporterBlockEntity blockEntity;
    private boolean continuous;
    private boolean rorConfigured;
    private int requestMode;
    public PneumaticStorageExporterMenu(int id, Inventory inventory, FriendlyByteBuf data) { this(id, inventory, get(inventory, data.readBlockPos())); }
    public PneumaticStorageExporterMenu(int id, Inventory inventory, PneumaticStorageExporterBlockEntity blockEntity) {
        super(ModMenuTypes.PNEUMATIC_STORAGE_EXPORTER.get(), id); this.blockEntity = blockEntity;
        for (int i = 0; i < 9; i++) addSlot(HbmInventoryMenuHelper.patternSlot(blockEntity.getItems(), i, 17 + i % 3 * 18, 17 + i / 3 * 18, true));
        for (int i = 0; i < 9; i++) addSlot(HbmInventoryMenuHelper.takeOnlySlot(blockEntity.getItems(), i + 9, 80 + i % 3 * 18, 17 + i / 3 * 18));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 103, 161);
        addDataSlot(new DataSlot() { @Override public int get() { return blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide ? (continuous ? 1 : 0) : (blockEntity.isContinuousRequest() ? 1 : 0); } @Override public void set(int value) { continuous = value != 0; } });
        addDataSlot(new DataSlot() { @Override public int get() { return blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide ? requestMode : blockEntity.getRequestMode(); } @Override public void set(int value) { requestMode = value; } });
        addDataSlot(new DataSlot() { @Override public int get() { return blockEntity.getLevel() != null && blockEntity.getLevel().isClientSide ? (rorConfigured ? 1 : 0) : (blockEntity.isRorConfiguredMode() ? 1 : 0); } @Override public void set(int value) { rorConfigured = value != 0; } });
    }
    public PneumaticStorageExporterBlockEntity getBlockEntity() { return blockEntity; }
    public boolean isContinuous() { return continuous; }
    public int getRequestMode() { return requestMode; }
    public boolean isRorConfigured() { return rorConfigured; }
    @Override public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity,
                HbmInventoryMenuHelper.legacyMenuUseDistanceSqr(blockEntity));
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { if (index < 0 || index >= 18) return ItemStack.EMPTY; return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, 18, 18, 54, 0, 0); }
    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < PneumaticStorageExporterBlockEntity.FILTER_SLOT_COUNT && clickType == ClickType.PICKUP) {
            Slot slot = slots.get(slotId);
            ItemStack previous = slot.getItem().copy();
            slot.set(getCarried());
            setCarried(previous);
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }
    private static PneumaticStorageExporterBlockEntity get(Inventory inv, BlockPos pos) { BlockEntity entity = inv.player.level().getBlockEntity(pos); if (entity instanceof PneumaticStorageExporterBlockEntity exporter) return exporter; throw new IllegalStateException("Expected pneumatic storage exporter at " + pos); }
}
