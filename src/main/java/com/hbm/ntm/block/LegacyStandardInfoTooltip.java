package com.hbm.ntm.block;

import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class LegacyStandardInfoTooltip {
    private static final Component STANDARD_INFO_PROMPT = Component.literal("Hold <")
            .append(Component.literal("LSHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
            .append(Component.literal("> to display more info"))
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);

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
        tooltip.add(STANDARD_INFO_PROMPT.copy());
    }

    public static void appendDirectDescription(List<Component> tooltip, String id) {
        appendDescriptionLines(tooltip, id);
    }

    /**
     * Keeps old direct lore for block items that have no block-owned tooltip, while avoiding a second unstyled copy
     * when {@link net.minecraft.world.item.BlockItem} already forwarded to this helper through the block itself.
     */
    public static void appendDirectDescriptionIfNotHandled(List<Component> tooltip, String translationKey) {
        Component description = Component.translatable(translationKey);
        String resolved = description.getString();
        if (resolved.equals(translationKey)) {
            return;
        }

        List<String> lines = Arrays.asList(resolved.split("\\$"));
        List<String> meaningfulLines = resolved.lines()
                .flatMap(line -> Arrays.stream(line.split("\\$")))
                .filter(line -> !line.isBlank())
                .toList();
        if (meaningfulLines.isEmpty()
                || hasStandardInfoPrompt(tooltip)
                || containsDescriptionLines(tooltip, meaningfulLines)) {
            return;
        }
        lines.stream().map(Component::literal).forEach(tooltip::add);
    }

    private static boolean hasStandardInfoPrompt(List<Component> tooltip) {
        return tooltip.stream().anyMatch(STANDARD_INFO_PROMPT::equals);
    }

    private static boolean containsDescriptionLines(List<Component> tooltip, List<String> descriptionLines) {
        if (descriptionLines.size() > tooltip.size()) {
            return false;
        }
        for (int start = 0; start <= tooltip.size() - descriptionLines.size(); start++) {
            boolean matches = true;
            for (int offset = 0; offset < descriptionLines.size(); offset++) {
                if (!tooltip.get(start + offset).getString().equals(descriptionLines.get(offset))) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                return true;
            }
        }
        return false;
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
