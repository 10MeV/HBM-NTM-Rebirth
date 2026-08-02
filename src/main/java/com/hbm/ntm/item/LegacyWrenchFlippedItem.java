package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** 1.7.10 {@code WeaponSpecial(tMatElec)} with its only item-specific modifier. */
public final class LegacyWrenchFlippedItem extends HbmAbilitySwordItem {
    public LegacyWrenchFlippedItem(Item.Properties properties) {
        super(HbmToolTiers.ELEC, 16.0F, -0.1D, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Wrench 2: The Wrenchening"));
    }
}
