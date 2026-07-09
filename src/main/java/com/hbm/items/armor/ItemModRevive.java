package com.hbm.items.armor;

import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Legacy package facade for the 1.7.10 revive armor module item.
 */
@Deprecated(forRemoval = false)
public class ItemModRevive extends ArmorModItems.Revive {
    public ItemModRevive(int durability) {
        super(new Item.Properties(), durability);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.is(ModItems.SCRUMPY.get())) {
            tooltip.add(Component.literal("But how did you survive?").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("I was drunk.").withStyle(ChatFormatting.RED));
        }
        if (stack.is(ModItems.WILD_P.get())) {
            tooltip.add(Component.literal("Explosive ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("Reactive ").withStyle(ChatFormatting.RED))
                    .append(Component.literal("Plot ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("Armor").withStyle(ChatFormatting.RED)));
        }
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
