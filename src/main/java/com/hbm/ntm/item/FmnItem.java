package com.hbm.ntm.item;

import com.hbm.ntm.player.HbmLivingProperties;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FmnItem extends LegacyPillItem {
    public FmnItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyPillEffects(ItemStack stack, Level level, Player player) {
        HbmLivingProperties.capDigamma(player, 2.0F);
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
    }
}
