package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PistonSetItem extends Item {
    private final Type type;

    public PistonSetItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type type() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Fuel efficiency:").withStyle(ChatFormatting.YELLOW));
        for (FuelGrade grade : FuelGrade.values()) {
            tooltip.add(Component.literal("-" + grade.label() + ": ").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal((int) (type.efficiency(grade) * 100.0D) + "%")
                            .withStyle(ChatFormatting.RED)));
        }
    }

    public enum Type {
        STEEL(1.00D, 0.75D, 0.25D, 0.00D, 0.00D),
        DURA(0.50D, 1.00D, 0.90D, 0.50D, 0.00D),
        DESH(0.00D, 0.50D, 1.00D, 0.75D, 0.00D),
        STARMETAL(0.50D, 0.75D, 1.00D, 0.90D, 0.50D);

        private final double[] efficiency;

        Type(double... efficiency) {
            this.efficiency = efficiency;
        }

        public double efficiency(FuelGrade grade) {
            return grade.ordinal() < efficiency.length ? efficiency[grade.ordinal()] : 0.0D;
        }
    }

    public enum FuelGrade {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        AERO("Aero"),
        BALEFIRE("Balefire");

        private final String label;

        FuelGrade(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }
}
