package com.hbm.ntm.item;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.sound.AudioWrapper;
import com.hbm.ntm.sound.LegacySoundIds;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TauCannonItem extends SednaGunItem {
    private static final int CONFIG_INDEX = 0;
    private static final String KEY_CHARGE_TIMER = "tau_charge_timer";
    private static final String KEY_CHARGING = "tau_charging";
    private static final String KEY_PRIMARY_FIRING = "tau_primary_firing";
    private static final Map<Integer, AudioWrapper> TAU_CHARGE_SOUNDS = new HashMap<>();

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
            // 1.7.10 XFactoryAccelerator.LAMBDA_TAU_PRIMARY_RELEASE only
            // emits the stop-fire sound while CYCLE is still the current animation.
            // An automatic dry-fire replaces it with CYCLE_DRY before the key is
            // released, which must stay silent.
            if (legacyAnimation(stack, CONFIG_INDEX) == LEGACY_ANIM_CYCLE) {
                playPrimaryReleaseSound(player);
            }
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
        // The 1.7.10 orchestra consumes the first belt unit at SPINUP timer 0,
        // before ItemGunBaseNT advances that animation timer. This specialized
        // tick runs after SednaGunItem's shared animation tick, so start one
        // tick behind and let the first specialized tick observe zero.
        stack.getOrCreateTag().putInt(KEY_CHARGE_TIMER, -1);
        playLegacyAnimation(stack, CONFIG_INDEX, LEGACY_ANIM_SPINUP);
    }

    private void releaseSecondaryCharge(ServerPlayer player, ItemStack stack) {
        int timer = stack.getOrCreateTag().getInt(KEY_CHARGE_TIMER);
        boolean charging = stack.getOrCreateTag().getBoolean(KEY_CHARGING);
        stack.getOrCreateTag().putBoolean(KEY_CHARGING, false);
        if (!charging || timer < 10 || legacyAnimation(stack, CONFIG_INDEX) != LEGACY_ANIM_SPINUP) {
            playLegacyAnimation(stack, CONFIG_INDEX, LEGACY_ANIM_CYCLE_DRY);
            return;
        }

        Optional<GunParts> parts = primaryParts(stack);
        if (parts.isEmpty()) {
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
        playLegacyAnimation(stack, CONFIG_INDEX, LEGACY_ANIM_ALT_CYCLE);
        playTauFireSound(player.level(), player, 0.7F);
        addWearClamped(stack, CONFIG_INDEX, config.wear() * unitsUsed, gun.mode().durability());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide) {
            tickTauChargeSound(stack, level, entity, selected);
            return;
        }
        if (!selected) {
            stack.getOrCreateTag().putBoolean(KEY_CHARGING, false);
            stack.getOrCreateTag().putBoolean(KEY_PRIMARY_FIRING, false);
            return;
        }
        if (entity instanceof ServerPlayer player && stack.getOrCreateTag().getBoolean(KEY_CHARGING)) {
            int timer = stack.getOrCreateTag().getInt(KEY_CHARGE_TIMER) + 1;
            stack.getOrCreateTag().putInt(KEY_CHARGE_TIMER, timer);
            tickSecondaryCharge(player, stack, timer);
        }
    }

    @Override
    protected void playFireSound(Level level, net.minecraft.world.entity.player.Player player,
            SednaReceiverConfig receiver) {
        playTauFireSound(level, player, 0.9F);
    }

    private void tickSecondaryCharge(ServerPlayer player, ItemStack stack, int timer) {
        Optional<GunParts> parts = primaryParts(stack);
        if (parts.isEmpty()) {
            endSecondaryChargeDry(stack);
            return;
        }
        if (timer % 10 == 0 && timer < 130) {
            Optional<RuntimeAmmo> ammo = findBeltAmmo(player, parts.get().magazine());
            if (ammo.isEmpty()) {
                endSecondaryChargeDry(stack);
                return;
            }
            consumeRound(player, stack, parts.get().magazine(), ammo.get().config());
        }
        if (timer > 200) {
            triggerTauOvercharge(player, stack, parts.get());
        }
    }

    private void endSecondaryChargeDry(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(KEY_CHARGING, false);
        playLegacyAnimation(stack, CONFIG_INDEX, LEGACY_ANIM_CYCLE_DRY);
    }

    private void triggerTauOvercharge(ServerPlayer player, ItemStack stack, GunParts gun) {
        endSecondaryChargeDry(stack);
        player.hurt(ModDamageSources.source(player.level(), ModDamageSources.TAU_BLAST), 1_000.0F);
        addWearClamped(stack, CONFIG_INDEX, 10_000.0F, gun.mode().durability());

        Vec3 eyePosition = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        LegacySoundPlayer.playLegacyUfoBlast(player.level(), eyePosition, 5.0F, 0.9F, 0.0F);
        LegacySoundPlayer.playLegacyFireworksBlast(player.level(), eyePosition, 5.0F, 0.5F);

        float yaw = player.level().random.nextFloat() * 180.0F;
        for (int i = 0; i < 3; i++) {
            ParticleUtil.spawnPlasmaBlast(player.level(), eyePosition.x, eyePosition.y, eyePosition.z,
                    1.0F, 0.8F, 0.5F, -60.0F + 60.0F * i, yaw, 2.0F, 100.0D);
        }
    }

    private void tickTauChargeSound(ItemStack stack, Level level, Entity entity, boolean selected) {
        AudioWrapper running = TAU_CHARGE_SOUNDS.get(entity.getId());
        int animation = legacyAnimation(stack, CONFIG_INDEX);
        int timer = legacyAnimationTimer(stack, CONFIG_INDEX);
        boolean charging = selected && animation == LEGACY_ANIM_SPINUP && timer < 300;
        if (!charging) {
            if (running != null && running.isPlaying()) {
                running.stopSound();
            }
            TAU_CHARGE_SOUNDS.remove(entity.getId());
            return;
        }
        if (running == null || !running.isPlaying()) {
            running = AudioWrapper.getLoopedEntitySound(level, "GUN_TAU_LOOP", entity, 1.0F, 15.0F, 0.75F, 10);
            TAU_CHARGE_SOUNDS.put(entity.getId(), running);
            running.startSound();
            running.attachTo(entity);
            running.updatePitch(0.75F);
        }
        if (running.isPlaying()) {
            running.keepAlive();
            running.attachTo(entity);
            running.updatePitch(0.75F + timer * 0.01F);
        }
    }

    private void playTauFireSound(Level level, net.minecraft.world.entity.player.Player player, float basePitch) {
        SoundEvent sound = LegacySoundIds.resolveEvent("GUN_TAU_FIRE");
        if (sound == null) {
            LegacySoundPlayer.playLegacyShotgunShoot(player, 0.5F, basePitch);
            return;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS,
                0.5F, basePitch + player.getRandom().nextFloat() * 0.2F);
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
