package com.hbm.ntm.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyLoreItem extends Item {
    public LegacyLoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String key = getDescriptionId() + ".desc";
        Component description = Component.translatable(key);
        if (!description.getString().equals(key)) {
            for (String line : description.getString().split("\\$")) {
                tooltip.add(Component.literal(line));
            }
        }
    }
}
