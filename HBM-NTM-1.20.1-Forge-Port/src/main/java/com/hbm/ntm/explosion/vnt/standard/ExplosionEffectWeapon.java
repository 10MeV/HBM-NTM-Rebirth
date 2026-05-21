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

public class ExplosionEffectWeapon implements ExplosionEffect {
    private final int cloudCount;
    private final float cloudScale;
    private final float cloudSpeedMultiplier;

    public ExplosionEffectWeapon(int cloudCount, float cloudScale, float cloudSpeedMultiplier) {
        this.cloudCount = cloudCount;
        this.cloudScale = cloudScale;
        this.cloudSpeedMultiplier = cloudSpeedMultiplier;
    }

    @Override
    public void doEffect(ExplosionVnt explosion, ServerLevel level, Vec3 position, float size, Set<BlockPos> affectedBlocks) {
        level.playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.8F);
        CompoundTag data = new CompoundTag();
        data.putString("type", "weaponExplosion");
        data.putInt("count", cloudCount);
        data.putFloat("scale", cloudScale);
        data.putFloat("speed", cloudSpeedMultiplier);
        ParticleUtil.spawnAux(level, position.x, position.y, position.z, data, 150.0D);
    }
}
