package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.PneumaticStorageImporterBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;

public class PneumaticStorageImporterMenu extends AbstractContainerMenu {
    private final PneumaticStorageImporterBlockEntity blockEntity;
    public PneumaticStorageImporterMenu(int id, Inventory inventory, FriendlyByteBuf data) { this(id, inventory, get(inventory, data.readBlockPos())); }
    public PneumaticStorageImporterMenu(int id, Inventory inventory, PneumaticStorageImporterBlockEntity blockEntity) {
        super(ModMenuTypes.PNEUMATIC_STORAGE_IMPORTER.get(), id); this.blockEntity = blockEntity;
        HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 62, 17, 3, 3, 18);
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 8, 103, 161);
    }
    public PneumaticStorageImporterBlockEntity getBlockEntity() { return blockEntity; }
    @Override public boolean stillValid(Player player) { return !blockEntity.isRemoved() && player.distanceToSqr(blockEntity.getBlockPos().getX()+.5, blockEntity.getBlockPos().getY()+.5, blockEntity.getBlockPos().getZ()+.5) <= 64D; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return HbmInventoryMenuHelper.moveMachineStack(slots, this::moveItemStackTo, index, PneumaticStorageImporterBlockEntity.SLOT_COUNT, PneumaticStorageImporterBlockEntity.SLOT_COUNT, PneumaticStorageImporterBlockEntity.SLOT_COUNT + 36, 0, PneumaticStorageImporterBlockEntity.SLOT_COUNT); }
    private static PneumaticStorageImporterBlockEntity get(Inventory inv, BlockPos pos) { BlockEntity entity = inv.player.level().getBlockEntity(pos); if (entity instanceof PneumaticStorageImporterBlockEntity importer) return importer; throw new IllegalStateException("Expected pneumatic storage importer at " + pos); }
}
