package com.hbm.ntm.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class LegacyMachineAudio {
    private LegacyMachineAudio() {
    }

    public static Object updateLoop(Object current, BlockEntity blockEntity, ResourceLocation sound,
            boolean active, double maxDistance, float range) {
        return updateLoop(current, blockEntity, sound, active, maxDistance, range, 1.0F, 1.0F);
    }

    public static Object updateLoop(Object current, BlockEntity blockEntity, ResourceLocation sound,
            boolean active, double maxDistance, float range, float volume, float pitch) {
        return updateLoop(current, blockEntity, sound, active, maxDistance, range, volume, pitch,
                0.5D, 0.5D, 0.5D);
    }

    public static Object updateLoop(Object current, BlockEntity blockEntity, ResourceLocation sound,
            boolean active, double maxDistance, float range, float volume, float pitch,
            double offsetX, double offsetY, double offsetZ) {
        SoundLoopMachine loop = current instanceof SoundLoopMachine machine ? machine : null;
        if (sound == null) {
            if (loop != null) {
                loop.requestStop();
            }
            return null;
        }
        Minecraft minecraft = Minecraft.getInstance();
        double soundX = blockEntity.getBlockPos().getX() + offsetX;
        double soundY = blockEntity.getBlockPos().getY() + offsetY;
        double soundZ = blockEntity.getBlockPos().getZ() + offsetZ;
        boolean audible = active
                && minecraft.player != null
                && minecraft.player.distanceToSqr(soundX, soundY, soundZ) < maxDistance * maxDistance;
        if (audible) {
            if (loop == null || !loop.isPlaying()) {
                loop = new SoundLoopMachine(sound, blockEntity, SoundSource.BLOCKS, offsetX, offsetY, offsetZ);
                loop.setRange(range);
                loop.setKeepAlive(20);
                loop.start();
            }
            loop.setPosition(soundX, soundY, soundZ);
            loop.setVolume(volume);
            loop.setPitch(pitch);
            loop.keepAlive();
            return loop;
        }
        if (loop != null) {
            loop.requestStop();
        }
        return null;
    }

    public static void playLocal(BlockEntity blockEntity, ResourceLocation sound, float volume, float pitch,
            double maxDistance) {
        if (sound == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (minecraft.player.distanceToSqr(blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ())
                >= maxDistance * maxDistance) {
            return;
        }
        minecraft.level.playLocalSound(
                blockEntity.getBlockPos().getX() + 0.5D,
                blockEntity.getBlockPos().getY() + 0.5D,
                blockEntity.getBlockPos().getZ() + 0.5D,
                SoundEvent.createVariableRangeEvent(sound),
                SoundSource.BLOCKS,
                volume,
                pitch,
                false);
    }
}
