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
}
