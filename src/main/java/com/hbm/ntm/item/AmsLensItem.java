package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AmsLensItem extends Item {
    public static final String TAG_DAMAGE = "damage";
    public static final long LEGACY_MAX_DAMAGE = 60L * 60L * 60L * 20L * 100L;
    private final long maxLensDamage;

    public AmsLensItem(Properties properties, long maxLensDamage) {
        super(properties);
        this.maxLensDamage = maxLensDamage;
    }

    public long maxLensDamage() {
        return maxLensDamage;
    }

    public static long getLensDamage(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getLong(TAG_DAMAGE);
    }

    public static void setLensDamage(ItemStack stack, long damage) {
        stack.getOrCreateTag().putLong(TAG_DAMAGE, Math.max(0L, damage));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long damage = getLensDamage(stack);
        long remaining = Math.max(0L, maxLensDamage - damage);
        int percent = maxLensDamage <= 0L ? 0 : (int) (remaining * 100L / maxLensDamage);
        tooltip.add(Component.literal("Durability: " + remaining + "/" + maxLensDamage + " (" + percent + "%)"));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getBarWidth(stack) < 13;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (maxLensDamage <= 0L) {
            return 13;
        }
        long remaining = Math.max(0L, maxLensDamage - getLensDamage(stack));
        return Math.round(13.0F * remaining / (float) maxLensDamage);
    }
}
