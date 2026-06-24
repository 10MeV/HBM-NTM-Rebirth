package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.AutocrafterBlockEntity;
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
import net.minecraftforge.items.ItemStackHandler;

public class AutocrafterMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = AutocrafterBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final AutocrafterBlockEntity blockEntity;
    private final int[] modeIndexes = new int[9];
    private long power;
    private long maxPower;
    private int recipeCount;
    private int recipeIndex;

    public AutocrafterMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory, getBlockEntity(inventory, data.readBlockPos()));
    }

    public AutocrafterMenu(int containerId, Inventory inventory, AutocrafterBlockEntity blockEntity) {
        super(ModMenuTypes.AUTOCRAFTER.get(), containerId);
        this.blockEntity = blockEntity;
        addMachineSlots(inventory, blockEntity.getItems());
        addDataSlots();
    }

    public long getPower() {
        return power;
    }

    public long getMaxPower() {
        return maxPower;
    }

    public int getPowerBarHeight(int maxHeight) {
        return maxPower <= 0L ? 0 : (int) (power * maxHeight / maxPower);
    }

    public Component getModeLabel(int slot) {
        ItemStack pattern = blockEntity.getItems().getStackInSlot(slot);
        String mode = slot >= 0 && slot < modeIndexes.length
                ? LegacyPatternMatcher.modeForIndex(pattern, modeIndexes[slot])
                : null;
        return LegacyPatternMatcher.label(mode).copy().withStyle(ChatFormatting.YELLOW);
    }

    public Component getRecipeLabel() {
        return Component.literal((recipeIndex + 1) + " / " + recipeCount).withStyle(ChatFormatting.YELLOW);
    }

    public boolean hasPreviewRecipe() {
        return recipeCount > 0 && !blockEntity.getItems().getStackInSlot(AutocrafterBlockEntity.SLOT_TEMPLATE_OUTPUT).isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidBlockEntity(player, blockEntity, 400.0D);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == AutocrafterBlockEntity.SLOT_TEMPLATE_OUTPUT) {
            if (clickType == ClickType.PICKUP && button == 1
                    && slots.get(slotId).hasItem()) {
                blockEntity.nextTemplate();
                broadcastChanges();
            }
            return;
        }
        if (HbmInventoryMenuHelper.handleLegacyPatternSlotClick(slots, slotId, button, clickType,
                getCarried(), AutocrafterBlockEntity.SLOT_TEMPLATE_START, 9, blockEntity::nextMode,
                blockEntity::updatePatternSlot, this::broadcastChanges)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
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
        ItemStack original = stack.copy();
        if (index >= AutocrafterBlockEntity.SLOT_INPUT_START && index <= AutocrafterBlockEntity.SLOT_BATTERY) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INVENTORY_START && AutocrafterBlockEntity.isLegacyBattery(stack)) {
            if (!moveItemStackTo(stack, AutocrafterBlockEntity.SLOT_BATTERY,
                    AutocrafterBlockEntity.SLOT_BATTERY + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private void addMachineSlots(Inventory playerInventory, ItemStackHandler items) {
        HbmInventoryMenuHelper.addPatternSlots(this::addSlot, items, AutocrafterBlockEntity.SLOT_TEMPLATE_START,
                44, 22, 3, 3);
        addSlot(HbmInventoryMenuHelper.patternSlot(items, AutocrafterBlockEntity.SLOT_TEMPLATE_OUTPUT,
                116, 40));
        HbmInventoryMenuHelper.addSlots(this::addSlot, items, AutocrafterBlockEntity.SLOT_INPUT_START,
                44, 86, 3, 3);
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, AutocrafterBlockEntity.SLOT_OUTPUT,
                116, 104));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(items, AutocrafterBlockEntity.SLOT_BATTERY,
                17, 99));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 158, 216);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getPower, () -> power, value -> power = value);
        HbmMenuDataSlots.addLong(this::addDataSlot, blockEntity::getMaxPower,
                () -> maxPower, value -> maxPower = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRecipeCount, value -> recipeCount = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, blockEntity::getRecipeIndex, value -> recipeIndex = value);
        for (int slot = 0; slot < modeIndexes.length; slot++) {
            final int modeSlot = slot;
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getModeIndex(modeSlot),
                    value -> modeIndexes[modeSlot] = value);
        }
    }

    private static AutocrafterBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AutocrafterBlockEntity autocrafter) {
            return autocrafter;
        }
        throw new IllegalStateException("Expected autocrafter block entity at " + pos);
    }
}
