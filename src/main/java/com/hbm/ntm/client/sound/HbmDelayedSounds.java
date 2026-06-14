package com.hbm.ntm.client.sound;

import com.hbm.ntm.sound.LegacySoundIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class HbmDelayedSounds {
    private static final double LEGACY_SPEED_OF_SOUND = 17.15D * 0.5D;

    public static void playExplosionLarge(double x, double y, double z, float soundRange) {
        playDistanceDelayedExplosion(x, y, z, Math.max(1.0F, soundRange),
                "hbm:weapon.explosionLargeNear", "hbm:weapon.explosionLargeFar", 1000.0F);
    }

    public static void playExplosionSmall(double x, double y, double z) {
        playDistanceDelayedExplosion(x, y, z, 200.0F,
                "hbm:weapon.explosionSmallNear", "hbm:weapon.explosionSmallFar", 100.0F);
    }

    private static void playDistanceDelayedExplosion(double x, double y, double z, float soundRange,
            String nearSound, String farSound, float volume) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        float distance = (float) player.distanceToSqr(x, y, z);
        distance = Mth.sqrt(distance);
        if (distance > soundRange) {
            return;
        }
        ResourceLocation sound = LegacySoundIds.resolveLocation(distance <= soundRange * 0.4F ? nearSound : farSound);
        if (sound == null) {
            return;
        }
        RandomSource random = player.getRandom();
        float pitch = 0.9F + random.nextFloat() * 0.2F;
        int delay = Math.max(0, (int) (distance / LEGACY_SPEED_OF_SOUND));
        SimpleSoundInstance instance = new SimpleSoundInstance(sound, SoundSource.BLOCKS, volume, pitch,
                SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, x, y, z, false);
        minecraft.getSoundManager().playDelayed(instance, delay);
    }

    private HbmDelayedSounds() {
    }
}
