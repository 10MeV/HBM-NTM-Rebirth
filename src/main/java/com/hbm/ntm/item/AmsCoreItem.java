package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class AmsCoreItem extends Item {
    private final long powerBase;
    private final int heatBase;
    private final int fuelBase;
    private final int dfcMultiplier;

    public AmsCoreItem(Properties properties, long powerBase, int heatBase, int fuelBase, int dfcMultiplier) {
        super(properties);
        this.powerBase = powerBase;
        this.heatBase = heatBase;
        this.fuelBase = fuelBase;
        this.dfcMultiplier = dfcMultiplier;
    }

    public long powerBase() {
        return powerBase;
    }

    public int heatBase() {
        return heatBase;
    }

    public int fuelBase() {
        return fuelBase;
    }

    public int dfcMultiplier() {
        return dfcMultiplier;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return dfcMultiplier >= 2500 ? Rarity.EPIC : Rarity.UNCOMMON;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("DFC multiplier: " + dfcMultiplier).withStyle(ChatFormatting.GRAY));
    }
}
