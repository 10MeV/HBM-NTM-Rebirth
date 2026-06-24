package com.hbm.ntm.client;

import com.hbm.ntm.client.particle.LargeExplodeParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ClientExplosionEffects {
    public static void standard(Vec3 center, float size, List<BlockPos> affectedBlocks) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        level.addParticle(size >= 2.0F ? ParticleTypes.EXPLOSION_EMITTER : ParticleTypes.EXPLOSION,
                center.x, center.y, center.z, 1.0D, 0.0D, 0.0D);

        for (BlockPos pos : affectedBlocks) {
            double originX = pos.getX() + level.random.nextFloat();
            double originY = pos.getY() + level.random.nextFloat();
            double originZ = pos.getZ() + level.random.nextFloat();
            double motionX = originX - center.x;
            double motionY = originY - center.y;
            double motionZ = originZ - center.z;
            double distance = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (distance < 1.0E-5D) {
                distance = 1.0D;
            }
            motionX /= distance;
            motionY /= distance;
            motionZ /= distance;
            double modifier = 0.5D / (distance / Math.max(size, 0.1F) + 0.1D);
            modifier *= level.random.nextFloat() * level.random.nextFloat() + 0.3F;
            motionX *= modifier;
            motionY *= modifier;
            motionZ *= modifier;

            Particle explode = LargeExplodeParticle.explode(level,
                    (originX + center.x) * 0.5D,
                    (originY + center.y) * 0.5D,
                    (originZ + center.z) * 0.5D,
                    motionX, motionY, motionZ);
            if (explode != null) {
                Minecraft.getInstance().particleEngine.add(explode);
            } else {
                level.addParticle(ParticleTypes.EXPLOSION,
                        (originX + center.x) * 0.5D,
                        (originY + center.y) * 0.5D,
                        (originZ + center.z) * 0.5D,
                        motionX, motionY, motionZ);
            }
            level.addParticle(ParticleTypes.SMOKE, originX, originY, originZ, motionX, motionY, motionZ);
        }
    }

    private ClientExplosionEffects() {
    }
}
