package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.ExplosionEffect;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ExplosionEffectAmat implements ExplosionEffect {
    @Override
    public void doEffect(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size, Set<BlockPos> affectedBlocks) {
        level.playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                size < 15.0F ? 4.0F : 15.0F, size < 15.0F ? 1.0F : 0.75F);
        CompoundTag data = new CompoundTag();
        data.putString("type", "amat");
        data.putFloat("scale", size);
        ParticleUtil.spawnAux(level, position.x, position.y, position.z, data, 200.0D);
    }
}
