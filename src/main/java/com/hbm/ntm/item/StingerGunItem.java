package com.hbm.ntm.item;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StingerGunItem extends SednaGunItem {
    private static final int CONFIG_INDEX = 0;
    private static final String KEY_PRIMARY = "mouse1_";
    private static final String KEY_SECONDARY = "mouse2_";
    private static final String KEY_LOCKING_ON = "lockingon";
    private static final String KEY_LOCKON_PROGRESS = "lockonprogress";
    private static final String KEY_LOCKON_TARGET = "lockontarget";
    private static final String KEY_LOCKED_ON = "lockedon";
    private static final double LOCKON_DISTANCE = 150.0D;
    private static final double LOCKON_ANGLE_DEGREES = 10.0D;
    private static final int LOCKON_TICKS = 60;

    public StingerGunItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties, gunConfig);
    }

    @Override
    public void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        if (keybind == HbmKeybind.GUN_PRIMARY) {
            handlePrimary(player, stack, pressed);
            return;
        }
        if (keybind == HbmKeybind.GUN_SECONDARY) {
            handleSecondary(stack, pressed);
            return;
        }
        super.handleKeybind(player, stack, keybind, pressed);
    }

    private void handlePrimary(ServerPlayer player, ItemStack stack, boolean pressed) {
        if (!handleEdgeKey(stack, KEY_PRIMARY, CONFIG_INDEX, pressed) || !pressed) {
            return;
        }
        primaryParts(stack).ifPresent(gun -> clickPrimary(player, stack, gun));
    }

    private void handleSecondary(ItemStack stack, boolean pressed) {
        if (handleEdgeKey(stack, KEY_SECONDARY, CONFIG_INDEX, pressed)) {
            setLockingOn(stack, pressed);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (!selected) {
            if (isLockingOn(stack)) {
                setLockingOn(stack, false);
            }
            resetLockon(stack);
            return;
        }

        Optional<GunParts> parts = primaryParts(stack);
        if (parts.isEmpty() || !isLockingOn(stack) || !isAiming(stack)
                || getLoadedRound(player, stack, parts.get().magazine()).isEmpty()) {
            resetLockon(stack);
            return;
        }

        LivingEntity target = findLockonTarget(player, LOCKON_DISTANCE, LOCKON_ANGLE_DEGREES);
        if (target == null) {
            if (!isLockedOn(stack)) {
                resetLockon(stack);
            }
            return;
        }

        if (!isLockedOn(stack) && target.getId() != lockonTargetId(stack)) {
            resetLockon(stack);
            setLockonTargetId(stack, target.getId());
        }
        progressLockon(player, stack);
    }

    @Override
    protected void clickPrimary(ServerPlayer player, ItemStack stack, GunParts gun) {
        SednaGunConfig.GunState state = gunState(stack, gun.mode().configIndex());
        if (state == SednaGunConfig.GunState.RELOADING) {
            super.clickPrimary(player, stack, gun);
            return;
        }
        if (state != SednaGunConfig.GunState.IDLE) {
            return;
        }
        LoadedRound round = getLoadedRound(player, stack, gun.magazine()).orElse(null);
        if (round != null && isLockedOn(stack)) {
            fire(player.level(), player, stack, gun, round);
            resetLockon(stack);
            setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.COOLDOWN);
            setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterFire());
        } else if (gun.receiver().doesDryFire()) {
            setGunState(stack, gun.mode().configIndex(), gun.receiver().refireAfterDry()
                    ? SednaGunConfig.GunState.COOLDOWN
                    : SednaGunConfig.GunState.DRAWING);
            setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterDryFire());
        }
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

    @Nullable
    static LivingEntity findLockonTarget(ServerPlayer player, double distance, double angleThresholdDegrees) {
        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = origin.add(look.scale(distance));
        AABB search = new AABB(origin, end).inflate(distance * Math.sin(Math.toRadians(angleThresholdDegrees)) + 1.0D,
                10.0D, distance * Math.sin(Math.toRadians(angleThresholdDegrees)) + 1.0D);
        LivingEntity closest = null;
        double closestAngle = 360.0D;
        for (Entity entity : player.level().getEntities(player, search,
                candidate -> candidate instanceof LivingEntity && candidate.isPickable() && candidate.isAlive())) {
            LivingEntity living = (LivingEntity) entity;
            Vec3 toEntity = living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D).subtract(origin);
            double length = toEntity.length();
            if (length <= 0.0D) {
                continue;
            }
            double dot = toEntity.normalize().dot(look.normalize());
            double angle = Math.toDegrees(Math.acos(Math.max(-1.0D, Math.min(1.0D, dot))));
            if (angle < closestAngle && angle < angleThresholdDegrees) {
                closestAngle = angle;
                closest = living;
            }
        }
        return closest;
    }

    private void progressLockon(ServerPlayer player, ItemStack stack) {
        int progress = lockonProgress(stack) + 1;
        setLockonProgress(stack, progress);
        if (progress >= LOCKON_TICKS && !isLockedOn(stack)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.ITEM_TECH_BLEEP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            setLockedOn(stack, true);
        }
    }

    private void resetLockon(ItemStack stack) {
        setLockonProgress(stack, 0);
        setLockedOn(stack, false);
    }

    private boolean isLockingOn(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_LOCKING_ON);
    }

    private void setLockingOn(ItemStack stack, boolean locking) {
        stack.getOrCreateTag().putBoolean(KEY_LOCKING_ON, locking);
    }

    private int lockonProgress(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_LOCKON_PROGRESS);
    }

    private void setLockonProgress(ItemStack stack, int progress) {
        stack.getOrCreateTag().putInt(KEY_LOCKON_PROGRESS, Math.max(0, progress));
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
