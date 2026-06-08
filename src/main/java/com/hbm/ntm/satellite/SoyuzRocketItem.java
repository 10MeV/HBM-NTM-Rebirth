package com.hbm.ntm.satellite;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoyuzRocketItem extends Item {
    public static final String TAG_SKIN = "skin";
    public static final int SKIN_COUNT = 3;

    public SoyuzRocketItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack stackForSkin(Item item, int skin) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TAG_SKIN, clampSkin(skin));
        return stack;
    }

    public static int getSkin(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() == null) {
            return 0;
        }
        return clampSkin(stack.getTag().getInt(TAG_SKIN));
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, Item item) {
        for (int skin = 0; skin < SKIN_COUNT; skin++) {
            output.accept(stackForSkin(item, skin));
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return switch (getSkin(stack)) {
            case 1 -> Rarity.RARE;
            case 2 -> Rarity.EPIC;
            default -> Rarity.UNCOMMON;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.missile_soyuz.skin")
                .append(": ")
                .append(Component.translatable("item.hbm_ntm_rebirth.missile_soyuz.skin." + getSkin(stack)))
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static int clampSkin(int skin) {
        return Math.max(0, Math.min(SKIN_COUNT - 1, skin));
    }
}
