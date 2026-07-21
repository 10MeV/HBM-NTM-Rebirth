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

    /**
     * EntitySoyuzCapsule copied its watcher value directly into the landed
     * missile stack's metadata. Keep that raw persistent value separate from
     * ordinary item creation, whose user-facing skin contract is 0..2.
     */
    public static ItemStack stackForEntitySkin(Item item, int skin) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TAG_SKIN, skin);
        return stack;
    }

    public static int getSkin(ItemStack stack) {
        return clampSkin(getRawSkin(stack));
    }

    /**
     * Legacy ItemSoyuz used the raw item metadata for rarity, tooltip and
     * launcher payload state. Only getIconFromDamage clamped it for texture
     * lookup, so a recovered capsule's unvalidated watcher value must remain
     * observable here.
     */
    public static int getRawSkin(ItemStack stack) {
        if (stack.isEmpty() || stack.getTag() == null) {
            return 0;
        }
        return stack.getTag().getInt(TAG_SKIN);
    }

    public static boolean isValidSkin(int skin) {
        return skin >= 0 && skin < SKIN_COUNT;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, Item item) {
        for (int skin = 0; skin < SKIN_COUNT; skin++) {
            output.accept(stackForSkin(item, skin));
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return switch (getRawSkin(stack)) {
            case 1 -> Rarity.RARE;
            case 2 -> Rarity.EPIC;
            case 0 -> Rarity.UNCOMMON;
            default -> Rarity.COMMON;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Skin:"));
        int skin = getRawSkin(stack);
        if (isValidSkin(skin)) {
            tooltip.add(Component.translatable("item.hbm_ntm_rebirth.missile_soyuz.skin." + skin)
                    .withStyle(skinColor(skin)));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    private static ChatFormatting skinColor(int skin) {
        return switch (skin) {
            case 1 -> ChatFormatting.BLUE;
            case 2 -> ChatFormatting.GREEN;
            default -> ChatFormatting.GOLD;
        };
    }

    private static int clampSkin(int skin) {
        return Math.max(0, Math.min(SKIN_COUNT - 1, skin));
    }
}
