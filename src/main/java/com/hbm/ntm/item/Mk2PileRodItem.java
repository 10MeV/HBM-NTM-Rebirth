package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/** Direct 1.7.10 {@code ItemPileRodMK2} migration for the dynamic MK2 Pile. */
public final class Mk2PileRodItem extends Item {
    public static final String DEPLETION_KEY = "depletion";

    public Mk2PileRodItem(Properties properties) {
        // Damage value remains the seven legacy metadata variants; no vanilla use action mutates it.
        super(properties.stacksTo(1).durability(6));
    }

    public static RodType type(ItemStack stack) {
        int index = stack == null ? 0 : stack.getDamageValue();
        RodType[] values = RodType.values();
        return index >= 0 && index < values.length ? values[index] : RodType.RA226BE;
    }

    public static double depletion(ItemStack stack) {
        return stack != null && stack.hasTag() ? stack.getTag().getDouble(DEPLETION_KEY) : 0.0D;
    }

    public static void setDepletion(ItemStack stack, double value) {
        stack.getOrCreateTag().putDouble(DEPLETION_KEY, Math.max(0.0D, value));
    }

    public static double depletionPercent(ItemStack stack) {
        RodType type = type(stack);
        return type.life <= 0.0D ? 0.0D : depletion(stack) * 100.0D / type.life;
    }

    public static double lifetime(ItemStack stack) {
        return type(stack).life;
    }

    public static double reactivity(ItemStack stack, double incomingFlux) {
        RodType type = type(stack);
        return type.neutronSource + (type.reactionMultiplier > 0.0D
                ? squirt(incomingFlux) * type.reactionMultiplier : 0.0D);
    }

    public static double heatPerNeutron(ItemStack stack) {
        return type(stack).heatMultiplier;
    }

    public static ItemStack react(ItemStack stack, double producedNeutrons) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        RodType type = type(stack);
        if (type.life <= 0.0D) {
            return stack;
        }
        double total = depletion(stack) + producedNeutrons;
        if (total < type.life) {
            setDepletion(stack, total);
            return stack;
        }
        ItemStack output = stack.copy();
        output.setCount(1);
        output.setDamageValue(type.turnsInto);
        CompoundTag tag = output.getTag();
        if (tag != null) {
            tag.remove(DEPLETION_KEY);
            if (tag.isEmpty()) {
                output.setTag(null);
            }
        }
        return output;
    }

    private static double squirt(double value) {
        return Math.sqrt(value + 1.0D / ((value + 2.0D) * (value + 2.0D))) - 1.0D / (value + 2.0D);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return type(stack).life > 0.0D && depletion(stack) > 0.0D;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item.hbm_ntm_rebirth.pile_rod." + type(stack).serializedName;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        RodType type = type(stack);
        return type.life <= 0.0D ? 0 : Math.round(13.0F - Math.min(13.0F,
                (float) (13.0D * depletion(stack) / type.life)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
            TooltipFlag flag) {
        RodType type = type(stack);
        if (type.life > 0.0D) {
            tooltip.add(Component.literal("Lifetime: " + Math.round(type.life)));
            double depletion = depletionPercent(stack);
            if (depletion > 0.0D) {
                tooltip.add(Component.literal("Depletion: " + Math.round(depletion) + "%"));
            }
        }
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.pile_rod."
                + type.serializedName + ".desc").withStyle(ChatFormatting.YELLOW));
    }

    public enum RodType {
        RA226BE(1.0D), PO210BE(1.0D), ZR(0.0D, 0.0D, 0.0D, 2),
        NU(1.0D, 25_000.0D, 0.25D, 4), PU239(1.0D, 500.0D, 0.5D, 5),
        RGP(1.0D, 1_000.0D, 0.5D, 6), WASTE(1.0D, 0.0D, 1.5D, 6);

        private final double reactionMultiplier;
        private final double life;
        private final double heatMultiplier;
        private final double neutronSource;
        private final int turnsInto;
        private final String serializedName;

        RodType(double neutronSource) { this(0.0D, 0.0D, 0.0D, neutronSource, 0); }
        RodType(double reactionMultiplier, double life, double heatMultiplier, int turnsInto) {
            this(reactionMultiplier, life, heatMultiplier, 0.0D, turnsInto);
        }
        RodType(double reactionMultiplier, double life, double heatMultiplier, double neutronSource, int turnsInto) {
            this.reactionMultiplier = reactionMultiplier;
            this.life = life;
            this.heatMultiplier = heatMultiplier;
            this.neutronSource = neutronSource;
            this.turnsInto = turnsInto;
            this.serializedName = name().toLowerCase(Locale.ROOT);
        }
    }
}
