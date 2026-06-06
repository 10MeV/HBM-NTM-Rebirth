package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.ExplosionEffect;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ExplosionEffectEnergy implements ExplosionEffect {
    private final float red;
    private final float green;
    private final float blue;
    private final float scale;

    public ExplosionEffectEnergy(float red, float green, float blue, float scale) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.scale = scale;
    }

    @Override
    public void doEffect(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size, Set<BlockPos> affectedBlocks) {
        level.playSound(null, position.x, position.y, position.z, ModSounds.ENTITY_UFO_BLAST.get(), SoundSource.BLOCKS,
                5.0F, 0.9F + level.random.nextFloat() * 0.2F);
        level.playSound(null, position.x, position.y, position.z, SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.BLOCKS,
                5.0F, 0.5F);

        float yaw = level.random.nextFloat() * 180.0F;
        for (int i = 0; i < 3; i++) {
            ParticleUtil.spawnPlasmaBlast(level, position.x, position.y + 0.125D, position.z,
                    red, green, blue, -60.0F + 60.0F * i, yaw, scale);
        }
    }
}
