package com.hbm.ntm.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LegacyMachineAudio {
    private LegacyMachineAudio() {
    }

    public static Object updateLoop(Object current, BlockEntity blockEntity, ResourceLocation sound,
            boolean active, double maxDistance, float range) {
        SoundLoopMachine loop = current instanceof SoundLoopMachine machine ? machine : null;
        Minecraft minecraft = Minecraft.getInstance();
        boolean audible = active
                && minecraft.player != null
                && minecraft.player.distanceToSqr(blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ())
                < maxDistance * maxDistance;
        if (audible) {
            if (loop == null || !loop.isPlaying()) {
                loop = new SoundLoopMachine(sound, blockEntity);
                loop.setRange(range);
                loop.setKeepAlive(20);
                loop.start();
            }
            loop.keepAlive();
            return loop;
        }
        if (loop != null) {
            loop.requestStop();
        }
        return null;
    }
}
