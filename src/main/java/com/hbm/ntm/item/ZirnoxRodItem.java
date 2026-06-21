package com.hbm.ntm.item;

import com.hbm.ntm.util.BobMathUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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
        int life = Math.max(0, time);
        stack.getOrCreateTag().putInt(LEGACY_LIFE_KEY, life);
        if (stack.isDamageableItem()) {
            stack.setDamageValue(maxLife > 0 ? Math.min(life, maxLife) : life);
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
        int width = Math.round(13.0F - (float) getLifeTime(stack) * 13.0F / (float) maxLife);
        return Math.max(0, Math.min(13, width));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int maxLife = stack.getMaxDamage();
        double depletion = maxLife <= 0 ? 0.0D
                : ((int) ((((double) getLifeTime(stack)) / (double) maxLife) * 100000.0D)) / 1000.0D;
        tooltip.add(Component.translatable("trait.rbmk.depletion",
                String.format(Locale.US, "%s%%", depletion)).withStyle(ChatFormatting.YELLOW));
        Component description = breeding
                ? Component.translatable("desc.item.zirnoxBreedingRod", BobMathUtil.getShortNumber(maxLife))
                : Component.translatable("desc.item.zirnoxRod", heat, BobMathUtil.getShortNumber(maxLife));
        Arrays.stream(description.getString().split("\\$"))
                .map(Component::literal)
                .forEach(tooltip::add);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }
}
