package com.hbm.ntm.radiation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class HazardTooltipUtil {
    public static void addHazardInformation(ItemStack stack, List<Component> tooltip) {
        if (stack.isEmpty()) {
            return;
        }

        for (HazardEntry entry : HazardRegistry.getHazards(stack)) {
            addHazardEntryInformation(stack, tooltip, entry);
        }

        double resistance = HazmatRegistry.getResistance(stack);
        if (resistance > 0.0D) {
            double blocked = (1.0D - Math.pow(10.0D, -resistance)) * 100.0D;
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.radiation.resistance", format(resistance), format(blocked))
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    private static void addHazardEntryInformation(ItemStack stack, List<Component> tooltip, HazardEntry entry) {
        float level = entry.modifiedLevel(stack, null);
        if (entry.type() == HazardType.RADIATION) {
            addRadiationInformation(stack, tooltip, level);
            return;
        }
        if (entry.type() == HazardType.DIGAMMA) {
            addDigammaInformation(stack, tooltip, level);
            return;
        }
        if (entry.type() == HazardType.HOT && level <= 0.0F) {
            return;
        }
        tooltip.add(Component.literal("[")
                .append(Component.translatable(traitKey(entry.type())))
                .append(Component.literal("]"))
                .withStyle(colorForHazard(entry.type())));
    }

    private static void addRadiationInformation(ItemStack stack, List<Component> tooltip, float radiation) {
        if (radiation < 1.0e-5F) {
            return;
        }
        tooltip.add(Component.literal("[")
                .append(Component.translatable("trait.radioactive"))
                .append(Component.literal("]"))
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal(formatLegacyRadiation(radiation) + "RAD/s")
                .withStyle(ChatFormatting.YELLOW));
        if (stack.getCount() > 1) {
            tooltip.add(Component.literal("Stack: " + formatLegacyRadiation(radiation * stack.getCount()) + "RAD/s")
                    .withStyle(ChatFormatting.YELLOW));
        }
    }

    private static void addDigammaInformation(ItemStack stack, List<Component> tooltip, float digamma) {
        tooltip.add(Component.literal("[")
                .append(Component.translatable("trait.digamma"))
                .append(Component.literal("]"))
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal(formatLegacyDigamma(digamma) + "mDRX/s")
                .withStyle(ChatFormatting.DARK_RED));
        if (stack.getCount() > 1) {
            tooltip.add(Component.literal("Stack: " + formatLegacyDigamma(digamma * stack.getCount()) + "mDRX/s")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    private static ChatFormatting colorForHazard(HazardType type) {
        return switch (type) {
            case HOT -> ChatFormatting.GOLD;
            case BLINDING -> ChatFormatting.DARK_AQUA;
            case ASBESTOS -> ChatFormatting.WHITE;
            case COAL -> ChatFormatting.DARK_GRAY;
            case HYDROACTIVE, EXPLOSIVE -> ChatFormatting.RED;
            default -> ChatFormatting.GOLD;
        };
    }

    private static String traitKey(HazardType type) {
        return switch (type) {
            case RADIATION -> "trait.radioactive";
            case DIGAMMA -> "trait.digamma";
            case HOT -> "trait.hot";
            case BLINDING -> "trait.blinding";
            case ASBESTOS -> "trait.asbestos";
            case COAL -> "trait.coal";
            case HYDROACTIVE -> "trait.hydro";
            case EXPLOSIVE -> "trait.explosive";
        };
    }

    private static String formatLegacyRadiation(float value) {
        return Double.toString(Math.floor(value * 1000.0D) / 1000.0D);
    }

    private static String formatLegacyDigamma(float value) {
        return Double.toString(Math.floor(value * 10000.0D) / 10.0D);
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
