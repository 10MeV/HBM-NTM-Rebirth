package com.hbm.ntm.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HbmDynamicSound extends AbstractSoundInstance implements TickableSoundInstance {
    private float maxVolume = 1.0F;
    private float range = 10.0F;
    private int keepAlive;
    private int timeSinceKeepAlive;
    private boolean shouldExpire;
    private boolean stopped;
    private Entity parentEntity;

    public HbmDynamicSound(ResourceLocation location) {
        this(location, SoundSource.BLOCKS);
    }

    public HbmDynamicSound(ResourceLocation location, SoundSource source) {
        super(location, source, RandomSource.create());
        this.looping = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.volume = 1.0F;
        this.pitch = 1.0F;
    }

    @Override
    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (parentEntity != null && player != parentEntity) {
            setPosition(parentEntity.getX(), parentEntity.getY(), parentEntity.getZ());
        }

        if (player != null && player != parentEntity) {
            double distance = Math.sqrt(Math.pow(x - player.getX(), 2) + Math.pow(y - player.getY(), 2) + Math.pow(z - player.getZ(), 2));
            this.volume = volumeForDistance((float) distance);
        } else {
            if (player != null && player == parentEntity) {
                setPosition(parentEntity.getX(), parentEntity.getY() + 10.0D, parentEntity.getZ());
            }
            this.volume = maxVolume;
        }

        if (shouldExpire && timeSinceKeepAlive++ > keepAlive) {
            requestStop();
        }
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    public void start() {
        Minecraft.getInstance().getSoundManager().play(this);
    }

    public void requestStop() {
        this.stopped = true;
        Minecraft.getInstance().getSoundManager().stop(this);
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void attachTo(Entity entity) {
        this.parentEntity = entity;
    }

    public void setVolume(float volume) {
        this.maxVolume = volume;
    }

    public float getMaxVolume() {
        return maxVolume;
    }

    public void setRange(float range) {
        this.range = Math.max(0.001F, range);
    }

    public float getRange() {
        return range;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
        this.shouldExpire = true;
    }

    public void keepAlive() {
        this.timeSinceKeepAlive = 0;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isPlaying() {
        return Minecraft.getInstance().getSoundManager().isActive(this);
    }

    private float volumeForDistance(float distance) {
        return Math.max(0.0F, (distance / range) * -maxVolume + maxVolume);
    }
}
