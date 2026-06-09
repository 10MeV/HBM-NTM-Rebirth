package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class PACoilItem extends Item {
    private final Type type;

    public PACoilItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type type() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Quadrupole operational range: ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(formatRange(type.quadMin(), type.quadMax())).withStyle(ChatFormatting.RESET)));
        tooltip.add(Component.literal("Dipole operational range: ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(formatRange(type.diMin(), type.diMax())).withStyle(ChatFormatting.RESET)));
        tooltip.add(Component.literal("Dipole minimum side length: ").withStyle(ChatFormatting.BLUE)
                .append(Component.literal(Integer.toString(type.diDistMin())).withStyle(ChatFormatting.RESET)));
        tooltip.add(Component.literal("Minimums not met result in a power draw penalty!").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Maximums exceeded result in the particle crashing!").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("Particles will crash in dipoles if both penalties take effect!").withStyle(ChatFormatting.RED));
    }

    private static String formatRange(int min, int max) {
        return String.format(Locale.US, "%,d - %,d", min, max);
    }

    public enum Type {
        GOLD(0, 2_200, 0, 2_200, 15),
        NIOBIUM(1_500, 8_400, 1_500, 8_400, 21),
        BSCCO(7_500, 15_000, 7_500, 15_000, 27),
        CHLOROPHYTE(14_500, 75_000, 14_500, 75_000, 51);

        private final int quadMin;
        private final int quadMax;
        private final int diMin;
        private final int diMax;
        private final int diDistMin;

        Type(int quadMin, int quadMax, int diMin, int diMax, int diDistMin) {
            this.quadMin = quadMin;
            this.quadMax = quadMax;
            this.diMin = diMin;
            this.diMax = diMax;
            this.diDistMin = diDistMin;
        }

        public int quadMin() {
            return quadMin;
        }

        public int quadMax() {
            return quadMax;
        }

        public int diMin() {
            return diMin;
        }

        public int diMax() {
            return diMax;
        }

        public int diDistMin() {
            return diDistMin;
        }
    }
}
