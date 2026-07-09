package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class HotDustedItem extends HotItem {
    private final int forgedCount;

    public HotDustedItem(Properties properties, int maxHeat, int forgedCount) {
        super(properties, maxHeat);
        this.forgedCount = Math.max(0, forgedCount);
    }

    @Override
    public int maxHeat(ItemStack stack) {
        return Math.max(1, super.maxHeat(stack) - forgedCount * 10);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hot_dusted.forged", forgedCount));
    }
}
