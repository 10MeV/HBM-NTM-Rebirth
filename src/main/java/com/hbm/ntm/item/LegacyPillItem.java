package com.hbm.ntm.item;

import com.hbm.ntm.config.PotionConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public abstract class LegacyPillItem extends Item {
    protected LegacyPillItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (PotionConfig.hasPotionSickness(player)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            PotionConfig.applyPotionSickness(player, 5);
            applyPillEffects(stack, level, player);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int lines = tooltipLineCount();
        if (lines <= 1) {
            tooltip.add(Component.translatable(getDescriptionId() + ".desc"));
            return;
        }
        for (int i = 0; i < lines; i++) {
            tooltip.add(Component.translatable(getDescriptionId() + ".desc." + i));
        }
    }

    protected abstract void applyPillEffects(ItemStack stack, Level level, Player player);

    protected int tooltipLineCount() {
        return 1;
    }
}
