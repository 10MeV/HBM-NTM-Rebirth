package com.hbm.ntm.item;

import com.hbm.ntm.config.PotionConfig;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.InventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class LegacySyringeItem extends Item {
    private final Kind kind;

    public LegacySyringeItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (kind == Kind.MKUNICORN) {
            return InteractionResultHolder.pass(stack);
        }
        if (!applyTo(level, stack, player, player)) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyTo(target.level(), stack, target, attacker);
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int lines = kind.tooltipLines;
        for (int i = 0; i < lines; i++) {
            tooltip.add(Component.translatable(getDescriptionId() + ".desc." + i));
        }
    }

    private boolean applyTo(Level level, ItemStack stack, LivingEntity target, LivingEntity source) {
        if (level.isClientSide) {
            return true;
        }
        if (kind.blocksPotionSickness && PotionConfig.hasPotionSickness(target)) {
            return false;
        }

        switch (kind) {
            case STIMPAK -> {
                target.heal(5.0F);
                PotionConfig.applyPotionSickness(target, 5);
            }
            case MEDX -> {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4 * 60 * 20, 2));
                PotionConfig.applyPotionSickness(target, 5);
            }
            case PSYCHO -> {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2 * 60 * 20, 0));
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2 * 60 * 20, 0));
                PotionConfig.applyPotionSickness(target, 5);
            }
            case SUPER -> {
                target.heal(25.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 0));
                PotionConfig.applyPotionSickness(target, 15);
            }
            case TAINT -> {
                target.addEffect(new MobEffectInstance(ModEffects.TAINT.get(), 60 * 20, 0));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 20, 0));
            }
            case MKUNICORN -> HbmLivingProperties.applyMkuContagion(target);
        }

        stack.shrink(1);
        playSyringeSound(level, target);
        if (kind != Kind.MKUNICORN) {
            giveContainer(source, new ItemStack(ModItems.SYRINGE_METAL_EMPTY.get()));
        }
        if (kind == Kind.TAINT) {
            giveContainer(source, new ItemStack(ModItems.BOTTLE2_EMPTY.get()));
        }
        return true;
    }

    private static void playSyringeSound(Level level, LivingEntity target) {
        LegacySoundPlayer.playLegacySyringe(target);
    }

    private static void giveContainer(LivingEntity source, ItemStack stack) {
        if (source instanceof Player player) {
            InventoryUtil.giveOrDrop(player, stack);
        }
    }

    public enum Kind {
        STIMPAK(true, 1),
        MEDX(true, 1),
        PSYCHO(true, 2),
        SUPER(true, 2),
        TAINT(false, 3),
        MKUNICORN(false, 1);

        private final boolean blocksPotionSickness;
        private final int tooltipLines;

        Kind(boolean blocksPotionSickness, int tooltipLines) {
            this.blocksPotionSickness = blocksPotionSickness;
            this.tooltipLines = tooltipLines;
        }
    }
}
