package com.hbm.ntm.api.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RadarScreenTooltipProfile {
    public static final String TOGGLE_GUI_KEY = "radar.toggleGui";
    public static final String CLEAR_MAP_KEY = "radar.clearMap";
    private static final String ENABLED_KEY = "container.hbm_ntm_rebirth.radar.enabled";
    private static final String DISABLED_KEY = "container.hbm_ntm_rebirth.radar.disabled";

    private RadarScreenTooltipProfile() {
    }

    public static List<Component> energy(long power, long maxPower, int redstonePower) {
        return List.of(
                Component.literal(power + " / " + maxPower + " HE"),
                Component.literal("Redstone: " + redstonePower).withStyle(ChatFormatting.RED));
    }

    public static List<Component> localizedLines(String key) {
        return new ArrayList<>(Arrays.stream(Component.translatable(key).getString().split("\\$"))
                .map(Component::literal)
                .toList());
    }

    public static List<Component> control(String key, boolean active) {
        List<Component> tooltip = localizedLines(key);
        tooltip.add(Component.translatable(active ? ENABLED_KEY : DISABLED_KEY)
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
        return tooltip;
    }

    public static List<Component> entry(Component name, BlockPos pos) {
        return List.of(
                name,
                Component.literal(pos.getX() + " / " + pos.getZ()),
                Component.literal("Alt.: " + pos.getY()));
    }

    public static List<Component> target(int x, int z) {
        return List.of(Component.literal(x + " / " + z));
    }

    public static List<FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }
}
