package com.hbm.ntm.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ZirnoxRodItem extends Item {
    private static final String LEGACY_LIFE_KEY = "life";

    private final int heat;
    private final boolean breeding;

    public ZirnoxRodItem(Properties properties, int heat, boolean breeding) {
        super(properties);
        this.heat = heat;
        this.breeding = breeding;
    }

    public int heat() {
        return heat;
    }

    public boolean breeding() {
        return breeding;
    }

    public static int getLifeTime(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(LEGACY_LIFE_KEY)) {
            return stack.getTag().getInt(LEGACY_LIFE_KEY);
        }
        return stack.getDamageValue();
    }

    public static void setLifeTime(ItemStack stack, int time) {
        int maxLife = stack.getMaxDamage();
        int clamped = maxLife > 0 ? Math.max(0, Math.min(time, maxLife)) : Math.max(0, time);
        stack.getOrCreateTag().putInt(LEGACY_LIFE_KEY, clamped);
        if (stack.isDamageableItem()) {
            stack.setDamageValue(clamped);
        }
    }

    public static void incrementLifeTime(ItemStack stack) {
        setLifeTime(stack, getLifeTime(stack) + 1);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getLifeTime(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int maxLife = stack.getMaxDamage();
        if (maxLife <= 0) {
            return 13;
        }
        return Math.round(13.0F - (float) getLifeTime(stack) * 13.0F / (float) maxLife);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }
}
