package com.hbm.ntm.api.fluid;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Legacy-name bridge for Fluid MK2 pipe node creation.
 */
@Deprecated(forRemoval = false)
public interface IFluidPipeMK2 extends IFluidConnectorMK2 {
    default HbmFluidNode createNode(FluidType type) {
        if (this instanceof BlockEntity blockEntity) {
            return createNode(type, blockEntity.getLevel(), blockEntity.getBlockPos());
        }
        return new HbmFluidNode(BlockPos.ZERO, type);
    }

    default HbmFluidNode createNode(FluidType type, Level level, BlockPos pos) {
        return new HbmFluidNode(pos, type,
                level == null || pos == null ? java.util.Set.of()
                        : HbmFluidConnectionUtil.collectNodeConnections(level, pos, type, this));
    }
}
