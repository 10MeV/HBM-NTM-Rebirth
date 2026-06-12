package com.hbm.ntm.api.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

    public static Optional<Tooltip> mainTarget(List<RadarEntry> entries, BlockPos radarPos, int range,
            int leftPos, int topPos, int mouseX, int mouseY, Function<String, Component> nameResolver) {
        RadarGuiTargetProfile.Target entryTarget = RadarGuiTargetProfile.hoveredEntry(entries,
                radarPos, range, leftPos, topPos, mouseX, mouseY);
        if (entryTarget != null && entryTarget.hasEntry()) {
            RadarEntry entry = entryTarget.entry();
            Component name = nameResolver != null ? nameResolver.apply(entry.name()) : Component.literal(entry.name());
            return Optional.of(new Tooltip(entry(name, entry.pos()), entryTarget.screenX(), entryTarget.screenZ()));
        }

        if (RadarGuiHitProfile.hitsRadarArea(leftPos, topPos, mouseX, mouseY)) {
            RadarGuiTargetProfile.Target target = RadarGuiTargetProfile.positionTarget(radarPos,
                    range, leftPos, topPos, mouseX, mouseY);
            return Optional.of(new Tooltip(target(target.x(), target.z()), mouseX, mouseY));
        }
        return Optional.empty();
    }

    public static List<Component> chrome(RadarScreenHoverProfile.Hover hover, boolean includeControlState,
            boolean active, long power, long maxPower, int redstonePower) {
        return switch (hover.type()) {
            case ENERGY -> energy(power, maxPower, redstonePower);
            case CONTROL -> includeControlState
                    ? control(hover.button().tooltipKey(), active)
                    : localizedLines(hover.button().tooltipKey());
            case TOGGLE_VIEW -> localizedLines(TOGGLE_GUI_KEY);
            case CLEAR_MAP -> localizedLines(CLEAR_MAP_KEY);
            case NONE -> Collections.emptyList();
        };
    }

    public static List<FormattedCharSequence> split(List<Component> tooltip) {
        return tooltip.stream().map(Component::getVisualOrderText).toList();
    }

    public record Tooltip(List<Component> lines, int x, int y) {
        public Tooltip {
            lines = List.copyOf(lines != null ? lines : List.of());
        }
    }
}
