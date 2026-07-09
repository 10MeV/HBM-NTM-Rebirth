package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Legacy package facade for the 1.7.10 radiation-resistance cladding module.
 */
@Deprecated(forRemoval = false)
public class ItemModCladding extends ItemArmorMod {
    public double rad;

    public ItemModCladding(double rad) {
        super(ArmorModHandler.cladding, true, true, true, true);
        this.rad = rad;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("+" + rad + " rad-resistance").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ")
                .append(stack.getHoverName())
                .append(Component.literal(" (+" + rad + " radiation resistance)"))
                .withStyle(ChatFormatting.YELLOW));
    }
}
