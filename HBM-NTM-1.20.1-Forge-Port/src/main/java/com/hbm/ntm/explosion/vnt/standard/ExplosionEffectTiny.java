package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.ExplosionEffect;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ExplosionEffectTiny implements ExplosionEffect {
    @Override
    public void doEffect(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size, Set<BlockPos> affectedBlocks) {
        level.playSound(null, position.x, position.y, position.z, ModSounds.WEAPON_EXPLOSION_TINY.get(), SoundSource.BLOCKS, 15.0F, 1.0F);

        CompoundTag data = new CompoundTag();
        data.putString("type", "vanillaExt");
        data.putString("mode", "largeexplode");
        data.putFloat("size", 1.5F);
        data.putByte("count", (byte) 1);
        ParticleUtil.spawnAux(level, position.x, position.y, position.z, data, 100.0D);
    }
}
