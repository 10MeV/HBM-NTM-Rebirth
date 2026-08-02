package com.hbm.ntm.client;

import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.client.anim.LegacyBusAnimation;
import com.hbm.ntm.client.anim.LegacyBusAnimationKeyframe.IType;
import com.hbm.ntm.client.anim.LegacyBusAnimationSequence;
import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.sound.AudioWrapper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Client carrier for the exact 1.7.10 {@code XFactoryDrill.LAMBDA_DRILL_ANIMS}
 * and {@code Orchestras.ORCHESTRA_DRILL} contracts.  Animation state itself
 * remains owned by the shared {@link LegacyHbmAnimations} hotbar bus.
 */
public final class DrillGunItemClient {
    private static final int CYCLE = 3;
    private static final int CYCLE_DRY = 5;
    private static final int EQUIP = 9;
    private static final int INSPECT = 10;
    private static final Map<Integer, AudioWrapper> LOOPED_SOUNDS = new HashMap<>();

    public static void handleAnimation(ItemStack stack, int selectedSlot, short animationType, int receiverIndex,
            int itemIndex) {
        LegacyBusAnimation animation = switch (animationType) {
            case CYCLE -> cycleAnimation();
            case CYCLE_DRY -> dryCycleAnimation();
            case EQUIP -> equipAnimation();
            case INSPECT -> inspectAnimation();
            default -> null;
        };
        if (animation != null) {
            LegacyHbmAnimations.start(selectedSlot, 0, stack.getItem().getDescriptionId(), animation, false);
        }
    }

    public static void tick(ItemStack stack, Level level, Entity entity, boolean selected, int animationType) {
        if (entity == null) {
            return;
        }

        int entityId = entity.getId();
        if (!selected) {
            stopAndRemove(entityId);
            return;
        }

        AudioWrapper running = LOOPED_SOUNDS.get(entityId);
        if (running != null && !running.isPlaying()) {
            LOOPED_SOUNDS.remove(entityId, running);
            running.stopSound();
            running = null;
        }
        if (animationType != CYCLE && animationType != CYCLE_DRY) {
            stopAndRemove(entityId);
            return;
        }

        double speed = LegacyHbmAnimations.getRelevantTransformation("SPEED")[0];
        if (speed <= 0.0D) {
            // The legacy orchestra intentionally leaves this to the keep-alive timeout.
            return;
        }

        if (running == null || !running.isPlaying()) {
            boolean electric = SednaWeaponModEvaluator.hasUpgrade(stack, 0,
                    SednaWeaponModEvaluator.ID_ENGINE_ELECTRIC);
            AudioWrapper audio = AudioWrapper.getLoopedSound(level,
                    electric ? "TURBINE_LARGE_LOOP" : "ENGINE_LOOP",
                    entity.getX(), entity.getY(), entity.getZ(), (float) speed, 15.0F, (float) speed, 25);
            LOOPED_SOUNDS.put(entityId, audio);
            audio.startSound();
            audio.attachTo(entity);
            return;
        }

        running.keepAlive();
        running.updateVolume((float) speed);
        running.updatePitch((float) speed);
    }

    /**
     * Entity ids are only unique within a client level. Every stop path must
     * discard its map entry as well as stopping the facade, otherwise a later
     * level can retain or reuse the old client entity through the wrapper.
     */
    public static void clearRuntimeState() {
        for (AudioWrapper sound : LOOPED_SOUNDS.values()) {
            if (sound != null) {
                sound.stopSound();
            }
        }
        LOOPED_SOUNDS.clear();
    }

    private static void stopAndRemove(int entityId) {
        AudioWrapper sound = LOOPED_SOUNDS.remove(entityId);
        if (sound != null) {
            sound.stopSound();
        }
    }

    private static LegacyBusAnimation cycleAnimation() {
        double deploy = LegacyHbmAnimations.getRelevantTransformation("DEPLOY")[0];
        double speed = LegacyHbmAnimations.getRelevantTransformation("SPEED")[0];
        double spin = LegacyHbmAnimations.getRelevantTransformation("SPIN")[0] % 360.0D;
        int finishDuration = 750 + (int) (1_000.0D * (1.0D - spin / 360.0D));
        return new LegacyBusAnimation()
                .addBus("DEPLOY", new LegacyBusAnimationSequence()
                        .setPos(deploy, 0.0D, 0.0D)
                        .addPos(1.0D, 0.0D, 0.0D, (int) (500.0D * (1.0D - deploy)), IType.SIN_FULL)
                        .hold(1_000)
                        .addPos(0.0D, 0.0D, 0.0D, 500, IType.SIN_FULL))
                .addBus("SPIN", new LegacyBusAnimationSequence()
                        .setPos(spin, 0.0D, 0.0D)
                        .addPos(spin + 540.0D, 0.0D, 0.0D, 1_500)
                        .addPos(1_080.0D, 0.0D, 0.0D, finishDuration, IType.SIN_DOWN))
                .addBus("SPEED", new LegacyBusAnimationSequence()
                        .setPos(speed, 0.0D, 0.0D)
                        .addPos(1.0D, 0.0D, 0.0D, 500)
                        .hold(1_000)
                        .addPos(0.0D, 0.0D, 0.0D, finishDuration, IType.SIN_DOWN));
    }

    private static LegacyBusAnimation dryCycleAnimation() {
        return new LegacyBusAnimation()
                .addBus("DEPLOY", new LegacyBusAnimationSequence()
                        .addPos(0.25D, 0.0D, 0.0D, 250, IType.SIN_FULL)
                        .addPos(0.0D, 0.0D, 0.0D, 250, IType.SIN_FULL))
                .addBus("SPIN", new LegacyBusAnimationSequence()
                        .addPos(360.0D, 0.0D, 0.0D, 1_500, IType.SIN_DOWN))
                .addBus("SPEED", new LegacyBusAnimationSequence()
                        .addPos(0.75D, 0.0D, 0.0D, 250)
                        .addPos(0.0D, 0.0D, 0.0D, 1_000, IType.SIN_DOWN));
    }

    private static LegacyBusAnimation equipAnimation() {
        return new LegacyBusAnimation().addBus("EQUIP", new LegacyBusAnimationSequence()
                .setPos(-1.0D, 0.0D, 0.0D)
                .addPos(0.0D, 0.0D, 0.0D, 750, IType.SIN_DOWN));
    }

    private static LegacyBusAnimation inspectAnimation() {
        return new LegacyBusAnimation().addBus("LIFT", new LegacyBusAnimationSequence()
                .addPos(-45.0D, 0.0D, 0.0D, 500, IType.SIN_FULL)
                .hold(1_000)
                .addPos(0.0D, 0.0D, 0.0D, 500, IType.SIN_DOWN));
    }

    private DrillGunItemClient() {
    }
}
