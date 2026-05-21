package com.hbm.ntm.block.conveyor;

import com.hbm.ntm.api.conveyor.ConveyorMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TripleConveyorBlock extends ConveyorBlock {
    public TripleConveyorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        return ConveyorMath.closestTripleLaneSnappingPosition(legacyMetadata(level.getBlockState(pos)), pos, itemPos);
    }
}
