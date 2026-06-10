package com.hbm.ntm.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class AudioWrapper {
    public static AudioWrapper create(Level level, ResourceLocation sound) {
        if (level != null && level.isClientSide()) {
            return DistExecutor.unsafeRunForDist(
                    () -> () -> com.hbm.ntm.client.sound.ClientAudioWrapper.create(sound),
                    () -> AudioWrapper::new);
        }
        return new AudioWrapper();
    }

    public static AudioWrapper create(Level level, String sound) {
        return create(level, LegacySoundIds.resolveLocation(sound));
    }

    public static AudioWrapper getLoopedSound(Level level, String sound, double x, double y, double z,
            float volume, float range, float pitch) {
        return getLoopedSound(level, LegacySoundIds.resolveLocation(sound), x, y, z, volume, range, pitch);
    }

    public static AudioWrapper getLoopedSound(Level level, ResourceLocation sound, double x, double y, double z,
            float volume, float range, float pitch) {
        AudioWrapper audio = create(level, sound);
        audio.updatePosition(x, y, z);
        audio.updateVolume(volume);
        audio.updateRange(range);
        // Legacy ClientProxy accepted pitch here but left initial pitch at the sound default.
        return audio;
    }

    public static AudioWrapper getLoopedSound(Level level, String sound, double x, double y, double z,
            float volume, float range, float pitch, int keepAlive) {
        return getLoopedSound(level, LegacySoundIds.resolveLocation(sound), x, y, z, volume, range, pitch, keepAlive);
    }

    public static AudioWrapper getLoopedSound(Level level, ResourceLocation sound, double x, double y, double z,
            float volume, float range, float pitch, int keepAlive) {
        AudioWrapper audio = getLoopedSound(level, sound, x, y, z, volume, range, pitch);
        audio.setKeepAlive(keepAlive);
        return audio;
    }

    public static AudioWrapper getLoopedEntitySound(Level level, String sound, Entity entity,
            float volume, float range, float pitch) {
        return getLoopedEntitySound(level, LegacySoundIds.resolveLocation(sound), entity, volume, range, pitch);
    }

    public static AudioWrapper getLoopedEntitySound(Level level, ResourceLocation sound, Entity entity,
            float volume, float range, float pitch) {
        if (entity == null) {
            return new AudioWrapper();
        }
        AudioWrapper audio = getLoopedSound(level, sound, entity.getX(), entity.getY(), entity.getZ(), volume, range, pitch);
        audio.attachTo(entity);
        return audio;
    }

    public static AudioWrapper getLoopedEntitySound(Level level, String sound, Entity entity,
            float volume, float range, float pitch, int keepAlive) {
        return getLoopedEntitySound(level, LegacySoundIds.resolveLocation(sound), entity, volume, range, pitch, keepAlive);
    }

    public static AudioWrapper getLoopedEntitySound(Level level, ResourceLocation sound, Entity entity,
            float volume, float range, float pitch, int keepAlive) {
        if (entity == null) {
            return new AudioWrapper();
        }
        AudioWrapper audio = getLoopedSound(level, sound, entity.getX(), entity.getY(), entity.getZ(), volume, range, pitch, keepAlive);
        audio.attachTo(entity);
        return audio;
    }

    public void setKeepAlive(int keepAlive) {
    }

    public void keepAlive() {
    }

    public void updatePosition(double x, double y, double z) {
    }

    public void attachTo(Entity entity) {
    }

    public void updateVolume(float volume) {
    }

    public void updateRange(float range) {
    }

    public void updatePitch(float pitch) {
    }

    public float getVolume() {
        return 0.0F;
    }

    public float getRange() {
        return 0.0F;
    }

    public float getPitch() {
        return 0.0F;
    }

    public void setDoesRepeat(boolean repeats) {
    }

    public void startSound() {
    }

    public void stopSound() {
    }

    public boolean isPlaying() {
        return false;
    }
}
