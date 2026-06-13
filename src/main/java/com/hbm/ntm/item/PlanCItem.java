package com.hbm.ntm.item;

import com.hbm.ntm.radiation.ModDamageSources;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PlanCItem extends LegacyPillItem {
    public PlanCItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void applyPillEffects(ItemStack stack, Level level, Player player) {
        for (int i = 0; i < 10; i++) {
            player.hurt(ModDamageSources.source(level,
                    level.random.nextBoolean() ? ModDamageSources.EUTHANIZED_SELF : ModDamageSources.EUTHANIZED_SELF2),
                    1000.0F);
        }
    }
}
