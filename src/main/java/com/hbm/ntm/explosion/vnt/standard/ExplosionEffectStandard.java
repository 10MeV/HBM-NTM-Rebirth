package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.ExplosionEffect;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ExplosionEffectStandard implements ExplosionEffect {
    private static final int MAX_BLOCK_PARTICLE_POSITIONS = 512;
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
            LegacySoundPlayer.playLegacyExplosion(level, position);
        }
        if (particles) {
            ParticleUtil.spawnVntExplosion(level, position.x, position.y, position.z, size, sampledPositions(affectedBlocks));
        }
    }

    private static long[] sampledPositions(Set<BlockPos> affectedBlocks) {
        int limit = Math.min(affectedBlocks.size(), MAX_BLOCK_PARTICLE_POSITIONS);
        long[] positions = new long[limit];
        if (limit == 0) {
            return positions;
        }

        int step = Math.max(1, affectedBlocks.size() / limit);
        int index = 0;
        int cursor = 0;
        for (BlockPos pos : affectedBlocks) {
            if (cursor++ % step == 0) {
                positions[index++] = pos.asLong();
                if (index >= positions.length) {
                    break;
                }
            }
        }
        return positions;
    }
}
