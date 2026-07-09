package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StealthBoyItem extends Item {
    private static final int INVISIBILITY_DURATION_TICKS = 30 * 20;

    public StealthBoyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_DURATION_TICKS, 1, true, true));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_UNPACK.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        stack.shrink(1);
        player.getInventory().setChanged();
        return InteractionResultHolder.consume(stack);
    }
}
