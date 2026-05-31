package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.FluidDuctPaintableExhaustBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidDuctPaintableExhaustBlock extends FluidDuctPaintableBlock {
    private static final FluidType[] SMOKES = {
            HbmFluids.SMOKE,
            HbmFluids.SMOKE_LEADED,
            HbmFluids.SMOKE_POISON
    };

    public FluidDuctPaintableExhaustBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidDuctPaintableExhaustBlockEntity(pos, state);
    }

    @Override
    protected BlockState getConnectionState(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState result = state;
        HbmFluidConnector connector = getFluidConnector(level, pos);
        for (Direction direction : Direction.values()) {
            boolean connects = false;
            for (FluidType type : SMOKES) {
                if (HbmFluidConnectionUtil.canConnect(level, pos, type, connector, direction)) {
                    connects = true;
                    break;
                }
            }
            result = result.setValue(propertyFor(direction), connects);
        }
        return result;
    }
}
