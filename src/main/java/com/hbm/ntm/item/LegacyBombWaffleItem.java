package com.hbm.ntm.item;

import com.hbm.ntm.explosion.ExplosionNukeSmall;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LegacyBombWaffleItem extends Item {
    public LegacyBombWaffleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            ExplosionNukeSmall.explode(level, player.getX(), player.getY() + 0.5D, player.getZ(),
                    ExplosionNukeSmall.PARAMS_MEDIUM);
        }
        return result;
    }
}
