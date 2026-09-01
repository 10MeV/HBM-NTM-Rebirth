package com.hbm.ntm.item;

import com.hbm.ntm.recipe.WatzFuelRuntime;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WatzPelletItem extends Item {
    private static final String LEGACY_YIELD_KEY = "yield";

    private final WatzFuelRuntime.Type type;
    private final boolean depleted;

    public WatzPelletItem(Properties properties, WatzFuelRuntime.Type type, boolean depleted) {
        super(properties);
        this.type = type;
        this.depleted = depleted;
    }

    public WatzFuelRuntime.Type type() {
        return type;
    }

    public boolean depleted() {
        return depleted;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (depleted) {
            return;
        }
        tooltip.add(Component.literal("Depletion: "
                + String.format(Locale.US, "%.1f", depletionFraction(stack) * 100.0D) + "%")
                .withStyle(ChatFormatting.GREEN));
        if (type.passive() > 0.0D) {
            tooltip.add(legacyValueLine("Base fission rate: ", Double.toString(type.passive())));
            tooltip.add(Component.literal("Self-igniting!").withStyle(ChatFormatting.RED));
        }
        if (type.heatEmission() > 0.0D) {
            tooltip.add(legacyValueLine("Heat per flux: ", type.heatEmission() + " TU"));
        }
        if (type.burnFunc() != null) {
            tooltip.add(legacyValueLine("Reaction function: ", type.burnFunc().fuelLabel()));
            tooltip.add(Component.empty()
                    .append(Component.literal("Fuel type: ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(type.burnFunc().dangerLabel())
                            .withStyle(type.burnFunc().dangerous() ? ChatFormatting.RED : ChatFormatting.YELLOW)));
        }
        if (type.heatDiv() != null) {
            tooltip.add(legacyValueLine("Thermal multiplier: ", type.heatDiv().fuelLabel() + " TU\u207b\u00b9"));
        }
        if (type.absorbFunc() != null) {
            tooltip.add(legacyValueLine("Flux capture: ", type.absorbFunc().fuelLabel()));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !depleted && depletionFraction(stack) > 0.0D;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) depletionFraction(stack) * 13.0F);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int green = (int) Math.round(255.0D - depletionFraction(stack) * 255.0D);
        return (255 - green) << 16 | green << 8;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!depleted) {
            setNBTDefaults(stack);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    public static double getEnrichment(ItemStack stack) {
        if (!(stack.getItem() instanceof WatzPelletItem pellet)) {
            return 0.0D;
        }
        return getYield(stack) / WatzFuelRuntime.INITIAL_YIELD;
    }

    private static double depletionFraction(ItemStack stack) {
        if (!(stack.getItem() instanceof WatzPelletItem pellet) || pellet.depleted()) {
            return 0.0D;
        }
        return 1.0D - getEnrichment(stack);
    }

    public static double getYield(ItemStack stack) {
        if (!stack.hasTag()) {
            setNBTDefaults(stack);
        }
        return stack.getOrCreateTag().getDouble(LEGACY_YIELD_KEY);
    }

    public static void setYield(ItemStack stack, double yield) {
        if (!stack.hasTag()) {
            setNBTDefaults(stack);
        }
        stack.getOrCreateTag().putDouble(LEGACY_YIELD_KEY, yield);
    }

    private static void setNBTDefaults(ItemStack stack) {
        if (stack.getItem() instanceof WatzPelletItem) {
            CompoundTag tag = new CompoundTag();
            tag.putDouble(LEGACY_YIELD_KEY, WatzFuelRuntime.INITIAL_YIELD);
            stack.setTag(tag);
        }
    }

    private static Component legacyValueLine(String label, String value) {
        return Component.empty()
                .append(Component.literal(label).withStyle(ChatFormatting.GOLD))
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }
}
