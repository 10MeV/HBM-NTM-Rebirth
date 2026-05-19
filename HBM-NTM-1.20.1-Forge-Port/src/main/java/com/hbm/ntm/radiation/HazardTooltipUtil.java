package com.hbm.ntm.radiation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public final class HazardTooltipUtil {
    public static void addHazardInformation(ItemStack stack, List<Component> tooltip) {
        if (stack.isEmpty()) {
            return;
        }

        float radiation = HazardRegistry.getHazardLevel(stack, HazardType.RADIATION);
        if (radiation > 0.0F) {
            tooltip.add(Component.translatable("tooltip.hbm.radiation.single", format(radiation))
                    .withStyle(colorForRadiation(radiation)));
            if (stack.getCount() > 1) {
                tooltip.add(Component.translatable("tooltip.hbm.radiation.total", format(radiation * stack.getCount()))
                        .withStyle(ChatFormatting.RED));
            }
        }

        for (HazardEntry entry : HazardRegistry.getHazards(stack)) {
            if (entry.type() != HazardType.RADIATION) {
                tooltip.add(Component.translatable("tooltip.hbm.hazard." + entry.type().name().toLowerCase(Locale.ROOT), format(entry.level()))
                        .withStyle(ChatFormatting.GOLD));
            }
        }

        double resistance = HazmatRegistry.getResistance(stack);
        if (resistance > 0.0D) {
            double blocked = (1.0D - Math.pow(10.0D, -resistance)) * 100.0D;
            tooltip.add(Component.translatable("tooltip.hbm.radiation.resistance", format(resistance), format(blocked))
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    private static ChatFormatting colorForRadiation(float radiation) {
        if (radiation < 1.0F) {
            return ChatFormatting.YELLOW;
        }
        if (radiation < 10.0F) {
            return ChatFormatting.GOLD;
        }
        if (radiation < 100.0F) {
            return ChatFormatting.RED;
        }
        return ChatFormatting.DARK_RED;
    }

    private static String format(double value) {
        if (value >= 100.0D) {
            return Integer.toString((int) Math.round(value));
        }
        return Double.toString(Math.round(value * 100.0D) / 100.0D);
    }

    private HazardTooltipUtil() {
    }
}
