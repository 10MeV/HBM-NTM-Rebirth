package com.hbm.entity.mob;

import api.hbm.entity.IRadiationImmune;
import com.hbm.ntm.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Legacy 1.7.10 package bridge for the tainted creeper entity.
 *
 * <p>The modern runtime owns the behavior; this subclass restores the old FQCN
 * and marker-interface identity for migrated source without registering a
 * second entity type.
 */
@Deprecated(forRemoval = false)
public class EntityCreeperTainted extends com.hbm.ntm.entity.mob.EntityCreeperTainted implements IRadiationImmune {
    public EntityCreeperTainted(EntityType<? extends com.hbm.ntm.entity.mob.EntityCreeperTainted> type,
            Level level) {
        super(type, level);
    }

    public EntityCreeperTainted(Level level) {
        this(ModEntityTypes.TAINTED_CREEPER.get(), level);
    }

    public static boolean hasPosNeightbour(Level level, int x, int y, int z) {
        return hasSolidNeighbor(level, new BlockPos(x + 1, y, z))
                || hasSolidNeighbor(level, new BlockPos(x, y + 1, z))
                || hasSolidNeighbor(level, new BlockPos(x, y, z + 1))
                || hasSolidNeighbor(level, new BlockPos(x - 1, y, z))
                || hasSolidNeighbor(level, new BlockPos(x, y - 1, z))
                || hasSolidNeighbor(level, new BlockPos(x, y, z - 1));
    }

    public static boolean hasPosNeightbour(Level level, BlockPos pos) {
        return hasPosNeightbour(level, pos.getX(), pos.getY(), pos.getZ());
    }

    private static boolean hasSolidNeighbor(Level level, BlockPos pos) {
        return level.getBlockState(pos).isSolidRender(level, pos);
    }
}
