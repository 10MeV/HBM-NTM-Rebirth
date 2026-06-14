package com.hbm.ntm.item;

import com.hbm.ntm.player.HbmLivingProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SioxItem extends LegacyPillItem {
    public SioxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyPillEffects(ItemStack stack, Level level, Player player) {
        HbmLivingProperties.clearAsbestos(player);
        HbmLivingProperties.capBlackLung(player, HbmLivingProperties.MAX_BLACK_LUNG / 5);
    }
}
