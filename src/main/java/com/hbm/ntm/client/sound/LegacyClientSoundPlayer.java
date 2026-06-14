package com.hbm.ntm.client.sound;

import com.hbm.ntm.sound.LegacySoundIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LegacyClientSoundPlayer {
    public static void playUi(String sound, float pitch) {
        ResourceLocation location = LegacySoundIds.resolveLocation(sound);
        if (location == null) {
            return;
        }
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(location), pitch));
    }

    public static void playSoundClient(double x, double y, double z, String sound, SoundSource source,
            float volume, float pitch) {
        ResourceLocation location = LegacySoundIds.resolveLocation(sound);
        if (location == null) {
            return;
        }
        SimpleSoundInstance instance = new SimpleSoundInstance(location,
                source == null ? SoundSource.BLOCKS : source, volume, pitch,
                SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR,
                x, y, z, false);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    private LegacyClientSoundPlayer() {
    }
}
