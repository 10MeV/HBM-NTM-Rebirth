package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModEffects;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IodinePillItem extends LegacyPillItem {
    public IodinePillItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyPillEffects(ItemStack stack, Level level, Player player) {
        player.removeEffect(MobEffects.BLINDNESS);
        player.removeEffect(MobEffects.CONFUSION);
        player.removeEffect(MobEffects.DIG_SLOWDOWN);
        player.removeEffect(MobEffects.HUNGER);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.POISON);
        player.removeEffect(MobEffects.WEAKNESS);
        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(ModEffects.RADIATION.get());
    }
}
