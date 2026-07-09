package com.hbm.ntm.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Source-backed 1.7.10 ItemNuclearWaste drop contract.
 *
 * <p>Legacy waste items replaced vanilla dropped items with an invulnerable
 * EntityItemWaste and returned Integer.MAX_VALUE from getEntityLifespan. Forge
 * 1.20 already lets item classes supply both the lifespan and damage policy for
 * ItemEntity, so this keeps the same gameplay contract without a second entity
 * type.
 */
public class LegacyNuclearWasteItem extends Item {
    public LegacyNuclearWasteItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeHurtBy(DamageSource damageSource) {
        return false;
    }
}
