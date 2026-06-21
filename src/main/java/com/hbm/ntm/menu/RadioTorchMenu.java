package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.RadioTorchBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchCounterBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RadioTorchMenu extends AbstractContainerMenu {
    private static final int COUNTER_FILTER_SLOT_COUNT = RadioTorchCounterBlockEntity.FILTER_SLOT_COUNT;

    private final RadioTorchBlockEntity blockEntity;

    public RadioTorchMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public RadioTorchMenu(int containerId, Inventory inventory, RadioTorchBlockEntity blockEntity) {
        super(ModMenuTypes.RADIO_TORCH.get(), containerId);
        this.blockEntity = blockEntity;
        if (blockEntity instanceof RadioTorchCounterBlockEntity counter) {
            for (int i = 0; i < COUNTER_FILTER_SLOT_COUNT; i++) {
                addSlot(HbmInventoryMenuHelper.patternSlot(counter.getFilterItems(), i, 138, 18 + 44 * i));
            }
            HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, inventory, 12, 156, 214);
        }
    }

    public RadioTorchBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (blockEntity instanceof RadioTorchCounterBlockEntity counter
                && HbmInventoryMenuHelper.handleLegacyPatternSlotClick(slots, slotId, button, clickType,
                        getCarried(), 0, COUNTER_FILTER_SLOT_COUNT, counter::nextFilterMode, null,
                        this::broadcastChanges)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private static RadioTorchBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof RadioTorchBlockEntity torch) {
            return torch;
        }
        throw new IllegalStateException("Expected RTTY torch block entity at " + pos);
    }
}
