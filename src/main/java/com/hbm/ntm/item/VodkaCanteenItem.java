package com.hbm.ntm.item;

import com.hbm.ntm.config.PotionConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class VodkaCanteenItem extends Item {
    public VodkaCanteenItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && stack.getDamageValue() > 0 && entity.tickCount % 20 == 0) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() != 0 || PotionConfig.hasPotionSickness(player)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            stack.setDamageValue(stack.getMaxDamage());
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 10 * 20, 0));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 2));
            PotionConfig.applyPotionSickness(player, 5);
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.canteen_vodka.desc.cooldown").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.canteen_vodka.desc.nausea").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.canteen_vodka.desc.strength").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("item.hbm_ntm_rebirth.canteen_vodka.desc.flavor").withStyle(ChatFormatting.GRAY));
    }
}
