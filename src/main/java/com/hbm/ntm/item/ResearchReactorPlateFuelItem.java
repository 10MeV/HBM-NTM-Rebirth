package com.hbm.ntm.item;

import com.hbm.ntm.recipe.ResearchReactorFuelRuntime;
import com.hbm.ntm.recipe.ResearchReactorFuelRuntime.FuelSpec;
import com.hbm.ntm.util.HbmMathUtil;
import java.util.List;
import java.util.OptionalDouble;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Legacy {@code ItemPlateFuel} display contract backed by the shared research-reactor fuel runtime.
 */
public final class ResearchReactorPlateFuelItem extends Item {
    public ResearchReactorPlateFuelItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return displayFraction(stack).orElse(0.0D) > 0.0D;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float depletion = (float) displayFraction(stack).orElse(0.0D);
        return Math.round(13.0F - 13.0F * Mth.clamp(depletion, 0.0F, 1.0F));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float depletion = (float) displayFraction(stack).orElse(0.0D);
        return Mth.hsvToRgb((1.0F - Mth.clamp(depletion, 0.0F, 1.0F)) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        FuelSpec spec = ResearchReactorFuelRuntime.fuelFor(stack);
        if (spec == null) {
            return;
        }

        tooltip.add(Component.literal("[Research Reactor Plate Fuel]").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("   " + functionDescription(spec)).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("   Yield of " + HbmMathUtil.getShortNumber(spec.lifetime()) + " events")
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private static OptionalDouble displayFraction(ItemStack stack) {
        return ResearchReactorFuelRuntime.durabilityForDisplay(stack);
    }

    private static String functionDescription(FuelSpec spec) {
        return switch (spec.function()) {
            case LOGARITHM -> "f(x) = log10(x + 1) * 0.5 * " + spec.reactivity();
            case SQUARE_ROOT -> "f(x) = sqrt(x) * " + spec.reactivity() + " / 10";
            case NEGATIVE_QUADRATIC -> "f(x) = [x - (x² / 10000)] / 100 * " + spec.reactivity();
            case LINEAR -> "f(x) = x / 100 * " + spec.reactivity();
            case PASSIVE -> "f(x) = " + spec.reactivity();
        };
    }
}
