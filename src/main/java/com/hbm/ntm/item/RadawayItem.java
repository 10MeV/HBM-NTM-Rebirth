package com.hbm.ntm.item;

import com.hbm.ntm.config.PotionConfig;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RadawayItem extends Item {
    private final int duration;
    private final int amplifier;

    public RadawayItem(Properties properties, int duration, int amplifier) {
        super(properties);
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (PotionConfig.hasPotionSickness(player)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            PotionConfig.applyPotionSickness(player, 5);
            MobEffect effect = ModEffects.RADAWAY.get();
            MobEffectInstance active = player.getEffect(effect);
            int appliedDuration = active == null ? duration : active.getDuration() + duration;
            player.addEffect(new MobEffectInstance(effect, appliedDuration, amplifier));
            level.playSound(null, player.blockPosition(), ModSounds.TOOL_RADAWAY.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
