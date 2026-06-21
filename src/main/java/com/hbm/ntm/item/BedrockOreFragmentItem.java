package com.hbm.ntm.item;

import com.hbm.ntm.item.BedrockOreItem.BedrockOreType;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BedrockOreFragmentItem extends Item {
    private static final String TAG_TYPE = "type";

    public BedrockOreFragmentItem(Properties properties) {
        super(properties);
    }

    public static ItemStack make(BedrockOreType type, int amount) {
        ItemStack stack = new ItemStack(com.hbm.ntm.registry.ModItems.BEDROCK_ORE_FRAGMENT.get(), amount);
        stack.getOrCreateTag().putString(TAG_TYPE, (type == null ? BedrockOreType.LIGHT_METAL : type).suffix());
        return stack;
    }

    public static BedrockOreType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? BedrockOreType.LIGHT_METAL : BedrockOreType.bySuffix(tag.getString(TAG_TYPE));
    }

    public static int tint(ItemStack stack, int tintIndex) {
        return tintIndex == 0 ? getType(stack).lightColor() : 0xFFFFFF;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.hbm_ntm_rebirth.bedrock_ore_fragment",
                Component.translatable(getType(stack).translationKey()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getType(stack).translationKey()));
    }
}
