package com.hbm.ntm.api.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record LegacyLookOverlay(Component title, int titleColor, int titleShadowColor, List<Component> lines) {
    public static final int LEGACY_TITLE_COLOR = 0xFFFF00;
    public static final int LEGACY_TITLE_SHADOW_COLOR = 0x404000;

    public LegacyLookOverlay {
        lines = List.copyOf(lines == null ? List.of() : lines);
    }

    public static LegacyLookOverlay forBlock(BlockEntity blockEntity, List<Component> lines) {
        return forBlockState(blockEntity.getBlockState(), lines);
    }

    public static LegacyLookOverlay forBlockState(BlockState state, List<Component> lines) {
        return new LegacyLookOverlay(
                Component.translatable(state.getBlock().getDescriptionId()),
                LEGACY_TITLE_COLOR,
                LEGACY_TITLE_SHADOW_COLOR,
                lines);
    }

    public static LegacyLookOverlay forItem(ItemStack stack, List<Component> lines) {
        return withTitle(stack.getHoverName(), lines);
    }

    public static LegacyLookOverlay withTitle(Component title, List<Component> lines) {
        return new LegacyLookOverlay(title, LEGACY_TITLE_COLOR, LEGACY_TITLE_SHADOW_COLOR, lines);
    }

    public static LegacyLookOverlay withTitle(Component title, int titleColor, int titleShadowColor,
            List<Component> lines) {
        return new LegacyLookOverlay(title, titleColor, titleShadowColor, lines);
    }

    public static LegacyLookOverlay titleOnly(Component title) {
        return withTitle(title, List.of());
    }

    public static LegacyLookOverlay titleOnly(Component title, int titleColor, int titleShadowColor) {
        return withTitle(title, titleColor, titleShadowColor, List.of());
    }
}
