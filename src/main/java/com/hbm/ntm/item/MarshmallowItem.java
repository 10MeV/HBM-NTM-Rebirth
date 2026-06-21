package com.hbm.ntm.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MarshmallowItem extends Item {
    public static final String TAG_LEGACY_DAMAGE = "hbmLegacyDamage";

    public MarshmallowItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static boolean isRoasted(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getInt(TAG_LEGACY_DAMAGE) == 1;
    }

    public static void setRoasted(ItemStack stack, boolean roasted) {
        stack.getOrCreateTag().putInt(TAG_LEGACY_DAMAGE, roasted ? 1 : 0);
    }

    public static ItemStack roastedStack(Item item) {
        ItemStack stack = new ItemStack(item);
        setRoasted(stack, true);
        return stack;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, Item item) {
        output.accept(new ItemStack(item));
        output.accept(roastedStack(item));
    }
}
