package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LegacyTemFlakesItem extends Item {
    public static final String TAG_LEGACY_DAMAGE = "hbmLegacyDamage";

    public LegacyTemFlakesItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            player.heal(2.0F);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        switch (legacyDamage(stack)) {
            case 1 -> tooltip.add(Component.literal("Heals 2HP food of tem"));
            case 2 -> tooltip.add(Component.literal("Heals food of tem (expensiv)"));
            default -> tooltip.add(Component.literal("Heals 2HP DISCOUNT FOOD OF TEM!!!"));
        }
    }

    public static ItemStack stack(Item item, int legacyDamage) {
        ItemStack stack = new ItemStack(item);
        setLegacyDamage(stack, legacyDamage);
        return stack;
    }

    public static void setLegacyDamage(ItemStack stack, int legacyDamage) {
        stack.getOrCreateTag().putInt(TAG_LEGACY_DAMAGE, clampLegacyDamage(legacyDamage));
    }

    public static int legacyDamage(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(TAG_LEGACY_DAMAGE)) {
            return clampLegacyDamage(stack.getTag().getInt(TAG_LEGACY_DAMAGE));
        }
        return clampLegacyDamage(stack.getDamageValue());
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, Item item) {
        output.accept(stack(item, 0));
        output.accept(stack(item, 1));
        output.accept(stack(item, 2));
    }

    private static int clampLegacyDamage(int legacyDamage) {
        return Math.max(0, Math.min(2, legacyDamage));
    }
}
