package com.hbm.ntm.client.sound;

import com.hbm.ntm.sound.AudioWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientAudioWrapper extends AudioWrapper {
    private final HbmDynamicSound sound;

    public static AudioWrapper create(ResourceLocation source) {
        return source == null ? new AudioWrapper() : new ClientAudioWrapper(source);
    }

    private ClientAudioWrapper(ResourceLocation source) {
        this.sound = new HbmDynamicSound(source);
    }

    @Override
    public void setKeepAlive(int keepAlive) {
        sound.setKeepAlive(keepAlive);
    }

    @Override
    public void keepAlive() {
        sound.keepAlive();
    }

    @Override
    public void updatePosition(double x, double y, double z) {
        sound.setPosition(x, y, z);
    }

    @Override
    public void attachTo(Entity entity) {
        sound.attachTo(entity);
    }

    @Override
    public void updateVolume(float volume) {
        sound.setVolume(volume);
    }

    @Override
    public void updateRange(float range) {
        sound.setRange(range);
    }

    @Override
    public void updatePitch(float pitch) {
        sound.setPitch(pitch);
    }

    @Override
    public float getVolume() {
        return sound.getMaxVolume();
    }

    @Override
    public float getRange() {
        return sound.getRange();
    }

    @Override
    public float getPitch() {
        return sound.getPitch();
    }

    @Override
    public void setDoesRepeat(boolean repeats) {
        sound.setLooping(repeats);
    }

    @Override
    public void startSound() {
        sound.start();
    }

    @Override
    public void stopSound() {
        sound.requestStop();
    }

    @Override
    public boolean isPlaying() {
        return sound.isPlaying();
    }
}
