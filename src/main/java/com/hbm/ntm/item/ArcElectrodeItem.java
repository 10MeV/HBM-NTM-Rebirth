package com.hbm.ntm.item;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ArcElectrodeItem extends Item {
    private static final String DURABILITY_TAG = "durability";

    private final Type type;
    private final boolean burnt;

    public ArcElectrodeItem(Properties properties, Type type, boolean burnt) {
        super(properties);
        this.type = type;
        this.burnt = burnt;
    }

    public Type type() {
        return type;
    }

    public boolean burnt() {
        return burnt;
    }

    public static int getDurability(ItemStack stack) {
        return stack.hasTag() ? stack.getOrCreateTag().getInt(DURABILITY_TAG) : 0;
    }

    public static int getMaxDurability(ItemStack stack) {
        if (stack.getItem() instanceof ArcElectrodeItem electrode) {
            return electrode.type.durability();
        }
        return Type.GRAPHITE.durability();
    }

    public static boolean damage(ItemStack stack) {
        int durability = getDurability(stack) + 1;
        stack.getOrCreateTag().putInt(DURABILITY_TAG, durability);
        return durability >= getMaxDurability(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !burnt && getDurability(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float remaining = 1.0F - (float) getDurability(stack) / (float) getMaxDurability(stack);
        return Math.round(13.0F * Mth.clamp(remaining, 0.0F, 1.0F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF6A00;
    }

    public enum Type {
        GRAPHITE(10),
        LANTHANIUM(100),
        DESH(500),
        SATURNITE(1_500);

        private final int durability;

        Type(int durability) {
            this.durability = durability;
        }

        public int durability() {
            return durability;
        }
    }
}
