package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SingularityItem extends Item {
    private final String legacyName;
    private final int tooltipLines;

    public SingularityItem(Properties properties) {
        this(properties, "singularity", 3);
    }

    public SingularityItem(Properties properties, String legacyName) {
        this(properties, legacyName, 3);
    }

    public SingularityItem(Properties properties, String legacyName, int tooltipLines) {
        super(properties);
        this.legacyName = legacyName;
        this.tooltipLines = tooltipLines;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int line = 1; line <= tooltipLines; line++) {
            tooltip.add(Component.translatable("item.hbm." + legacyName + ".desc." + line)
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.hbm.trait.drop").withStyle(ChatFormatting.RED));
    }
}
