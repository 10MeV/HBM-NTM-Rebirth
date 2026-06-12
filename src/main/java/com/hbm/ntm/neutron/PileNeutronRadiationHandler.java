package com.hbm.ntm.neutron;

import com.hbm.ntm.util.ContaminationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@FunctionalInterface
public interface PileNeutronRadiationHandler {
    PileNeutronRadiationHandler NOOP = (level, pos, radiation) -> {
    };
    PileNeutronRadiationHandler LEGACY_CONTAMINATION = (level, pos, radiation) -> {
        if (level == null || pos == null || radiation <= 0.0F) {
            return;
        }
        int x = (int) (pos.getX() + 0.5D);
        int y = (int) (pos.getY() + 0.5D);
        int z = (int) (pos.getZ() + 0.5D);
        AABB probe = new AABB(x, y, z, x, y, z);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, probe)) {
            ContaminationUtil.contaminate(
                    entity,
                    ContaminationUtil.HazardType.RADIATION,
                    ContaminationUtil.ContaminationType.CREATIVE,
                    radiation);
        }
    };

    void radiateEntities(Level level, BlockPos pos, float radiation);
}
