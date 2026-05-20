package com.hbm.ntm.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class EffectPillItem extends Item {
    private final RegistryObject<MobEffect> effect;
    private final int duration;
    private final int amplifier;
    private final RegistryObject<SoundEvent> sound;
    private final boolean showDescription;

    public EffectPillItem(Properties properties, RegistryObject<MobEffect> effect, int duration, int amplifier, RegistryObject<SoundEvent> sound) {
        this(properties, effect, duration, amplifier, sound, false);
    }

    public EffectPillItem(Properties properties, RegistryObject<MobEffect> effect, int duration, int amplifier,
            RegistryObject<SoundEvent> sound, boolean showDescription) {
        super(properties);
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
        this.sound = sound;
        this.showDescription = showDescription;
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
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(effect.get(), duration, amplifier));
            if (sound != null) {
                level.playSound(null, player.blockPosition(), sound.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (showDescription) {
            tooltip.add(Component.translatable(getDescriptionId() + ".desc"));
        }
    }
}
