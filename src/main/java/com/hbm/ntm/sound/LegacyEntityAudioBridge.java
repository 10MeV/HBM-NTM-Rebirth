package com.hbm.ntm.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Predicate;

public final class LegacyEntityAudioBridge {
    public static Object startLoop(Entity entity, String sound, String loopKey, float volume, float pitch) {
        return startLoop(entity, sound, loopKey, SoundSource.HOSTILE, volume, pitch);
    }

    public static Object startLoop(Entity entity, String sound, String loopKey, SoundSource source,
            float volume, float pitch) {
        return startLoop(entity, LegacySoundIds.resolveLocation(sound), loopKey, source, volume, pitch);
    }

    public static Object startLoop(Entity entity, ResourceLocation sound, String loopKey, float volume, float pitch) {
        return startLoop(entity, sound, loopKey, SoundSource.HOSTILE, volume, pitch);
    }

    public static Object startLoop(Entity entity, ResourceLocation sound, String loopKey, SoundSource source,
            float volume, float pitch) {
        return startLoop(entity, sound, loopKey, source, volume, pitch, Entity::isAlive);
    }

    public static Object startLoop(Entity entity, String sound, String loopKey, SoundSource source,
            float volume, float pitch, Predicate<Entity> keepPlaying) {
        return startLoop(entity, LegacySoundIds.resolveLocation(sound), loopKey, source, volume, pitch, keepPlaying);
    }

    public static Object startLoop(Entity entity, ResourceLocation sound, String loopKey, SoundSource source,
            float volume, float pitch, Predicate<Entity> keepPlaying) {
        if (entity == null || sound == null) {
            return null;
        }
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
                () -> () -> com.hbm.ntm.client.sound.LegacyMovingEntitySound.startForEntity(
                        sound, entity, loopKey, source, volume, pitch, keepPlaying));
    }

    public static void stopLoop(Entity entity, String loopKey) {
        if (entity == null) {
            return;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.hbm.ntm.client.sound.LegacyMovingEntitySound.stop(entity, loopKey));
    }

    public static void clearClientLoops() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.hbm.ntm.client.sound.LegacyMovingEntitySound.clearAll());
    }

    private LegacyEntityAudioBridge() {
    }
}
