package com.hbm.ntm.block.conveyor;

import com.hbm.ntm.api.conveyor.ConveyorMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ExpressConveyorBlock extends ConveyorBlock {
    public ExpressConveyorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        return ConveyorMath.expressTravelLocation(legacyMetadata(level.getBlockState(pos)), pos, itemPos, speed);
    }
}
