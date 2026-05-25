package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.ExplosionEffect;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
        ParticleUtil.spawnExplosionSmall(level, position.x, position.y, position.z, cloudCount, cloudScale, cloudSpeedMultiplier);
    }
}
