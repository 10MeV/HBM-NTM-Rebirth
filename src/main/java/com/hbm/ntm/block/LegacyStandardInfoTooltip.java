package com.hbm.ntm.block;

import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class LegacyStandardInfoTooltip {
    private LegacyStandardInfoTooltip() {
    }

    public static void append(List<Component> tooltip, String id) {
        boolean hasShiftDown = DistExecutor.unsafeRunForDist(
                () -> () -> com.hbm.ntm.client.ClientTooltipState.hasShiftDown(),
                () -> () -> false);
        if (hasShiftDown) {
            appendDescriptionLines(tooltip, id);
            return;
        }
        tooltip.add(Component.literal("Hold <")
                .append(Component.literal("LSHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
                .append(Component.literal("> to display more info"))
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    public static void appendDirectDescription(List<Component> tooltip, String id) {
        appendDescriptionLines(tooltip, id);
    }

    private static void appendDescriptionLines(List<Component> tooltip, String id) {
        String key = "block.hbm_ntm_rebirth." + id + ".desc";
        Component.translatable(key).getString().lines()
                .flatMap(line -> Arrays.stream(line.split("\\$")))
                .filter(line -> !line.isBlank())
                .map(line -> Component.literal(line).withStyle(ChatFormatting.YELLOW))
                .forEach(tooltip::add);
    }
}
