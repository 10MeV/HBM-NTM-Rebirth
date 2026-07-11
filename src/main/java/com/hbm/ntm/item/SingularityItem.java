package com.hbm.ntm.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class SingularityItem extends Item {
    private final String legacyName;
    private final int tooltipLines;
    private final Supplier<? extends Item> craftingRemainder;

    public SingularityItem(Properties properties) {
        this(properties, "singularity", 3);
    }

    public SingularityItem(Properties properties, String legacyName) {
        this(properties, legacyName, 3);
    }

    public SingularityItem(Properties properties, String legacyName, int tooltipLines) {
        this(properties, legacyName, tooltipLines, null);
    }

    public SingularityItem(Properties properties, String legacyName, int tooltipLines,
            Supplier<? extends Item> craftingRemainder) {
        super(properties);
        this.legacyName = legacyName;
        this.tooltipLines = tooltipLines;
        this.craftingRemainder = craftingRemainder;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return craftingRemainder != null;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return craftingRemainder == null ? ItemStack.EMPTY : new ItemStack(craftingRemainder.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int line = 1; line <= tooltipLines; line++) {
            tooltip.add(Component.translatable("item.hbm_ntm_rebirth." + legacyName + ".desc." + line)
                    .withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.trait.drop").withStyle(ChatFormatting.RED));
    }
}
