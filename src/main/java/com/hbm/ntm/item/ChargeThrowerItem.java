package com.hbm.ntm.item;

import com.hbm.ntm.bullet.BulletBehaviorTag;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmServerKeybinds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ChargeThrowerItem extends SednaGunItem {
    public static final String KEY_LAST_HOOK = "lasthook";

    public ChargeThrowerItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties, gunConfig);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide) {
            return;
        }
        if (gunState(stack, 0) == SednaGunConfig.GunState.RELOADING && getLastHook(stack) != -1) {
            setLastHook(stack, -1);
        }
        if (!selected || !(entity instanceof ServerPlayer player)) {
            if (getLastHook(stack) != -1) {
                setLastHook(stack, -1);
            }
            return;
        }
        applyHookMotion(stack, player);
    }

    @Override
    protected BulletProjectileEntity createBullet(Level level, Player player, ItemStack stack, GunParts gun,
            BulletConfig config, SednaReceiverConfig receiver, float overrideDamage) {
        BulletProjectileEntity bullet = super.createBullet(level, player, stack, gun, config, receiver, overrideDamage);
        if (bullet != null && config.hasBehavior(BulletBehaviorTag.CHARGE_HOOK_STICK)) {
            setLastHook(stack, bullet.getId());
            bullet.noPhysics = false;
        }
        return bullet;
    }

    private void applyHookMotion(ItemStack stack, ServerPlayer player) {
        Entity entity = player.level().getEntity(getLastHook(stack));
        if (!(entity instanceof BulletProjectileEntity bullet) || !bullet.isAlive()
                || bullet.config() == null
                || !bullet.config().hasBehavior(BulletBehaviorTag.CHARGE_HOOK_STICK)
                || !bullet.inGround()) {
            return;
        }

        Vec3 delta = bullet.position().subtract(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        double line = delta.length();
        if (HbmServerKeybinds.isPressed(player, HbmKeybind.GUN_PRIMARY)) {
            Vec3 pull = delta.normalize().scale(0.1D);
            player.setDeltaMovement(player.getDeltaMovement().add(pull.x, pull.y + 0.04D, pull.z));
            player.hurtMarked = true;
            if (line < 2.0D) {
                bullet.discard();
            }
        } else if (!HbmServerKeybinds.isPressed(player, HbmKeybind.GUN_SECONDARY)) {
            constrainToHookLength(player, bullet, line);
        } else {
            player.setDeltaMovement(player.getDeltaMovement().scale(0.5D));
            player.hurtMarked = true;
        }

        if (player.getDeltaMovement().y > -0.1D) {
            player.fallDistance = 0.0F;
        }
    }

    private void constrainToHookLength(ServerPlayer player, BulletProjectileEntity bullet, double line) {
        Vec3 motion = player.getDeltaMovement();
        Vec3 next = new Vec3(player.getX() + motion.x, player.getY() + player.getEyeHeight() + motion.y,
                player.getZ() + motion.z);
        Vec3 delta = bullet.position().subtract(next);
        if (delta.length() <= line) {
            return;
        }
        Vec3 newNext = bullet.position().subtract(delta.normalize().scale(line));
        Vec3 velocity = newNext.subtract(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        if (velocity.length() < 3.0D) {
            player.setDeltaMovement(velocity);
            player.hurtMarked = true;
        }
    }

    public static int getLastHook(ItemStack stack) {
        return stack.getTag() == null || !stack.getTag().contains(KEY_LAST_HOOK)
                ? -1
                : stack.getTag().getInt(KEY_LAST_HOOK);
    }

    public static void setLastHook(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(KEY_LAST_HOOK, value);
    }
}
