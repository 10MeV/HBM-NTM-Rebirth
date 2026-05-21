package com.hbm.ntm.explosion;

import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class ExplosionHurtUtil {
    public static void doRadiation(Level level, double x, double y, double z, float outer, float inner, double radius) {
        if (level == null || level.isClientSide() || radius <= 0.0D) {
            return;
        }

        AABB bounds = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, bounds)) {
            double distance = entity.distanceToSqr(x, y, z);
            if (distance > radius * radius) {
                continue;
            }
            float interpolation = 1.0F - (float) (Math.sqrt(distance) / radius);
            float radiation = outer + (inner - outer) * interpolation;
            RadiationUtil.contaminate(entity, HazardType.RADIATION, RadiationUtil.ContaminationType.CREATIVE, radiation);
        }
    }

    private ExplosionHurtUtil() {
    }
}
