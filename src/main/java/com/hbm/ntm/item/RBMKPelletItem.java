package com.hbm.ntm.item;

import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.neutron.RBMKItemPlanner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RBMKPelletItem extends Item {
    private final RBMKFuelRodRegistry.Entry entry;

    public RBMKPelletItem(Properties properties, RBMKFuelRodRegistry.Entry entry) {
        super(properties);
        this.entry = entry;
    }

    public String getLegacyPelletId() {
        return entry.legacyPelletId();
    }

    public boolean isXenonEnabled() {
        return entry.pelletXenonOverlay();
    }

    public RBMKItemPlanner.PelletMetaPlan getMetaPlan(ItemStack stack) {
        return RBMKItemPlanner.pelletMeta(stack.getDamageValue(), entry.pelletXenonOverlay());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        RBMKItemPlanner.PelletTooltipPlan plan = RBMKItemPlanner.pelletTooltip(
                entry.fullName(), stack.getDamageValue(), entry.pelletXenonOverlay());
        for (RBMKItemPlanner.TooltipLine line : plan.lines()) {
            tooltip.add(applyStyle(component(line), line.style()));
        }
    }

    private static MutableComponent component(RBMKItemPlanner.TooltipLine line) {
        if (!line.literal().isEmpty()) {
            return Component.literal(line.literal());
        }
        if (!line.argumentTranslationKey().isEmpty()) {
            return Component.translatable(line.translationKey(), Component.translatable(line.argumentTranslationKey()));
        }
        if (!line.argument().isEmpty()) {
            return Component.translatable(line.translationKey(), line.argument());
        }
        return Component.translatable(line.translationKey());
    }

    private static MutableComponent applyStyle(MutableComponent component, RBMKItemPlanner.TooltipStyle style) {
        return switch (style) {
            case ITALIC -> component.withStyle(ChatFormatting.ITALIC);
            case DARK_GRAY_ITALIC -> component.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
            case GOLD -> component.withStyle(ChatFormatting.GOLD);
            case RED -> component.withStyle(ChatFormatting.RED);
            case GREEN -> component.withStyle(ChatFormatting.GREEN);
            case DARK_PURPLE -> component.withStyle(ChatFormatting.DARK_PURPLE);
            case BLUE -> component.withStyle(ChatFormatting.BLUE);
            case YELLOW -> component.withStyle(ChatFormatting.YELLOW);
            case DARK_RED -> component.withStyle(ChatFormatting.DARK_RED);
            case DARK_GREEN -> component.withStyle(ChatFormatting.DARK_GREEN);
            case DARK_GRAY -> component.withStyle(ChatFormatting.DARK_GRAY);
        };
    }
}
