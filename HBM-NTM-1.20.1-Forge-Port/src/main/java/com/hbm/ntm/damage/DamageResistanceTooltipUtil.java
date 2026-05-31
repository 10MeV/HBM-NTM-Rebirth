package com.hbm.ntm.damage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public final class DamageResistanceTooltipUtil {
    public static void addResistanceInformation(ItemStack stack, List<Component> tooltip) {
        if (stack.isEmpty()) {
            return;
        }
        DamageResistanceStats set = DamageResistanceHandler.setStatsForItem(stack.getItem());
        if (set != null) {
            addStats(tooltip, "tooltip.hbm.damage.set", set);
        }
        DamageResistanceStats item = DamageResistanceHandler.itemStats(stack.getItem());
        if (item != null) {
            addStats(tooltip, "tooltip.hbm.damage.item", item);
        }
    }

    private static void addStats(List<Component> tooltip, String titleKey, DamageResistanceStats stats) {
        if (stats.exactResistances().isEmpty() && stats.categoryResistances().isEmpty() && stats.otherResistance() == null) {
            return;
        }
        tooltip.add(Component.translatable(titleKey).withStyle(ChatFormatting.DARK_PURPLE));
        for (Map.Entry<String, DamageResistance> entry : stats.categoryResistances().entrySet()) {
            tooltip.add(line("tooltip.hbm.damage.category." + entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<String, DamageResistance> entry : stats.exactResistances().entrySet()) {
            tooltip.add(line("tooltip.hbm.damage.exact." + entry.getKey(), entry.getValue()));
        }
        if (stats.otherResistance() != null) {
            tooltip.add(line("tooltip.hbm.damage.other", stats.otherResistance()));
        }
    }

    private static Component line(String labelKey, DamageResistance resistance) {
        return Component.translatable("tooltip.hbm.damage.line",
                Component.translatable(labelKey),
                format(resistance.threshold()),
                (int) (resistance.resistance() * 100.0F)).withStyle(ChatFormatting.GRAY);
    }

    private static String format(float value) {
        if (value == (int) value) {
            return Integer.toString((int) value);
        }
        return Float.toString(value);
    }

    private DamageResistanceTooltipUtil() {
    }
}
