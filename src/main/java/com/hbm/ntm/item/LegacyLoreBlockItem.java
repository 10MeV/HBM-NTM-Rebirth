package com.hbm.ntm.item;

import com.hbm.ntm.block.LegacyStandardInfoTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyLoreBlockItem extends BlockItem {
    public LegacyLoreBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        LegacyStandardInfoTooltip.appendDirectDescriptionIfNotHandled(tooltip, getDescriptionId() + ".desc");
    }
}
