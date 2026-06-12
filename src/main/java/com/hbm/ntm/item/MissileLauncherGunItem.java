package com.hbm.ntm.item;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.network.HbmKeybind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MissileLauncherGunItem extends SednaGunItem {
    private static final int CONFIG_INDEX = 0;
    private static final String KEY_PRIMARY = "mouse1_";
    private static final String KEY_LOCKON_TARGET = "lockontarget";
    private static final String KEY_LOCKED_ON = "lockedon";
    private static final double LOCKON_DISTANCE = 150.0D;
    private static final double LOCKON_ANGLE_DEGREES = 20.0D;

    public MissileLauncherGunItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties, gunConfig);
    }

    @Override
    public void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        if (keybind == HbmKeybind.GUN_PRIMARY) {
            handlePrimary(player, stack, pressed);
            return;
        }
        super.handleKeybind(player, stack, keybind, pressed);
    }

    private void handlePrimary(ServerPlayer player, ItemStack stack, boolean pressed) {
        if (!handleEdgeKey(stack, KEY_PRIMARY, CONFIG_INDEX, pressed) || !pressed) {
            return;
        }
        if (isAiming(stack)) {
            LivingEntity target = StingerGunItem.findLockonTarget(player, LOCKON_DISTANCE, LOCKON_ANGLE_DEGREES);
            if (target != null) {
                setLockonTargetId(stack, target.getId());
                setLockedOn(stack, true);
            }
        }
        primaryParts(stack).ifPresent(gun -> clickPrimary(player, stack, gun));
        setLockedOn(stack, false);
    }

    @Override
    protected BulletProjectileEntity createBullet(Level level, Player player, ItemStack stack, GunParts gun,
            BulletConfig config, SednaReceiverConfig receiver, float overrideDamage) {
        BulletProjectileEntity bullet = super.createBullet(level, player, stack, gun, config, receiver, overrideDamage);
        if (bullet != null && level instanceof ServerLevel serverLevel && isLockedOn(stack)) {
            Entity target = serverLevel.getEntity(lockonTargetId(stack));
            if (target instanceof LivingEntity living && living.isAlive()) {
                bullet.setHomingTargetEntity(living);
            }
        }
        return bullet;
    }

    private int lockonTargetId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_LOCKON_TARGET);
    }

    private void setLockonTargetId(ItemStack stack, int targetId) {
        stack.getOrCreateTag().putInt(KEY_LOCKON_TARGET, targetId);
    }

    private boolean isLockedOn(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_LOCKED_ON);
    }

    private void setLockedOn(ItemStack stack, boolean locked) {
        stack.getOrCreateTag().putBoolean(KEY_LOCKED_ON, locked);
    }
}
