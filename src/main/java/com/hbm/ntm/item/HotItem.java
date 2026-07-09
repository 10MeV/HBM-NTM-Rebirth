package com.hbm.ntm.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HotItem extends Item {
    public static final String TAG_HEAT = "heat";

    private final int maxHeat;

    public HotItem(Properties properties, int maxHeat) {
        super(properties);
        this.maxHeat = Math.max(1, maxHeat);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        if (level.isClientSide || !stack.hasTag()) {
            return;
        }
        CompoundTag tag = stack.getTag();
        int heat = tag.getInt(TAG_HEAT);
        if (heat > 0) {
            tag.putInt(TAG_HEAT, heat - 1);
            return;
        }
        tag.remove(TAG_HEAT);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    public int maxHeat(ItemStack stack) {
        return maxHeat;
    }

    public static boolean isHotItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HotItem;
    }

    public static ItemStack heatUp(ItemStack stack) {
        return heatUp(stack, 1.0D);
    }

    public static ItemStack heatUp(ItemStack stack, double ratio) {
        if (!(stack.getItem() instanceof HotItem hotItem)) {
            return stack;
        }
        int heat = (int) (Mth.clamp(ratio, 0.0D, 1.0D) * hotItem.maxHeat(stack));
        stack.getOrCreateTag().putInt(TAG_HEAT, heat);
        return stack;
    }

    public static double heatRatio(ItemStack stack) {
        if (!(stack.getItem() instanceof HotItem hotItem) || !stack.hasTag()) {
            return 0.0D;
        }
        int heat = stack.getTag().getInt(TAG_HEAT);
        return (double) Math.max(0, heat) / (double) hotItem.maxHeat(stack);
    }

    public static boolean hasUsableHeat(ItemStack stack) {
        return heatRatio(stack) >= 0.5D;
    }

    public static boolean isHot(ItemStack stack) {
        return heatRatio(stack) > 0.0D;
    }
}
