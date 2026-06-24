package com.hbm.ntm.block.conveyor;

import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.ConveyorPathType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ExpressConveyorBlock extends ConveyorBlock {
    public ExpressConveyorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        return ConveyorMath.expressTravelLocation(legacyMetadata(level.getBlockState(pos)), pos, itemPos, speed);
    }

    @Override
    protected BlockState nextScrewdriverState(BlockState state, int metadata, int baseMetadata,
            ConveyorPathType path, boolean sneaking) {
        return stateFromLegacyMetadata(nextBendableMetadata(metadata, baseMetadata, path, sneaking));
    }
}
