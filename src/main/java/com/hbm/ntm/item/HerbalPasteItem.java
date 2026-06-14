package com.hbm.ntm.item;

import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HerbalPasteItem extends LegacyPillItem {
    public HerbalPasteItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyPillEffects(ItemStack stack, Level level, Player player) {
        HbmLivingProperties.clearAsbestos(player);
        HbmLivingProperties.capBlackLung(player, HbmLivingProperties.MAX_BLACK_LUNG / 5);
        HbmLivingProperties.reduceRadiation(player, 100.0F);

        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 10 * 20, 0));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10 * 60 * 20, 2));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10 * 60 * 20, 2));
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 5 * 20, 2));
        player.addEffect(new MobEffectInstance(ModEffects.POTION_SICKNESS.get(), 10 * 60 * 20, 0, false, true));
    }

    @Override
    protected int tooltipLineCount() {
        return 2;
    }
}
