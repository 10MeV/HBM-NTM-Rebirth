package com.hbm.ntm.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class LegacyMachineAudioBridge {
    private LegacyMachineAudioBridge() {
    }

    public static Object updateLoop(Object current, BlockEntity blockEntity, ResourceLocation sound,
            boolean active, double maxDistance, float range) {
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
                () -> () -> com.hbm.ntm.client.sound.LegacyMachineAudio.updateLoop(current, blockEntity, sound, active, maxDistance, range));
    }

    public static Object updateLoop(Object current, BlockEntity blockEntity, String sound,
            boolean active, double maxDistance, float range) {
        return updateLoop(current, blockEntity, LegacySoundIds.resolveLocation(sound), active, maxDistance, range);
    }

    public static Object updateLoop(Object current, BlockEntity blockEntity, ResourceLocation sound,
            boolean active, double maxDistance, float range, float volume, float pitch) {
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
                () -> () -> com.hbm.ntm.client.sound.LegacyMachineAudio.updateLoop(
                        current, blockEntity, sound, active, maxDistance, range, volume, pitch));
    }

    public static Object updateLoop(Object current, BlockEntity blockEntity, String sound,
            boolean active, double maxDistance, float range, float volume, float pitch) {
        return updateLoop(current, blockEntity, LegacySoundIds.resolveLocation(sound),
                active, maxDistance, range, volume, pitch);
    }

    public static void playLocal(BlockEntity blockEntity, ResourceLocation sound, float volume, float pitch,
            double maxDistance) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.hbm.ntm.client.sound.LegacyMachineAudio.playLocal(
                        blockEntity, sound, volume, pitch, maxDistance));
    }

    public static void playLocal(BlockEntity blockEntity, String sound, float volume, float pitch,
            double maxDistance) {
        playLocal(blockEntity, LegacySoundIds.resolveLocation(sound), volume, pitch, maxDistance);
    }
}
