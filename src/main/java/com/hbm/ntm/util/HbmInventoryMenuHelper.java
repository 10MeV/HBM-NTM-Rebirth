package com.hbm.ntm.util;

import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.item.ItemMachineUpgrade;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class HbmInventoryMenuHelper {
    public static final String LEGACY_ITEMS_TAG = HbmItemStackUtil.LEGACY_ITEMS_TAG;
    public static final String LEGACY_SLOT_TAG = HbmItemStackUtil.LEGACY_SLOT_TAG;

    private HbmInventoryMenuHelper() {
    }

    public static SlotItemHandler outputSlot(IItemHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
    }

    public static SlotItemHandler craftingOutputSlot(Player player, IItemHandler items, int slot, int x, int y) {
        return outputSlot(player, items, slot, x, y, null);
    }

    public static SlotItemHandler smeltingOutputSlot(Player player, IItemHandler items, int slot, int x, int y,
            OutputExperienceGetter experienceGetter) {
        return outputSlot(player, items, slot, x, y, experienceGetter);
    }

    private static SlotItemHandler outputSlot(Player player, IItemHandler items, int slot, int x, int y,
            OutputExperienceGetter experienceGetter) {
        return new SlotItemHandler(items, slot, x, y) {
            private int removeCount;

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack remove(int amount) {
                if (hasItem()) {
                    removeCount += Math.min(amount, getItem().getCount());
                }
                return super.remove(amount);
            }

            @Override
            protected void onQuickCraft(ItemStack stack, int amount) {
                removeCount += amount;
                super.onQuickCraft(stack, amount);
            }

            @Override
            protected void checkTakeAchievements(ItemStack stack) {
                stack.onCraftedBy(player.level(), player, removeCount);
                if (experienceGetter != null && player.level() instanceof ServerLevel serverLevel) {
                    awardOutputExperience(player, serverLevel, stack, removeCount, experienceGetter);
                }
                removeCount = 0;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                checkTakeAchievements(stack);
                super.onTake(player, stack);
            }
        };
    }

    public static SlotItemHandler takeOnlySlot(IItemHandler items, int slot, int x, int y) {
        return outputSlot(items, slot, x, y);
    }

    public static SlotItemHandler validatedSlot(IItemHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y);
    }

    public static SlotItemHandler upgradeSlot(IItemHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemMachineUpgrade;
            }
        };
    }

    public static SlotItemHandler patternSlot(IItemHandler items, int slot, int x, int y) {
        return patternSlot(items, slot, x, y, false, true);
    }

    public static SlotItemHandler patternSlot(IItemHandler items, int slot, int x, int y, boolean allowStackSize) {
        return patternSlot(items, slot, x, y, allowStackSize, true);
    }

    public static SlotItemHandler hiddenPatternSlot(IItemHandler items, int slot, int x, int y) {
        return patternSlot(items, slot, x, y, false, false);
    }

    public static SlotItemHandler patternSlot(IItemHandler items, int slot, int x, int y, boolean allowStackSize,
            boolean canHover) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 1;
            }

            @Override
            public void set(ItemStack stack) {
                super.set(copyPatternStack(stack, allowStackSize));
            }

            @Override
            public boolean isActive() {
                return canHover;
            }
        };
    }

    public static SlotItemHandler legacyMachineSlot(IItemHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        };
    }

    public static SlotItemHandler deprecatedSlot(IItemHandler items, int slot, int x, int y) {
        return new SlotItemHandler(items, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }
        };
    }

    public static void addPlayerInventory(SlotSink sink, Inventory inventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                sink.add(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
    }

    public static void addHotbar(SlotSink sink, Inventory inventory, int x, int y) {
        for (int column = 0; column < 9; column++) {
            sink.add(new Slot(inventory, column, x + column * 18, y));
        }
    }

    public static void addPlayerInventoryAndHotbar(SlotSink sink, Inventory inventory,
            int x, int inventoryY, int hotbarY) {
        addPlayerInventory(sink, inventory, x, inventoryY);
        addHotbar(sink, inventory, x, hotbarY);
    }

    public static void addSlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows, int columns) {
        addSlots(sink, items, from, x, y, rows, columns, 18);
    }

    public static void addSlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows, int columns,
            int slotSize) {
        addSlotGrid(sink, items, from, x, y, rows, columns, slotSize, HbmInventoryMenuHelper::validatedSlot);
    }

    public static void addOutputSlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows,
            int columns) {
        addOutputSlots(sink, items, from, x, y, rows, columns, 18);
    }

    public static void addOutputSlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows,
            int columns, int slotSize) {
        addSlotGrid(sink, items, from, x, y, rows, columns, slotSize, HbmInventoryMenuHelper::outputSlot);
    }

    public static void addCraftingOutputSlots(SlotSink sink, Player player, IItemHandler items, int from, int x, int y,
            int rows, int columns) {
        addCraftingOutputSlots(sink, player, items, from, x, y, rows, columns, 18);
    }

    public static void addCraftingOutputSlots(SlotSink sink, Player player, IItemHandler items, int from, int x, int y,
            int rows, int columns, int slotSize) {
        addSlotGrid(sink, items, from, x, y, rows, columns, slotSize,
                (handler, slot, slotX, slotY) -> craftingOutputSlot(player, handler, slot, slotX, slotY));
    }

    public static void addSmeltingOutputSlots(SlotSink sink, Player player, IItemHandler items, int from, int x, int y,
            int rows, int columns, OutputExperienceGetter experienceGetter) {
        addSmeltingOutputSlots(sink, player, items, from, x, y, rows, columns, 18, experienceGetter);
    }

    public static void addSmeltingOutputSlots(SlotSink sink, Player player, IItemHandler items, int from, int x, int y,
            int rows, int columns, int slotSize, OutputExperienceGetter experienceGetter) {
        addSlotGrid(sink, items, from, x, y, rows, columns, slotSize,
                (handler, slot, slotX, slotY) -> smeltingOutputSlot(player, handler, slot, slotX, slotY,
                        experienceGetter));
    }

    public static void addTakeOnlySlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows,
            int columns) {
        addTakeOnlySlots(sink, items, from, x, y, rows, columns, 18);
    }

    public static void addTakeOnlySlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows,
            int columns, int slotSize) {
        addSlotGrid(sink, items, from, x, y, rows, columns, slotSize, HbmInventoryMenuHelper::takeOnlySlot);
    }

    public static void addUpgradeSlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows,
            int columns) {
        addUpgradeSlots(sink, items, from, x, y, rows, columns, 18);
    }

    public static void addUpgradeSlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows,
            int columns, int slotSize) {
        addSlotGrid(sink, items, from, x, y, rows, columns, slotSize, HbmInventoryMenuHelper::upgradeSlot);
    }

    public static void addPatternSlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows,
            int columns) {
        addPatternSlots(sink, items, from, x, y, rows, columns, 18, false);
    }

    public static void addPatternSlots(SlotSink sink, IItemHandler items, int from, int x, int y, int rows,
            int columns, int slotSize, boolean allowStackSize) {
        addSlotGrid(sink, items, from, x, y, rows, columns, slotSize,
                (handler, slot, slotX, slotY) -> patternSlot(handler, slot, slotX, slotY, allowStackSize));
    }

    public static void addSlotGrid(SlotSink sink, IItemHandler items, int from, int x, int y, int rows, int columns,
            int slotSize, SlotFactory factory) {
        if (rows <= 0 || columns <= 0 || slotSize <= 0) {
            return;
        }
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int slot = from + column + row * columns;
                sink.add(factory.create(items, slot, x + column * slotSize, y + row * slotSize));
            }
        }
    }

    public static boolean stillValidBlockEntity(Player player, BlockEntity blockEntity, double maxDistanceSqr) {
        return !blockEntity.isRemoved() && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D) <= maxDistanceSqr;
    }

    public static boolean stillValidMultiblockMachine(Player player, BlockEntity blockEntity, double maxDistanceSqr) {
        if (blockEntity.isRemoved()) {
            return false;
        }
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock machineBlock)) {
            return stillValidBlockEntity(player, blockEntity, maxDistanceSqr);
        }
        AABB bounds = machineBlock.definition().renderBoundingBox(state, blockEntity.getBlockPos());
        return distanceToSqr(bounds, player.position()) <= maxDistanceSqr;
    }

    private static double distanceToSqr(AABB bounds, Vec3 point) {
        double dx = axisDistance(point.x, bounds.minX, bounds.maxX);
        double dy = axisDistance(point.y, bounds.minY, bounds.maxY);
        double dz = axisDistance(point.z, bounds.minZ, bounds.maxZ);
        return dx * dx + dy * dy + dz * dz;
    }

    private static double axisDistance(double value, double min, double max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0.0D;
    }

    private static ItemStack copyPatternStack(ItemStack stack, boolean allowStackSize) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        if (!allowStackSize) {
            copy.setCount(1);
        }
        return copy;
    }

    private static void awardOutputExperience(Player player, ServerLevel level, ItemStack stack, int removeCount,
            OutputExperienceGetter experienceGetter) {
        if (removeCount <= 0) {
            return;
        }
        double totalExperience = removeCount * Math.max(0.0D, experienceGetter.get(stack));
        int award = Mth.floor(totalExperience);
        if (award < Mth.ceil(totalExperience) && player.getRandom().nextDouble() < totalExperience - award) {
            award++;
        }
        if (award > 0) {
            ExperienceOrb.award(level, player.position(), award);
        }
    }

    public static ItemStack moveMachineStack(java.util.List<Slot> slots, StackMover mover, int index, int machineSlotCount,
            int playerInventoryStart, int playerSlotEnd, int... machineInsertionRanges) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (index < machineSlotCount) {
            if (!mover.move(stack, playerInventoryStart, playerSlotEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveToAnyRange(slots, stack, machineInsertionRanges)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    public static boolean moveStackToAnyRange(java.util.List<Slot> slots, ItemStack stack, int... ranges) {
        return moveToAnyRange(slots, stack, ranges);
    }

    public static boolean isBatteryLike(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof HbmBatteryItem
                || stack.getCapability(ForgeCapabilities.ENERGY, null).isPresent());
    }

    public static CompoundTag saveLegacyItems(ItemStackHandler items) {
        return HbmItemStackUtil.saveLegacyItems(items);
    }

    public static CompoundTag saveLegacyItems(NonNullList<ItemStack> items) {
        return HbmItemStackUtil.saveLegacyItems(items);
    }

    public static void loadLegacyItems(CompoundTag tag, ItemStackHandler items) {
        HbmItemStackUtil.loadLegacyItems(tag, items);
    }

    public static NonNullList<ItemStack> loadLegacyItems(CompoundTag tag, int slotCount) {
        return HbmItemStackUtil.loadLegacyItems(tag, slotCount);
    }

    public static java.util.List<ItemStack> clearToDrops(ItemStackHandler items) {
        return HbmItemStackUtil.clearToDrops(items);
    }

    private static boolean moveToAnyRange(java.util.List<Slot> slots, ItemStack stack, int... ranges) {
        if (ranges == null || ranges.length % 2 != 0) {
            return false;
        }
        boolean moved = false;
        for (int i = 0; i < ranges.length; i += 2) {
            moved |= legacyMergeItemStack(slots, stack, ranges[i], ranges[i + 1], false);
            if (stack.isEmpty()) {
                break;
            }
        }
        return moved;
    }

    private static boolean legacyMergeItemStack(java.util.List<Slot> slots, ItemStack stack, int start, int end,
            boolean reverse) {
        boolean moved = false;
        if (stack.isStackable()) {
            int index = reverse ? end - 1 : start;
            while (!stack.isEmpty() && inRange(index, start, end, reverse)) {
                Slot slot = slots.get(index);
                ItemStack current = slot.getItem();
                if (!current.isEmpty() && ItemStack.isSameItemSameTags(stack, current)) {
                    int max = Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize());
                    int transfer = Math.min(stack.getCount(), max - current.getCount());
                    if (transfer > 0 && slot.mayPlace(stack.copyWithCount(transfer))) {
                        stack.shrink(transfer);
                        current.grow(transfer);
                        slot.setByPlayer(current);
                        slot.setChanged();
                        moved = true;
                    }
                }
                index += reverse ? -1 : 1;
            }
        }

        if (!stack.isEmpty()) {
            int index = reverse ? end - 1 : start;
            while (!stack.isEmpty() && inRange(index, start, end, reverse)) {
                Slot slot = slots.get(index);
                ItemStack current = slot.getItem();
                if (current.isEmpty()) {
                    int transfer = Math.min(stack.getCount(),
                            Math.min(slot.getMaxStackSize(stack), stack.getMaxStackSize()));
                    if (transfer > 0 && slot.mayPlace(stack.copyWithCount(transfer))) {
                        slot.setByPlayer(stack.split(transfer));
                        slot.setChanged();
                        moved = true;
                    }
                }
                index += reverse ? -1 : 1;
            }
        }
        return moved;
    }

    private static boolean inRange(int index, int start, int end, boolean reverse) {
        return reverse ? index >= start : index < end;
    }

    @FunctionalInterface
    public interface SlotSink {
        void add(Slot slot);
    }

    @FunctionalInterface
    public interface SlotFactory {
        Slot create(IItemHandler items, int slot, int x, int y);
    }

    @FunctionalInterface
    public interface OutputExperienceGetter {
        double get(ItemStack stack);
    }

    @FunctionalInterface
    public interface StackMover {
        boolean move(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection);
    }
}
