package com.hbm.ntm.item;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaMagazineConfigs;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaMagazineConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.sound.LegacySoundIds;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TauCannonItem extends SednaGunItem {
    private static final int CONFIG_INDEX = 0;
    private static final String KEY_CHARGE_TIMER = "tau_charge_timer";
    private static final String KEY_CHARGING = "tau_charging";
    private static final String KEY_PRIMARY_FIRING = "tau_primary_firing";

    public TauCannonItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties, gunConfig);
    }

    @Override
    public void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        if (keybind == HbmKeybind.GUN_PRIMARY) {
            handlePrimary(player, stack, pressed);
            return;
        }
        if (keybind == HbmKeybind.GUN_SECONDARY) {
            handleSecondary(player, stack, pressed);
            return;
        }
        super.handleKeybind(player, stack, keybind, pressed);
    }

    private void handlePrimary(ServerPlayer player, ItemStack stack, boolean pressed) {
        if (!handleEdgeKey(stack, "mouse1_", CONFIG_INDEX, pressed)) {
            return;
        }
        if (pressed) {
            primaryParts(stack).ifPresent(gun -> {
                if (gunState(stack, CONFIG_INDEX) == SednaGunConfig.GunState.IDLE
                        && getLoadedRound(player, stack, gun.magazine()).isPresent()) {
                    stack.getOrCreateTag().putBoolean(KEY_PRIMARY_FIRING, true);
                }
                clickPrimary(player, stack, gun);
            });
        } else if (stack.getOrCreateTag().getBoolean(KEY_PRIMARY_FIRING)) {
            stack.getOrCreateTag().putBoolean(KEY_PRIMARY_FIRING, false);
            playPrimaryReleaseSound(player);
        }
    }

    private void handleSecondary(ServerPlayer player, ItemStack stack, boolean pressed) {
        if (!handleEdgeKey(stack, "mouse2_", CONFIG_INDEX, pressed)) {
            return;
        }
        if (pressed) {
            beginSecondaryCharge(player, stack);
        } else {
            releaseSecondaryCharge(player, stack);
        }
    }

    private void beginSecondaryCharge(ServerPlayer player, ItemStack stack) {
        Optional<GunParts> parts = primaryParts(stack);
        if (parts.isEmpty() || findBeltAmmo(player, parts.get().magazine()).isEmpty()) {
            return;
        }
        stack.getOrCreateTag().putBoolean(KEY_CHARGING, true);
        stack.getOrCreateTag().putInt(KEY_CHARGE_TIMER, 0);
    }

    private void releaseSecondaryCharge(ServerPlayer player, ItemStack stack) {
        int timer = stack.getOrCreateTag().getInt(KEY_CHARGE_TIMER);
        boolean charging = stack.getOrCreateTag().getBoolean(KEY_CHARGING);
        stack.getOrCreateTag().putBoolean(KEY_CHARGING, false);
        if (!charging || timer < 10) {
            setGunState(stack, CONFIG_INDEX, SednaGunConfig.GunState.COOLDOWN);
            setTimer(stack, CONFIG_INDEX, 4);
            return;
        }

        Optional<GunParts> parts = primaryParts(stack);
        Optional<SednaMagazineConfig> chargeMagazine = LegacySednaMagazineConfigs.byKey("gun_tau.charge");
        Optional<RuntimeAmmo> chargeAmmo = chargeMagazine.flatMap(magazine -> findBeltAmmo(player, magazine));
        if (parts.isEmpty() || chargeAmmo.isEmpty()) {
            return;
        }

        GunParts gun = parts.get();
        SednaReceiverConfig receiver = gun.receiver();
        BulletConfig config = LegacySednaRuntimeBulletConfigs.TAU_URANIUM_CHARGE;
        int unitsUsed = 1 + Math.min(12, timer / 10);
        float wearDamageMultiplier = standardWearDamage(stack, gun.mode().durability(), CONFIG_INDEX);
        float damage = wearDamageMultiplier * unitsUsed * 5.0F;
        BulletProjectileEntity bullet = createBullet(player.level(), player, stack, gun, config, receiver, damage);
        if (bullet != null) {
            player.level().addFreshEntity(bullet);
        }
        addWearClamped(stack, CONFIG_INDEX, config.wear() * unitsUsed, gun.mode().durability());
        setGunState(stack, CONFIG_INDEX, SednaGunConfig.GunState.COOLDOWN);
        setTimer(stack, CONFIG_INDEX, receiver.delayAfterFire());
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.world.level.Level level,
            net.minecraft.world.entity.Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && !selected) {
            stack.getOrCreateTag().putBoolean(KEY_CHARGING, false);
            stack.getOrCreateTag().putBoolean(KEY_PRIMARY_FIRING, false);
            return;
        }
        if (!level.isClientSide && selected && stack.getOrCreateTag().getBoolean(KEY_CHARGING)) {
            stack.getOrCreateTag().putInt(KEY_CHARGE_TIMER, stack.getOrCreateTag().getInt(KEY_CHARGE_TIMER) + 1);
        }
    }

    @Override
    protected void playFireSound(net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player,
            SednaReceiverConfig receiver) {
        SoundEvent sound = LegacySoundIds.resolveEvent("GUN_TAU_FIRE");
        if (sound == null) {
            LegacySoundPlayer.playLegacyShotgunShoot(player, receiver.fireVolume(), receiver.firePitch());
            return;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS,
                receiver.fireVolume(), receiver.firePitch());
    }

    private void playPrimaryReleaseSound(ServerPlayer player) {
        SoundEvent sound = LegacySoundIds.resolveEvent("GUN_TAU_STOPFIRE");
        if (sound == null) {
            LegacySoundPlayer.playLegacyTauRelease(player);
            return;
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS,
                1.0F, 1.0F);
    }

    private float standardWearDamage(ItemStack stack, float durability, int index) {
        if (durability <= 0.0F) {
            return 1.0F;
        }
        float percent = wear(stack, index) / durability;
        if (percent < 0.75F) {
            return 1.0F;
        }
        return Math.max(0.0F, 1.0F - (percent - 0.75F) * 2.0F);
    }
}
