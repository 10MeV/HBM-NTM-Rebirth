package com.hbm.ntm.item;

import com.hbm.ntm.recipe.PWRFuelRuntime;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PWRFuelItem extends Item {
    private final PWRFuelRuntime.Type type;

    public PWRFuelItem(Properties properties, PWRFuelRuntime.Type type) {
        super(properties);
        this.type = type;
    }

    public PWRFuelRuntime.Type type() {
        return type;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Heat per flux: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(type.heatEmission() + " TU").withStyle(ChatFormatting.RESET)));
        tooltip.add(Component.literal("Reaction function: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(type.curve().fuelLabel()).withStyle(ChatFormatting.RESET)));
        tooltip.add(Component.literal("Fuel type: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(type.curve().dangerLabel()).withStyle(ChatFormatting.YELLOW)));
    }
}
