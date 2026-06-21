package com.hbm.ntm.item;

import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BedrockOreBaseItem extends Item {
    public BedrockOreBaseItem(Properties properties) {
        super(properties);
    }

    public static double getOreAmount(ItemStack stack, BedrockOreType type) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0.0D;
        }
        CompoundTag tag = stack.getTag();
        return tag == null ? 0.0D : tag.getDouble(type.suffix());
    }

    public static void setOreAmount(ItemStack stack, BedrockOreType type, double amount) {
        stack.getOrCreateTag().putDouble(type.suffix(), Math.max(0.0D, amount));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (BedrockOreType type : BedrockOreType.values()) {
            double amount = getOreAmount(stack, type);
            tooltip.add(Component.translatable(type.translationKey())
                    .append(Component.literal(": " + ((int) (amount * 100.0D)) / 100.0D))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
