package com.hbm.ntm.item;

import com.hbm.ntm.player.HbmLivingProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class XanaxItem extends LegacyPillItem {
    public XanaxItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyPillEffects(ItemStack stack, Level level, Player player) {
        HbmLivingProperties.reduceDigamma(player, 0.5F);
    }
}
