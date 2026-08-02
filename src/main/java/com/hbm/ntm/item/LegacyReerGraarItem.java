package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Direct 1.7.10 {@code ModSword(tMatTitan)} migration for The Reer Graar. */
public final class LegacyReerGraarItem extends HbmAbilitySwordItem {
    public LegacyReerGraarItem(Item.Properties properties) {
        super(HbmToolTiers.TITANIUM, 6.5F, 0.0D, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Call now!"));
        tooltip.add(Component.literal("555-10-3728-ZX7-INFINITE"));
    }
}
