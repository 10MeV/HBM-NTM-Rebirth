package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrillbitItem extends Item {
    private final Type type;

    public DrillbitItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type type() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Speed: " + (int) (type.speed() * 100.0D) + "%").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Tier: " + type.tier()).withStyle(ChatFormatting.YELLOW));
        if (type.fortune() > 0) {
            tooltip.add(Component.literal("Fortune " + type.fortune()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (type.vein()) {
            tooltip.add(Component.literal("Vein miner").withStyle(ChatFormatting.GREEN));
        }
        if (type.silk()) {
            tooltip.add(Component.literal("Silk touch").withStyle(ChatFormatting.GREEN));
        }
    }

    public enum Type {
        STEEL(1.0D, 1, 0, false, false),
        STEEL_DIAMOND(1.0D, 1, 2, false, true),
        HSS(1.2D, 2, 0, true, false),
        HSS_DIAMOND(1.2D, 2, 3, true, true),
        DESH(1.5D, 3, 1, true, true),
        DESH_DIAMOND(1.5D, 3, 4, true, true),
        TCALLOY(2.0D, 4, 1, true, true),
        TCALLOY_DIAMOND(2.0D, 4, 4, true, true),
        FERRO(2.5D, 5, 1, true, true),
        FERRO_DIAMOND(2.5D, 5, 4, true, true);

        private final double speed;
        private final int tier;
        private final int fortune;
        private final boolean vein;
        private final boolean silk;

        Type(double speed, int tier, int fortune, boolean vein, boolean silk) {
            this.speed = speed;
            this.tier = tier;
            this.fortune = fortune;
            this.vein = vein;
            this.silk = silk;
        }

        public double speed() {
            return speed;
        }

        public int tier() {
            return tier;
        }

        public int fortune() {
            return fortune;
        }

        public boolean vein() {
            return vein;
        }

        public boolean silk() {
            return silk;
        }
    }
}
