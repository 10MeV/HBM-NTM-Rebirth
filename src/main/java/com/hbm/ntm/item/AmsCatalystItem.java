package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AmsCatalystItem extends Item {
    private final int color;
    private final long powerAbs;
    private final float powerMod;
    private final float heatMod;
    private final float fuelMod;

    public AmsCatalystItem(Properties properties, int color) {
        this(properties, color, 0L, 1.0F, 1.0F, 1.0F);
    }

    public AmsCatalystItem(Properties properties, int color, long powerAbs, float powerMod, float heatMod, float fuelMod) {
        super(properties);
        this.color = color;
        this.powerAbs = powerAbs;
        this.powerMod = powerMod;
        this.heatMod = heatMod;
        this.fuelMod = fuelMod;
    }

    public int color() {
        return color;
    }

    public long powerAbs() {
        return powerAbs;
    }

    public float powerMod() {
        return powerMod;
    }

    public float heatMod() {
        return heatMod;
    }

    public float fuelMod() {
        return fuelMod;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Adds spice to the core."));
        tooltip.add(Component.literal("Look at all those colors!"));
    }
}
