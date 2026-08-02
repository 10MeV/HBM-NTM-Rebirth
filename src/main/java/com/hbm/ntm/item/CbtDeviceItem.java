package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Source-backed legacy self-applied Bang syringe. */
public class CbtDeviceItem extends Item {
    public CbtDeviceItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(ModEffects.BANG.get(), 30, 0));
            stack.shrink(1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_VICE.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
