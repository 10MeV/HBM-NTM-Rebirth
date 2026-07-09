package com.hbm.items.weapon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ItemGenericGrenade extends ItemGrenade {
    public ItemGenericGrenade(int fuse) {
        super(fuse);
    }

    public ItemGenericGrenade(int fuse, Item.Properties properties) {
        super(fuse, properties);
    }

    public void explode(Entity grenade, LivingEntity thrower, Level level, double x, double y, double z) {
    }

    public void explode(Entity grenade, LivingEntity thrower, Level level, double x, double y, double z,
            ItemStack stack) {
        explode(grenade, thrower, level, x, y, z);
    }

    public int getMaxTimer() {
        return this.fuse * 20;
    }

    public int getMaxTimer(ItemStack stack) {
        return getMaxTimer();
    }

    public double getBounceMod() {
        return 0.5D;
    }

    public double getBounceMod(ItemStack stack) {
        return getBounceMod();
    }

    public double getThrowForce(ItemStack stack) {
        return 1.5D;
    }

    public void onGrenadeTick(Entity grenade, ItemStack stack, int timer) {
    }

    public boolean onGrenadeBlockHit(Entity grenade, BlockHitResult hit, ItemStack stack, int timer) {
        return false;
    }

    public boolean onGrenadeEntityHit(Entity grenade, EntityHitResult hit, ItemStack stack, int timer) {
        return false;
    }
}
