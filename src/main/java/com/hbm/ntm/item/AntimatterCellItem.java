package com.hbm.ntm.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class AntimatterCellItem extends Item {
    private final String descriptionPrefix;

    public AntimatterCellItem(Properties properties, String descriptionPrefix) {
        super(properties);
        this.descriptionPrefix = descriptionPrefix;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(descriptionPrefix + ".desc.1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(descriptionPrefix + ".desc.2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.trait.drop").withStyle(ChatFormatting.RED));
    }
}
