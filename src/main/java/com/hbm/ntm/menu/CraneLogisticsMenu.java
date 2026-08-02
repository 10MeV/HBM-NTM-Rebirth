package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.CraneLogisticsBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.util.LegacyPatternMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class CraneLogisticsMenu extends AbstractContainerMenu {
    private final CraneLogisticsBlockEntity blockEntity;
    private final int machineSlots;
    private final int firstPatternSlot;
    private final int patternSlotCount;
    private final int firstPlayerSlot;
    private final int[] patternModeIndexes;

    public CraneLogisticsMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public CraneLogisticsMenu(int containerId, Inventory playerInventory, CraneLogisticsBlockEntity blockEntity) {
        super(ModMenuTypes.CRANE_LOGISTICS.get(), containerId);
        this.blockEntity = blockEntity;
        CraneLogisticsBlockEntity.Kind kind = blockEntity.kind();
        this.machineSlots = kind.slots();
        this.patternModeIndexes = new int[kind.filterSlots()];
        int patternStart = -1;
        int patternCount = 0;

        switch (kind) {
            case EXTRACTOR -> {
                patternStart = slots.size();
                patternCount = 9;
                HbmInventoryMenuHelper.addPatternSlots(this::addSlot, blockEntity.getItems(), 0, 71, 17, 3, 3);
                HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 9, 8, 17, 3, 3);
                addSlot(craneUpgradeSlot(18, 152, 23));
                addSlot(craneUpgradeSlot(19, 152, 47));
                HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 26, 103, 161);
            }
            case INSERTER, BOXER -> {
                HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 8, 17, 3, 7);
                HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 103, 161);
            }
            case GRABBER -> {
                patternStart = slots.size();
                patternCount = 9;
                HbmInventoryMenuHelper.addPatternSlots(this::addSlot, blockEntity.getItems(), 0, 40, 17, 3, 3);
                addSlot(craneUpgradeSlot(9, 121, 23));
                addSlot(craneUpgradeSlot(10, 121, 47));
                HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 103, 161);
            }
            case ROUTER -> {
                patternStart = slots.size();
                patternCount = 30;
                for (int group = 0; group < 2; group++) {
                    for (int row = 0; row < 3; row++) {
                        for (int column = 0; column < 5; column++) {
                            int slot = column + group * 15 + row * 5;
                            addSlot(HbmInventoryMenuHelper.patternSlot(blockEntity.getItems(), slot,
                                    34 + column * 18 + group * 98, 17 + row * 26));
                        }
                    }
                }
                HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 47, 119, 177);
            }
            case UNBOXER -> {
                HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 8, 17, 3, 7);
                addSlot(craneUpgradeSlot(21, 152, 23));
                addSlot(craneUpgradeSlot(22, 152, 47));
                HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 103, 161);
            }
            case PARTITIONER -> {
                HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 0, 8, 17, 5, 9);
                HbmInventoryMenuHelper.addSlots(this::addSlot, blockEntity.getItems(), 45, 8, 119, 5, 9);
                HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 221, 279);
            }
        }

        this.firstPatternSlot = patternStart;
        this.patternSlotCount = patternCount;
        this.firstPlayerSlot = slots.size() - 36;
        addPatternDataSlots();
    }

    public CraneLogisticsBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getPatternSlotCount() {
        return patternSlotCount;
    }

    public Component getPatternModeLabel(int slot) {
        if (slot < 0 || slot >= patternModeIndexes.length) {
            return Component.empty();
        }
        String mode = LegacyPatternMatcher.modeForIndex(blockEntity.getItems().getStackInSlot(slot),
                patternModeIndexes[slot]);
        return LegacyPatternMatcher.label(mode).copy().withStyle(ChatFormatting.RED);
    }

    public CraneLogisticsBlockEntity.Kind kind() {
        return blockEntity.kind();
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, HbmInventoryMenuHelper.legacyMenuUseDistanceSqr(blockEntity));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < firstPlayerSlot) {
            if (!moveItemStackTo(stack, firstPlayerSlot, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveIntoMachine(stack)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (firstPatternSlot >= 0 && HbmInventoryMenuHelper.handleLegacyPatternSlotClick(slots, slotId, button,
                clickType, getCarried(), firstPatternSlot, patternSlotCount, blockEntity::cyclePatternMode,
                blockEntity::setPatternStack, this::broadcastChanges)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void addPatternDataSlots() {
        for (int slot = 0; slot < patternModeIndexes.length; slot++) {
            final int patternSlot = slot;
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getPatternMode(patternSlot),
                    value -> patternModeIndexes[patternSlot] = value);
        }
    }
    private boolean moveIntoMachine(ItemStack stack) {
        CraneLogisticsBlockEntity.Kind kind = blockEntity.kind();
        return switch (kind) {
            case EXTRACTOR -> HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 18, 20, 9, 18);
            case GRABBER -> HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 9, 11);
            case ROUTER -> false;
            default -> moveItemStackTo(stack, 0, machineSlots, false);
        };
    }

    private SlotItemHandler craneUpgradeSlot(int slot, int x, int y) {
        return new SlotItemHandler(blockEntity.getItems(), slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return blockEntity.isValidUpgradeForSlot(slot, stack);
            }
        };
    }

    private static CraneLogisticsBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof CraneLogisticsBlockEntity crane) {
            return crane;
        }
        throw new IllegalStateException("Expected crane logistics block entity at " + pos);
    }
}
