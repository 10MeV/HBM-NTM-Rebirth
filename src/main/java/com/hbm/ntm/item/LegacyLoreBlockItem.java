package com.hbm.ntm.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class LegacyLoreBlockItem extends BlockItem {
    public LegacyLoreBlockItem(Block block, Properties properties) {
        super(block, properties);
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
