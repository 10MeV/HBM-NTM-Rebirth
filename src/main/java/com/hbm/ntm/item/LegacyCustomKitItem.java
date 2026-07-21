package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Exact modern carrier for the old {@code ItemKitCustom} one-shot package. */
public class LegacyCustomKitItem extends Item {
    public static final String COLOR_1_TAG = "color1";
    public static final String COLOR_2_TAG = "color2";

    public LegacyCustomKitItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);

        // 1.7.10 ignored addItemStackToInventory's return value: a full
        // inventory does not change this into a modern give-or-drop reward.
        for (ItemStack content : HbmItemStackUtil.readStacksFromNBT(stack)) {
            if (!content.isEmpty()) player.getInventory().add(content.copy());
        }
        stack.shrink(1);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_UNPACK.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        player.getInventory().setChanged();
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemStack[] contents = HbmItemStackUtil.readStacksFromNBT(stack);
        if (contents.length == 0) return;
        tooltip.add(Component.literal("Contains:"));
        for (ItemStack content : contents) {
            if (!content.isEmpty()) {
                tooltip.add(Component.literal("-").append(content.getHoverName())
                        .append(content.getCount() > 1 ? Component.literal(" x" + content.getCount()) : Component.empty()));
            }
        }
    }

    public static ItemStack create(String name, @Nullable String lore, int color1, int color2, ItemStack... contents) {
        ItemStack stack = new ItemStack(ModItems.KIT_CUSTOM.get());
        setColor(stack, color1, 1);
        setColor(stack, color2, 2);
        if (lore != null) HbmItemStackUtil.addTooltipToStack(stack, lore.split("\\$"));
        stack.setHoverName(Component.literal(name).withStyle(ChatFormatting.RESET));
        HbmItemStackUtil.addStacksToNBT(stack, contents);
        return stack;
    }

    public static void setColor(ItemStack stack, int color, int index) {
        if (stack == null || stack.isEmpty() || (index != 1 && index != 2)) return;
        stack.getOrCreateTag().putInt(index == 1 ? COLOR_1_TAG : COLOR_2_TAG, color);
    }

    public static int getColor(ItemStack stack, int index) {
        if (stack == null || stack.isEmpty() || !stack.hasTag() || (index != 1 && index != 2)) return 0;
        return stack.getTag().getInt(index == 1 ? COLOR_1_TAG : COLOR_2_TAG);
    }
}
