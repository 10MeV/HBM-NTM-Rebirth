package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.CrashedBombBlock;
import com.hbm.ntm.block.CrashedBombType;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class CrashedBombBlockEntity extends BlockEntity {
    public CrashedBombBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRASHED_BOMB.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrashedBombBlockEntity blockEntity) {
        if (level.getGameTime() % 2L != 0L || !(state.getBlock() instanceof CrashedBombBlock)) {
            return;
        }
        CrashedBombType type = state.getValue(CrashedBombBlock.TYPE);
        float amount = switch (type) {
            case BALEFIRE -> 1.0F;
            case NUKE -> 0.25F;
            case SALTED -> 0.5F;
            case CONVENTIONAL -> 0.0F;
        };
        double range = type == CrashedBombType.BALEFIRE ? 15.0D : 10.0D;
        if (amount <= 0.0F) {
            return;
        }
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(x, y, z, x, y, z).inflate(range))) {
            double dx = entity.getX() - x;
            double dy = entity.getY() + entity.getBbHeight() * 0.5D - y;
            double dz = entity.getZ() - z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance <= range) {
                RadiationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE,
                        amount * (float) (1.0D - distance / range));
            }
        }
    }
}
