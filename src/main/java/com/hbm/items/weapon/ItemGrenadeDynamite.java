package com.hbm.items.weapon;

import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ItemGrenadeDynamite extends ItemGenericGrenade {
    public ItemGrenadeDynamite(int fuse) {
        super(fuse);
    }

    public ItemGrenadeDynamite(int fuse, Item.Properties properties) {
        super(fuse, properties);
    }

    @Override
    public void explode(Entity grenade, LivingEntity thrower, Level level, double x, double y, double z) {
        if (!level.isClientSide) {
            WeaponExplosionUtil.smooth(level, x, y, z, 5.0F, thrower, 15.0F, 1.0D, true).explode();
        }
    }
}
