package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * 1.7.10 {@code ItemBDCL}: an intentionally unpleasant 40-tick drink.  The
 * old item completes itself at one tick remaining rather than using vanilla's
 * normal food completion path, so that contract is kept here explicitly.
 */
public class LegacyBdclItem extends Item {
    private static final int USE_DURATION = 40;

    public LegacyBdclItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            return;
        }

        // CommonForgeEvents applies ItemBDCL's old extra decrement at 24, 20,
        // 16, 12, 8 and 4 ticks.  The accelerated ticks reach this callback
        // one count lower, so preserve the source gulp sequence (40..10).
        boolean legacyGulpTick = remainingUseDuration % 5 == 0 && remainingUseDuration >= 10;
        boolean acceleratedLegacyGulpTick = remainingUseDuration >= 9
                && remainingUseDuration <= 19
                && remainingUseDuration % 5 == 4;
        if (legacyGulpTick || acceleratedLegacyGulpTick) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.PLAYER_GULP.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        if (remainingUseDuration == 1) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.stopUsingItem();
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.PLAYER_GROAN.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
}
