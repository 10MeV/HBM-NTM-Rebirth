package com.hbm.ntm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ClientExplosionEffects {
    public static void standard(Vec3 center, float size, List<BlockPos> affectedBlocks) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        level.playLocalSound(center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F, false);
        level.addParticle(size >= 2.0F && !affectedBlocks.isEmpty() ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION,
                center.x, center.y, center.z, 0.0D, 0.0D, 0.0D);

        int samples = Math.min(affectedBlocks.size(), 64);
        for (int i = 0; i < samples; i++) {
            BlockPos pos = affectedBlocks.get(i * affectedBlocks.size() / samples);
            level.addParticle(ParticleTypes.POOF, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    (level.random.nextDouble() - 0.5D) * 0.2D,
                    level.random.nextDouble() * 0.2D,
                    (level.random.nextDouble() - 0.5D) * 0.2D);
        }
    }

    private ClientExplosionEffects() {
    }
}
