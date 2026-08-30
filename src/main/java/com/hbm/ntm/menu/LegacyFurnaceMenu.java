package com.hbm.ntm.menu;

import com.hbm.ntm.blockentity.LegacyFurnaceBlockEntity;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmMenuDataSlots;
import com.hbm.ntm.multiblock.MultiblockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LegacyFurnaceMenu extends AbstractContainerMenu {
    private final LegacyFurnaceBlockEntity blockEntity;
    private int maxBurnTime;
    private int burnTime;
    private int ironProgress;
    private int ironProcessingTime;
    private int wasOn;
    private int heat;
    private final int[] steelProgress = new int[3];
    private final int[] steelBonus = new int[3];

    public LegacyFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, data.readBlockPos()));
    }

    public LegacyFurnaceMenu(int containerId, Inventory playerInventory, LegacyFurnaceBlockEntity blockEntity) {
        super(ModMenuTypes.LEGACY_FURNACE.get(), containerId);
        this.blockEntity = blockEntity;
        if (blockEntity.kind() == LegacyFurnaceBlockEntity.Kind.IRON) {
            addIronSlots(playerInventory);
        } else {
            addSteelSlots(playerInventory);
        }
        addDataSlots();
    }

    private void addIronSlots(Inventory playerInventory) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 53, 17));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 53, 53));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 2, 71, 53));
        addSlot(HbmInventoryMenuHelper.smeltingOutputSlot(playerInventory.player,
                blockEntity.getItems(), 3, 125, 35, null));
        addSlot(HbmInventoryMenuHelper.upgradeSlot(blockEntity.getItems(), 4, 17, 35));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    private void addSteelSlots(Inventory playerInventory) {
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 0, 35, 17));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 1, 35, 35));
        addSlot(HbmInventoryMenuHelper.legacyMachineSlot(blockEntity.getItems(), 2, 35, 53));
        addSlot(HbmInventoryMenuHelper.smeltingOutputSlot(playerInventory.player,
                blockEntity.getItems(), 3, 125, 17, null));
        addSlot(HbmInventoryMenuHelper.smeltingOutputSlot(playerInventory.player,
                blockEntity.getItems(), 4, 125, 35, null));
        addSlot(HbmInventoryMenuHelper.smeltingOutputSlot(playerInventory.player,
                blockEntity.getItems(), 5, 125, 53, null));
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 84, 142);
    }

    public LegacyFurnaceBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getProgressWidth(int maxWidth) {
        return ironProcessingTime <= 0 ? 0 : ironProgress * maxWidth / ironProcessingTime;
    }

    public int getIronProgress() {
        return ironProgress;
    }

    public int getIronProcessingTime() {
        return ironProcessingTime;
    }

    public int getBurnWidth(int maxWidth) {
        return maxBurnTime <= 0 ? 0 : burnTime * maxWidth / maxBurnTime;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public boolean wasOn() {
        return wasOn != 0;
    }

    public int getHeatBarHeight(int maxHeight) {
        return heat * maxHeight / 100_000;
    }

    public int getHeat() {
        return heat;
    }

    public int getSteelMaxHeat() {
        return 100_000;
    }

    public int getSteelProcessTime() {
        return 40_000;
    }

    public int getSteelProgress(int lane) {
        return lane >= 0 && lane < steelProgress.length ? steelProgress[lane] : 0;
    }

    public int getSteelBonus(int lane) {
        return lane >= 0 && lane < steelBonus.length ? steelBonus[lane] : 0;
    }

    public int getSteelProgressWidth(int lane, int maxWidth) {
        return steelProgress[lane] * maxWidth / 40_000;
    }

    public int getSteelBonusWidth(int lane, int maxWidth) {
        return Math.min(steelBonus[lane], 100) * maxWidth / 100;
    }

    @Override
    public boolean stillValid(Player player) {
        return HbmInventoryMenuHelper.stillValidMultiblockMachine(player, blockEntity, HbmInventoryMenuHelper.legacyMenuUseDistanceSqr(blockEntity));
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
        int machineSlotCount = machineSlotCount();
        int playerSlotEnd = machineSlotCount + 36;
        if (index < machineSlotCount) {
            if (!moveItemStackTo(stack, machineSlotCount, playerSlotEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!movePlayerStackToMachine(stack)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    private int machineSlotCount() {
        return blockEntity.kind() == LegacyFurnaceBlockEntity.Kind.IRON ? 5 : 6;
    }

    private boolean movePlayerStackToMachine(ItemStack stack) {
        if (blockEntity.kind() == LegacyFurnaceBlockEntity.Kind.IRON) {
            return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0, 5);
        }
        return HbmInventoryMenuHelper.moveStackToAnyRange(slots, stack, 0, 3);
    }

    private void addDataSlots() {
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getMaxBurnTime(),
                value -> maxBurnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getBurnTime(), value -> burnTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getIronProgress(),
                value -> ironProgress = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getIronProcessingTime(),
                value -> ironProcessingTime = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.wasOn() ? 1 : 0, value -> wasOn = value);
        HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getHeat(), value -> heat = value);
        for (int i = 0; i < 3; i++) {
            int lane = i;
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getSteelProgress(lane),
                    value -> steelProgress[lane] = value);
            HbmMenuDataSlots.addInt(this::addDataSlot, () -> blockEntity.getSteelBonus(lane),
                    value -> steelBonus[lane] = value);
        }
    }

    private static LegacyFurnaceBlockEntity getBlockEntity(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = MultiblockHelper.resolveCoreBlockEntity(inventory.player.level(), pos);
        if (blockEntity instanceof LegacyFurnaceBlockEntity furnace) {
            return furnace;
        }
        throw new IllegalStateException("Expected legacy furnace block entity at " + pos);
    }
}
