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
