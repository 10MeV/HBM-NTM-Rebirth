package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.ExplosionEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ExplosionEffectStandard implements ExplosionEffect {
    private final boolean sound;
    private final boolean particles;

    public ExplosionEffectStandard() {
        this(true, true);
    }

    public ExplosionEffectStandard(boolean sound, boolean particles) {
        this.sound = sound;
        this.particles = particles;
    }

    @Override
    public void doEffect(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size, Set<BlockPos> affectedBlocks) {
        if (sound) {
            level.playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                    4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
        }
        if (particles) {
            if (size >= 2.0F && !affectedBlocks.isEmpty()) {
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, position.x, position.y, position.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            } else {
                level.sendParticles(ParticleTypes.EXPLOSION, position.x, position.y, position.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
