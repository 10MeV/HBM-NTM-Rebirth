package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DepletedFuelItem extends Item {
    public static final int COLD_DAMAGE = 0;
    public static final int HOT_DAMAGE = 1;
    public static final int HOT_TINT = 0xFFBFA5;

    public DepletedFuelItem(Properties properties) {
        super(properties);
    }

    public static ItemStack stack(Item item, int damage) {
        ItemStack stack = new ItemStack(item);
        stack.setDamageValue(Math.max(COLD_DAMAGE, damage));
        return stack;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, DepletedFuelItem item) {
        output.accept(stack(item, COLD_DAMAGE));
        output.accept(stack(item, HOT_DAMAGE));
    }

    public static boolean isHot(ItemStack stack) {
        return stack.getDamageValue() > COLD_DAMAGE;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (isHot(stack)) {
            tooltip.add(Component.translatable("desc.item.wasteCooling").withStyle(ChatFormatting.GOLD));
        }
    }
}
