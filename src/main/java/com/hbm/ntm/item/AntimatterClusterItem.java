package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class AntimatterClusterItem extends Item {
    public AntimatterClusterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.pellet_antimatter.desc.1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.pellet_antimatter.desc.2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.trait.drop").withStyle(ChatFormatting.RED));
    }
}
